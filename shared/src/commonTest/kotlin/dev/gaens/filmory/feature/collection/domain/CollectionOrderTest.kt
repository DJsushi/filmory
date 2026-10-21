package dev.gaens.filmory.feature.collection.domain

import app.cash.turbine.test
import dev.gaens.filmory.testing.FakeCollectionRepository
import dev.gaens.filmory.testing.FixedClock
import dev.gaens.filmory.testing.SequentialIdGenerator
import dev.gaens.filmory.testing.at
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionOrderTest {
    private fun collection(id: String, createdAt: Long = 0, sortOrder: Int = 0, isPinned: Boolean = false) =
        TitleCollection(id, id, emptyList(), at(createdAt), sortOrder = sortOrder, isPinned = isPinned)

    @Test
    fun `pinned collections come first, then the chosen order, then the oldest`() {
        val collections = listOf(
            collection("late", createdAt = 9),
            collection("second", sortOrder = 2),
            collection("first", sortOrder = 1),
            collection("old", createdAt = 1),
            collection("pinned", sortOrder = 5, isPinned = true),
        )

        assertEquals(listOf("pinned", "old", "late", "first", "second"), collections.inDisplayOrder().map { it.id })
    }

    @Test
    fun `a new collection goes after the existing ones`() = runTest {
        val repository = FakeCollectionRepository(listOf(collection("a", sortOrder = 3), collection("b", sortOrder = 7)))

        CreateCollection(repository, SequentialIdGenerator(), FixedClock())("Rainy day")

        repository.observeCollections().test {
            val last = expectMostRecentItem().last()
            assertEquals("Rainy day", last.name)
            assertEquals(8, last.sortOrder)
        }
    }

    @Test
    fun `a taken name gets the first free number`() {
        assertEquals("New collection", uniqueCollectionName("New collection", listOf("Favorites")))
        assertEquals("New collection 2", uniqueCollectionName("New collection", listOf("new collection")))
        assertEquals(
            "New collection 3",
            uniqueCollectionName("New collection", listOf("New collection", "New collection 2")),
        )
    }

    @Test
    fun `reordering puts the collections in the given order`() = runTest {
        val repository = FakeCollectionRepository(listOf(collection("a"), collection("b"), collection("c")))

        repository.reorder(listOf("c", "a", "b"))

        repository.observeCollections().test {
            assertEquals(listOf("c", "a", "b"), expectMostRecentItem().map { it.id })
        }
    }
}
