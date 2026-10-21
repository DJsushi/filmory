package dev.gaens.filmory.feature.home.presentation

import app.cash.turbine.test
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.settings.domain.AppSettings
import dev.gaens.filmory.feature.settings.domain.HomeBlock
import dev.gaens.filmory.feature.settings.domain.homeLayoutOf
import dev.gaens.filmory.feature.title.domain.MoveWatchLater
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.title.domain.ToggleWatchLater
import dev.gaens.filmory.feature.watching.domain.FindContinueWatching
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.FakeEpisodeRepository
import dev.gaens.filmory.testing.FakeNoteImageStorage
import dev.gaens.filmory.testing.FakeSettingsRepository
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FakeWatchingRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.ViewModelTest
import dev.gaens.filmory.testing.aSeriesStructure
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HomeViewModelTest : ViewModelTest() {
    private val titles = FakeTitleRepository(
        listOf(
            aTitle(id = "title", status = TitleStatus.WATCHING),
            aTitle(id = "held", status = TitleStatus.ON_HOLD).copy(watchLaterPosition = 0),
            aTitle(id = "wish", type = TitleType.MOVIE).copy(isWishlisted = true, watchLaterPosition = 1),
        ),
    )
    private val watchings = FakeWatchingRepository(listOf(aWatching(currentEpisodeId = "e2")))
    private val episodes = FakeEpisodeRepository(aSeriesStructure()).apply {
        checks.value = listOf(EpisodeCheck("c", "watching", "e1", at(2)))
    }
    private val collections = FakeCollectionRepository(
        listOf(TitleCollection("favorites", "Favorites", listOf("title", "deleted"), at(0))),
    )

    private val settings = FakeSettingsRepository()

    private fun viewModel() = HomeViewModel(
        titles, watchings, episodes, collections, FindContinueWatching(),
        ToggleWatchLater(titles), MoveWatchLater(titles), settings, FakeNoteImageStorage(), FixedClock(),
    )

    @Test
    fun `home shows counts, continue watching progress and the watch later order`() = runTest {
        viewModel().state.test {
            val state = expectMostRecentItem()

            assertEquals(1, state.onHoldCount)
            assertEquals(1, state.wishlistCount)
            assertEquals(3, state.libraryCount)
            assertEquals(listOf("held", "wish"), state.watchLater.map { it.id })
            assertEquals(1, state.collections.single().titleCount)
            val continueWatching = state.continueWatching.single()
            assertEquals(1, continueWatching.seasonNumber)
            assertEquals(2, continueWatching.episodeNumber)
            assertEquals(1f / 3f, continueWatching.progress)
        }
    }

    @Test
    fun `moving a watch later title earlier reorders the row`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(HomeAction.OnMoveWatchLater("wish", moveEarlier = true))

            assertEquals(listOf("wish", "held"), expectMostRecentItem().watchLater.map { it.id })
        }
    }

    @Test
    fun `removing from watch later clears the slot`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(HomeAction.OnRemoveWatchLater("held"))

            assertEquals(listOf("wish"), expectMostRecentItem().watchLater.map { it.id })
        }
    }

    @Test
    fun `home starts with the saved layout`() = runTest {
        settings.settings.value = AppSettings(homeLayout = homeLayoutOf(listOf("LIBRARY")))

        viewModel().state.test {
            assertEquals(HomeBlock.LIBRARY, expectMostRecentItem().layout.first())
        }
    }

    @Test
    fun `moving a block changes the screen at once and saves only when dropped`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(HomeAction.OnEditLayoutClick)
            viewModel.onAction(HomeAction.OnMoveBlock(HomeBlock.LIBRARY, HomeBlock.WATCH_LATER))

            val state = expectMostRecentItem()
            assertEquals(HomeBlock.LIBRARY, state.layout.first())
            assertEquals(HomeBlock.entries, settings.settings.value.homeLayout)

            viewModel.onAction(HomeAction.OnLayoutDropped)

            assertEquals(state.layout, settings.settings.value.homeLayout)
        }
    }

    @Test
    fun `a saved layout arriving while editing does not undo a move`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(HomeAction.OnEditLayoutClick)
            viewModel.onAction(HomeAction.OnMoveBlock(HomeBlock.LIBRARY, HomeBlock.WATCH_LATER))
            settings.settings.value = AppSettings()

            assertEquals(HomeBlock.LIBRARY, expectMostRecentItem().layout.first())
        }
    }

    @Test
    fun `done leaves edit mode and saves the layout`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(HomeAction.OnEditLayoutClick)
            viewModel.onAction(HomeAction.OnMoveBlock(HomeBlock.DROPPED, HomeBlock.WATCH_LATER))
            viewModel.onAction(HomeAction.OnEditLayoutDone)

            val state = expectMostRecentItem()
            assertFalse(state.isEditingLayout)
            assertEquals(HomeBlock.DROPPED, settings.settings.value.homeLayout.first())
        }
    }

    @Test
    fun `nudging a block moves it one place and saves`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(HomeAction.OnNudgeBlock(HomeBlock.WATCH_LATER, offset = 1))

            assertEquals(HomeBlock.WISHLIST, expectMostRecentItem().layout.first())
            assertEquals(HomeBlock.WISHLIST, settings.settings.value.homeLayout.first())
        }
    }

    @Test
    fun `reset restores the default order and saves it`() = runTest {
        settings.settings.value = AppSettings(homeLayout = HomeBlock.entries.reversed())
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(HomeAction.OnResetLayout)

            assertEquals(HomeBlock.entries, expectMostRecentItem().layout)
            assertEquals(HomeBlock.entries, settings.settings.value.homeLayout)
        }
    }
}
