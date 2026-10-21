package dev.gaens.filmory.feature.title.presentation

import app.cash.turbine.test
import dev.gaens.filmory.feature.episode.domain.AdvanceEpisode
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.settings.domain.AppSettings
import dev.gaens.filmory.feature.settings.domain.MarkerTarget
import dev.gaens.filmory.feature.settings.domain.SeriesSettings
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
import dev.gaens.filmory.testing.FakeSettingsRepository
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
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
    private val settings = FakeSettingsRepository()
    private val sync = SyncTitleStatus(titles, watchings, DetermineTitleStatus())
    private val start = StartWatching(watchings, sync, ids, clock)

    private fun viewModel() = TitleDetailViewModel(
        "title", titles, watchings, episodes, notes, FakeCollectionRepository(),
        ChangeTitleStatus(titles, watchings, ids, clock), ToggleWatchLater(titles), start, SaveWatching(watchings, sync),
        DeleteWatching(watchings, sync), EnsureActiveWatching(watchings, start, sync, clock),
        AdvanceEpisode(watchings, episodes, ids, clock), DeleteTitle(titles, notes, FakeNoteImageStorage()), settings, ids, clock,
    )

    private fun followTicks(target: MarkerTarget = MarkerTarget.NEXT) {
        settings.settings.value = AppSettings(series = SeriesSettings(markerFollowsTicks = true, markerTarget = target))
    }

    private suspend fun markerAfter(action: TitleDetailAction): String? {
        val viewModel = viewModel()
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(action)
            expectMostRecentItem()
        }
        return watchings.getWatching("w")?.currentEpisodeId
    }

    @Test
    fun `ticking an episode leaves the marker alone by default`() = runTest {
        watchings.upsertWatching(aWatching(id = "w", currentEpisodeId = "e1"))

        assertEquals("e1", markerAfter(TitleDetailAction.OnEpisodeCheckClick("e1")))
    }

    @Test
    fun `ticking moves the marker to the next episode when asked to`() = runTest {
        followTicks()
        watchings.upsertWatching(aWatching(id = "w", currentEpisodeId = "e1"))

        assertEquals("e2", markerAfter(TitleDetailAction.OnEpisodeCheckClick("e1")))
    }

    @Test
    fun `ticking can put the marker on the ticked episode`() = runTest {
        followTicks(MarkerTarget.TICKED)
        watchings.upsertWatching(aWatching(id = "w", currentEpisodeId = "e1"))

        assertEquals("e2", markerAfter(TitleDetailAction.OnEpisodeCheckClick("e2")))
    }

    @Test
    fun `marking a season moves the marker past its last episode`() = runTest {
        followTicks()
        watchings.upsertWatching(aWatching(id = "w", currentEpisodeId = "e1"))

        assertEquals("e3", markerAfter(TitleDetailAction.OnMarkSeasonClick))
    }

    @Test
    fun `checking an episode on a fresh series starts a watching`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(TitleDetailAction.OnEpisodeCheckClick("e2"))

            val state = expectMostRecentItem()
            assertEquals(TitleStatus.WATCHING, state.title?.status)
            assertEquals(1, state.series?.checkedCount)
            assertEquals(EpisodeMark.WATCHED, state.series?.seasons?.first()?.episodes?.single { it.id == "e2" }?.mark)
        }
    }

    @Test
    fun `tapping an episode cycles watched, skipped, not watched, and skipped counts as checked`() = runTest {
        watchings.upsertWatching(aWatching(id = "w"))
        val viewModel = viewModel()
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(TitleDetailAction.OnEpisodeCheckClick("e1"))
            val watched = expectMostRecentItem().series
            assertEquals(1, watched?.watchedCount)
            assertEquals(0, watched?.skippedCount)

            viewModel.onAction(TitleDetailAction.OnEpisodeCheckClick("e1"))
            val skipped = expectMostRecentItem().series
            assertEquals(0, skipped?.watchedCount)
            assertEquals(1, skipped?.skippedCount)
            assertEquals(1, skipped?.checkedCount)
            assertEquals(1, skipped?.seasons?.first()?.checkedCount)
            assertEquals(EpisodeMark.SKIPPED, skipped?.seasons?.first()?.episodes?.first()?.mark)

            viewModel.onAction(TitleDetailAction.OnEpisodeCheckClick("e1"))
            assertEquals(0, expectMostRecentItem().series?.checkedCount)
        }
    }

    @Test
    fun `skipping does not move the marker and marking a season keeps skipped episodes skipped`() = runTest {
        followTicks()
        watchings.upsertWatching(aWatching(id = "w", currentEpisodeId = "e1"))
        val viewModel = viewModel()
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(TitleDetailAction.OnEpisodeCheckClick("e1"))
            viewModel.onAction(TitleDetailAction.OnEpisodeCheckClick("e1"))
            expectMostRecentItem()
            assertEquals("e2", watchings.getWatching("w")?.currentEpisodeId)

            viewModel.onAction(TitleDetailAction.OnMarkSeasonClick)
            val series = expectMostRecentItem().series
            assertEquals(1, series?.watchedCount)
            assertEquals(1, series?.skippedCount)
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
    fun `notes filter by several watchings and all watchings clears the selection`() = runTest {
        watchings.upsertWatching(aWatching(id = "w1", createdAt = at(1)))
        watchings.upsertWatching(aWatching(id = "w2", createdAt = at(2)))
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(2, expectMostRecentItem().notes.size)

            viewModel.onAction(TitleDetailAction.OnNoteWatchingToggle("w1"))
            val state = expectMostRecentItem()
            assertEquals(listOf("n1"), state.notes.map { it.id })
            assertEquals(2, state.noteCount)

            viewModel.onAction(TitleDetailAction.OnNoteWatchingToggle("w2"))
            assertEquals(setOf("w1", "w2"), expectMostRecentItem().noteWatchingFilter)

            viewModel.onAction(TitleDetailAction.OnNoteWatchingToggle("w1"))
            assertEquals(listOf("n2"), expectMostRecentItem().notes.map { it.id })

            viewModel.onAction(TitleDetailAction.OnNoteWatchingFilterClear)
            val cleared = expectMostRecentItem()
            assertEquals(emptySet(), cleared.noteWatchingFilter)
            assertEquals(2, cleared.notes.size)
        }
    }

    @Test
    fun `a finish date before the start date is reported inside the sheet`() = runTest {
        watchings.upsertWatching(aWatching(id = "w1", startedAt = at(10)))
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(TitleDetailAction.OnWatchingClick("w1"))
            viewModel.onAction(TitleDetailAction.OnDraftFinishDateChange(LocalDate(1970, 1, 1)))
            viewModel.onAction(TitleDetailAction.OnDraftSave)

            val sheet = expectMostRecentItem().sheet as TitleDetailSheet.EditWatching
            assertNotNull(sheet.error)

            viewModel.onAction(TitleDetailAction.OnDraftFinishDateChange(null))
            assertNull((expectMostRecentItem().sheet as TitleDetailSheet.EditWatching).error)
        }
    }

    @Test
    fun `closing the editor untouched just closes it`() = runTest {
        watchings.upsertWatching(aWatching(id = "w1"))
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(TitleDetailAction.OnWatchingClick("w1"))
            viewModel.onAction(TitleDetailAction.OnDraftDismiss)

            assertNull(expectMostRecentItem().sheet)
        }
    }

    @Test
    fun `closing an edited editor asks first and keep editing goes back`() = runTest {
        watchings.upsertWatching(aWatching(id = "w1"))
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(TitleDetailAction.OnWatchingClick("w1"))
            viewModel.onAction(TitleDetailAction.OnDraftRatingChange(7))
            viewModel.onAction(TitleDetailAction.OnDraftDismiss)
            assertEquals(true, (expectMostRecentItem().sheet as TitleDetailSheet.EditWatching).isAskingToSave)

            viewModel.onAction(TitleDetailAction.OnDraftKeepEditing)
            assertEquals(false, (expectMostRecentItem().sheet as TitleDetailSheet.EditWatching).isAskingToSave)
        }
    }

    @Test
    fun `the you are here badge opens the marker settings sheet`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(TitleDetailAction.OnMarkerSettingsClick)
            assertEquals(TitleDetailSheet.MarkerSettings, expectMostRecentItem().sheet)

            viewModel.onAction(TitleDetailAction.OnSheetDismiss)
            assertNull(expectMostRecentItem().sheet)
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
