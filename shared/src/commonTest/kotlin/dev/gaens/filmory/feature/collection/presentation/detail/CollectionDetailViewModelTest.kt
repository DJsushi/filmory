package dev.gaens.filmory.feature.collection.presentation.detail

import app.cash.turbine.test
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.ViewModelTest
import dev.gaens.filmory.testing.at
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollectionDetailViewModelTest : ViewModelTest() {
    private val collections = FakeCollectionRepository(
        listOf(
            TitleCollection("c", "Mine", listOf("show"), at(0)),
            TitleCollection(TitleCollection.DEFAULT_COLLECTION_ID, "Favorites", emptyList(), at(0)),
        ),
    )

    private fun viewModel(id: String = "c") = CollectionDetailViewModel(id, collections)

    @Test
    fun `it shows the collection name and whether it is the default one`() = runTest {
        viewModel().state.test {
            val state = expectMostRecentItem()
            assertEquals("Mine", state.name)
            assertFalse(state.isDefault)
        }
        viewModel(TitleCollection.DEFAULT_COLLECTION_ID).state.test {
            assertTrue(expectMostRecentItem().isDefault)
        }
    }

    @Test
    fun `renaming closes the dialog and changes the name`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(CollectionDetailAction.OnDialogOpen(CollectionDetailDialog.RENAME))
            assertEquals(CollectionDetailDialog.RENAME, expectMostRecentItem().dialog)

            viewModel.onAction(CollectionDetailAction.OnRenameConfirm("Rainy day"))

            val state = expectMostRecentItem()
            assertNull(state.dialog)
            assertEquals("Rainy day", state.name)
        }
    }

    @Test
    fun `deleting removes the collection and reports it`() = runTest {
        val viewModel = viewModel()
        viewModel.events.test {
            viewModel.onAction(CollectionDetailAction.OnDeleteConfirm)

            assertEquals(CollectionDetailEvent.Deleted, awaitItem())
        }
        assertEquals(listOf(TitleCollection.DEFAULT_COLLECTION_ID), collections.collections.value.map { it.id })
    }
}
