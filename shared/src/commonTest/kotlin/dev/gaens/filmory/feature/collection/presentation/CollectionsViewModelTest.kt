package dev.gaens.filmory.feature.collection.presentation

import app.cash.turbine.test
import dev.gaens.filmory.feature.collection.domain.CreateCollection
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.FakeNoteImageStorage
import dev.gaens.filmory.testing.FakeTitleRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.ViewModelTest
import dev.gaens.filmory.testing.at
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionsViewModelTest : ViewModelTest() {
    private val collections = FakeCollectionRepository(
        listOf(TitleCollection(TitleCollection.DEFAULT_COLLECTION_ID, "Favorites", emptyList(), at(0))),
    )
    private val viewModel = CollectionsViewModel(
        collections, FakeTitleRepository(), CreateCollection(collections, SequentialIdGenerator(), FixedClock()),
        FakeNoteImageStorage(),
    )

    @Test
    fun `adding creates the collection at once and asks to open it`() = runTest {
        viewModel.events.test {
            viewModel.onAction(CollectionsAction.OnAddClick("New collection"))

            assertEquals(CollectionsEvent.OpenNewCollection("id-1"), awaitItem())
        }
        assertEquals(listOf("Favorites", "New collection"), collections.collections.value.map { it.name })
    }

    @Test
    fun `a second new collection is numbered`() = runTest {
        viewModel.events.test {
            viewModel.onAction(CollectionsAction.OnAddClick("New collection"))
            awaitItem()
            viewModel.onAction(CollectionsAction.OnAddClick("New collection"))
            awaitItem()
        }

        assertEquals(
            listOf("Favorites", "New collection", "New collection 2"),
            collections.collections.value.map { it.name },
        )
    }
}
