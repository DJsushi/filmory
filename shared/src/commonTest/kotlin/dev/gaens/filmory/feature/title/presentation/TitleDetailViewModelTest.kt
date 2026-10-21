package dev.gaens.filmory.feature.title.presentation

import app.cash.turbine.test
import dev.gaens.filmory.feature.episode.domain.AdvanceEpisode
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.title.domain.ChangeTitleStatus
import dev.gaens.filmory.feature.title.domain.DeleteTitle
import dev.gaens.filmory.testing.FakeNoteImageStorage
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.ToggleWatchLater
import dev.gaens.filmory.feature.watching.domain.DeleteWatching
import dev.gaens.filmory.feature.watching.domain.DetermineTitleStatus
import dev.gaens.filmory.feature.watching.domain.EnsureActiveWatching
import dev.gaens.filmory.feature.watching.domain.SaveWatching
import dev.gaens.filmory.feature.watching.domain.StartWatching
import dev.gaens.filmory.feature.watching.domain.SyncTitleStatus
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.FakeEpisodeRepository
import dev.gaens.filmory.testing.FakeNoteRepository
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FakeWatchingRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.ViewModelTest
import dev.gaens.filmory.testing.aNote
import dev.gaens.filmory.testing.aSeriesStructure
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TitleDetailViewModelTest : ViewModelTest() {
    private val clock = FixedClock(at(500))
    private val ids = SequentialIdGenerator()
    private val titles = FakeTitleRepository(listOf(aTitle()))
    private val watchings = FakeWatchingRepository()
    private val episodes = FakeEpisodeRepository(aSeriesStructure())
    private val notes: NoteRepository = FakeNoteRepository(
        listOf(aNote(id = "n1", watchingId = "w1"), aNote(id = "n2", watchingId = "w2")),
    )
    private val sync = SyncTitleStatus(titles, watchings, DetermineTitleStatus())
    private val start = StartWatching(watchings, sync, ids, clock)

    private fun viewModel() = TitleDetailViewModel(
        "title", titles, watchings, episodes, notes, FakeCollectionRepository(),
        ChangeTitleStatus(titles, watchings, ids, clock), ToggleWatchLater(titles), start, SaveWatching(watchings, sync),
        DeleteWatching(watchings, sync), EnsureActiveWatching(watchings, start, sync, clock),
        AdvanceEpisode(watchings, episodes, ids, clock), DeleteTitle(titles, notes, FakeNoteImageStorage()), ids, clock,
    )

    @Test
    fun `checking an episode on a fresh series starts a watching`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(TitleDetailAction.OnEpisodeCheckClick("e2"))

            val state = expectMostRecentItem()
            assertEquals(TitleStatus.WATCHING, state.title?.status)
            assertEquals(1, state.series?.checkedCount)
            assertTrue(state.series?.seasons?.first()?.episodes?.single { it.id == "e2" }?.isChecked == true)
        }
    }

    @Test
    fun `next episode moves the pointer and marking a season checks the rest`() = runTest {
        watchings.upsertWatching(aWatching(currentEpisodeId = "e1"))
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(TitleDetailAction.OnNextEpisodeClick)
            assertEquals(2, expectMostRecentItem().series?.currentEpisodeNumber)

            viewModel.onAction(TitleDetailAction.OnMarkSeasonClick)
            assertEquals(2, expectMostRecentItem().series?.checkedCount)
        }
    }

    @Test
    fun `notes filter by watching`() = runTest {
        watchings.upsertWatching(aWatching(id = "w1", createdAt = at(1)))
        watchings.upsertWatching(aWatching(id = "w2", createdAt = at(2)))
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(2, expectMostRecentItem().notes.size)

            viewModel.onAction(TitleDetailAction.OnNoteWatchingFilterChange("w1"))
            val state = expectMostRecentItem()
            assertEquals(listOf("n1"), state.notes.map { it.id })
            assertEquals(2, state.noteCount)
        }
    }

    @Test
    fun `wishlist toggles on the title`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(TitleDetailAction.OnWishlistClick)

            assertEquals(true, expectMostRecentItem().title?.isWishlisted)
        }
    }
}
