package dev.gaens.filmory.feature.collection.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CollectionIconTest {
    @Test
    fun `every kind of icon survives its key`() {
        listOf(CollectionIcon.Symbol("moon"), CollectionIcon.Emoji("🍿"), CollectionIcon.Photo("note_images/a.jpg"))
            .forEach { assertEquals(it, collectionIconOf(it.key)) }
    }

    @Test
    fun `no key, an empty value or an unknown kind gives no icon`() {
        assertNull(collectionIconOf(null))
        assertNull(collectionIconOf("symbol:"))
        assertNull(collectionIconOf("sticker:happy"))
        assertNull(collectionIconOf("moon"))
    }
}
