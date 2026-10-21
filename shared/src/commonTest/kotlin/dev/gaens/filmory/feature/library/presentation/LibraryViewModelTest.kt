package dev.gaens.filmory.feature.library.presentation

import app.cash.turbine.test
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.library.domain.BuildLibrary
import dev.gaens.filmory.feature.settings.domain.LibrarySort
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.FakeSettingsRepository
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FakeWatchingRepository
import dev.gaens.filmory.testing.ViewModelTest
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryViewModelTest : ViewModelTest() {
    private val titles = FakeTitleRepository(
        listOf(
            aTitle(id = "held", status = TitleStatus.ON_HOLD).copy(name = "Held"),
            aTitle(id = "movie", type = TitleType.MOVIE, status = TitleStatus.WATCHED).copy(name = "Movie"),
            aTitle(id = "show", status = TitleStatus.WATCHING).copy(name = "Show"),
        ),
    )
    private val collections = FakeCollectionRepository(listOf(TitleCollection("c", "Mine", listOf("show"), at(0))))
    private val settings = FakeSettingsRepository()

    private fun viewModel(preset: LibraryPreset, collectionId: String? = null) = LibraryViewModel(
        preset, collectionId, titles, FakeWatchingRepository(), collections, settings, BuildLibrary(),
    )

    @Test
    fun `the on hold preset only lists titles on hold`() = runTest {
        viewModel(LibraryPreset.ON_HOLD).state.test {
            assertEquals(listOf("held"), expectMostRecentItem().items.map { it.id })
        }
    }

    @Test
    fun `a collection lists its titles and shows its name`() = runTest {
        viewModel(LibraryPreset.COLLECTION, "c").state.test {
            val state = expectMostRecentItem()
            assertEquals(listOf("show"), state.items.map { it.id })
            assertEquals("Mine", state.collectionName)
        }
    }

    @Test
    fun `type chips toggle and the sort is saved in settings`() = runTest {
        val viewModel = viewModel(LibraryPreset.ALL)
        viewModel.state.test {
            viewModel.onAction(LibraryAction.OnTypeFilterClick(TitleType.MOVIE))
            assertEquals(listOf("movie"), expectMostRecentItem().items.map { it.id })

            viewModel.onAction(LibraryAction.OnTypeFilterClick(TitleType.MOVIE))
            viewModel.onAction(LibraryAction.OnSortChange(LibrarySort.NAME))
            assertEquals(listOf("held", "movie", "show"), expectMostRecentItem().items.map { it.id })
            assertEquals(LibrarySort.NAME, settings.settings.value.library.sort)
        }
    }
}
