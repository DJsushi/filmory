package dev.gaens.filmory.feature.collection.presentation.detail

import app.cash.turbine.test
import dev.gaens.filmory.feature.collection.domain.CollectionIcon
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.FakeImageResizer
import dev.gaens.filmory.testing.FakeNoteImageStorage
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

    private val storage = FakeNoteImageStorage()

    private fun viewModel(id: String = "c", isNew: Boolean = false) =
        CollectionDetailViewModel(id, isNew, collections, storage, FakeImageResizer())

    private fun saved(id: String = "c") = collections.collections.value.single { it.id == id }

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
    fun `a new collection opens with its name being edited and an old one does not`() = runTest {
        viewModel(isNew = true).state.test { assertTrue(expectMostRecentItem().isEditingName) }
        viewModel(isNew = false).state.test { assertFalse(expectMostRecentItem().isEditingName) }
    }

    @Test
    fun `committing a name saves it and ends editing`() = runTest {
        val viewModel = viewModel(isNew = true)
        viewModel.state.test {
            viewModel.onAction(CollectionDetailAction.OnNameCommit("  Rainy day "))

            val state = expectMostRecentItem()
            assertFalse(state.isEditingName)
            assertEquals("Rainy day", state.name)
        }
    }

    @Test
    fun `an empty name keeps the old one`() = runTest {
        val viewModel = viewModel(isNew = true)
        viewModel.state.test {
            viewModel.onAction(CollectionDetailAction.OnNameCommit("   "))

            val state = expectMostRecentItem()
            assertFalse(state.isEditingName)
            assertEquals("Mine", state.name)
        }
    }

    @Test
    fun `a short tap on the name only shows the rename hint`() = runTest {
        val viewModel = viewModel()
        viewModel.events.test {
            viewModel.onAction(CollectionDetailAction.OnNameTap)

            assertEquals(CollectionDetailEvent.ShowRenameHint, awaitItem())
        }
        assertFalse(viewModel.state.value.isEditingName)
    }

    @Test
    fun `a long press starts editing`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(CollectionDetailAction.OnEditNameStart)

            assertTrue(expectMostRecentItem().isEditingName)
        }
    }

    @Test
    fun `an icon and a color are saved and can be cleared again`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(CollectionDetailAction.OnIconChange(CollectionIcon.Emoji("🍿")))
            viewModel.onAction(CollectionDetailAction.OnColorChange(0xFF346A97))
            assertEquals("emoji:🍿", saved().iconKey)
            assertEquals(0xFF346A97, saved().colorArgb)
            assertEquals(0xFF346A97, expectMostRecentItem().look.colorArgb)

            viewModel.onAction(CollectionDetailAction.OnIconChange(null))
            viewModel.onAction(CollectionDetailAction.OnColorChange(null))
            assertNull(saved().iconKey)
            assertNull(saved().colorArgb)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a picked photo becomes the icon and replacing it deletes the old file`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(CollectionDetailAction.OnPhotoPicked(byteArrayOf(9)))
            assertEquals("photo:note_images/1.jpg", saved().iconKey)
            assertEquals("/files/note_images/1.jpg", expectMostRecentItem().look.photoPath)

            viewModel.onAction(CollectionDetailAction.OnIconChange(CollectionIcon.Symbol("moon")))
            assertEquals("symbol:moon", saved().iconKey)
            assertFalse("note_images/1.jpg" in storage.files)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the appearance sheet opens and closes`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            viewModel.onAction(CollectionDetailAction.OnAppearanceClick)
            assertTrue(expectMostRecentItem().isAppearanceSheetOpen)

            viewModel.onAction(CollectionDetailAction.OnAppearanceDismiss)
            assertFalse(expectMostRecentItem().isAppearanceSheetOpen)
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
