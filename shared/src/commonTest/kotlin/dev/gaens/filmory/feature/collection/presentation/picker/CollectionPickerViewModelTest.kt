package dev.gaens.filmory.feature.collection.presentation.picker

import app.cash.turbine.test
import dev.gaens.filmory.feature.collection.domain.CreateCollection
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.FakeNoteImageStorage
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.ViewModelTest
import dev.gaens.filmory.testing.at
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollectionPickerViewModelTest : ViewModelTest() {
    private val collections = FakeCollectionRepository(
        listOf(
            TitleCollection(TitleCollection.DEFAULT_COLLECTION_ID, "Favorites", emptyList(), at(0)),
            TitleCollection("a", "A", listOf("title"), at(1), sortOrder = 1),
            TitleCollection("b", "B", emptyList(), at(2), sortOrder = 2),
            TitleCollection("c", "C", emptyList(), at(3), sortOrder = 3),
        ),
    )
    // Built on first use, so its scope runs on the dispatcher the test sets up.
    private val viewModel by lazy {
        CollectionPickerViewModel(
            "title", collections, CreateCollection(collections, SequentialIdGenerator(), FixedClock()), FakeNoteImageStorage(),
        )
    }

    private fun saved(id: String) = collections.collections.value.single { it.id == id }

    @Test
    fun `it lists the collections and which ones hold the title`() = runTest {
        viewModel.state.test {
            val items = expectMostRecentItem().collections

            assertEquals(listOf("favorites", "a", "b", "c"), items.map { it.id })
            assertEquals(listOf(false, true, false, false), items.map { it.containsTitle })
        }
    }

    @Test
    fun `toggling adds the title to a collection and removes it again`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnToggle("b"))
            assertEquals(listOf("title"), saved("b").titleIds)

            viewModel.onAction(CollectionPickerAction.OnToggle("a"))
            assertEquals(emptyList(), saved("a").titleIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pinning puts a collection first and unpinning puts it back`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnPinClick("c"))
            assertEquals(listOf("c", "favorites", "a", "b"), expectMostRecentItem().collections.map { it.id })

            viewModel.onAction(CollectionPickerAction.OnPinClick("c"))
            assertEquals(listOf("favorites", "a", "b", "c"), expectMostRecentItem().collections.map { it.id })
        }
    }

    @Test
    fun `dragging shows the new order at once and dropping saves it`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnMove("c", "a"))
            assertEquals(listOf("favorites", "c", "a", "b"), expectMostRecentItem().collections.map { it.id })
            assertEquals(1, saved("a").sortOrder)

            // The saved order now matches what was on screen, so the screen does not change again.
            viewModel.onAction(CollectionPickerAction.OnMoveDropped)
            assertEquals(listOf("c", "a", "b"), collections.collections.value.sortedBy { it.sortOrder }.map { it.id }.filter { it != "favorites" })
            assertTrue(saved("c").sortOrder < saved("a").sortOrder)
            expectNoEvents()
        }
    }

    @Test
    fun `a pinned collection cannot be dragged among the others`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnPinClick("b"))
            val before = expectMostRecentItem().collections.map { it.id }

            viewModel.onAction(CollectionPickerAction.OnMove("c", "b"))

            expectNoEvents()
            assertEquals(listOf("b", "favorites", "a", "c"), before)
        }
    }

    @Test
    fun `renaming asks for the name and saves it`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnRenameClick("a"))
            assertEquals(CollectionPickerDialog.Rename("a", "A"), expectMostRecentItem().dialog)

            viewModel.onAction(CollectionPickerAction.OnRenameConfirm("  Alpha "))
            assertEquals("Alpha", saved("a").name)
            assertNull(expectMostRecentItem().dialog)
        }
    }

    @Test
    fun `deleting asks first, removes the collection, and never touches Favorites`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnDeleteClick(TitleCollection.DEFAULT_COLLECTION_ID))
            expectNoEvents()

            viewModel.onAction(CollectionPickerAction.OnDeleteClick("b"))
            assertEquals(CollectionPickerDialog.Delete("b"), expectMostRecentItem().dialog)
            assertEquals(4, collections.collections.value.size)

            viewModel.onAction(CollectionPickerAction.OnDeleteConfirm)
            assertEquals(listOf("favorites", "a", "c"), expectMostRecentItem().collections.map { it.id })
        }
    }

    @Test
    fun `adding creates a numbered collection and asks for its name`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnEditClick)
            viewModel.onAction(CollectionPickerAction.OnAddClick("New collection"))

            val state = expectMostRecentItem()
            assertTrue(state.isEditing)
            assertEquals("New collection", state.collections.last().name)
            assertEquals(CollectionPickerDialog.Rename("id-1", "New collection"), state.dialog)
        }
    }

    @Test
    fun `leaving edit mode closes any dialog`() = runTest {
        viewModel.state.test {
            expectMostRecentItem()
            viewModel.onAction(CollectionPickerAction.OnEditClick)
            viewModel.onAction(CollectionPickerAction.OnRenameClick("a"))
            expectMostRecentItem()

            viewModel.onAction(CollectionPickerAction.OnEditDone)

            val state = expectMostRecentItem()
            assertFalse(state.isEditing)
            assertNull(state.dialog)
        }
    }
}
