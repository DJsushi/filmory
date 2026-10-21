package dev.gaens.filmory.feature.collection.domain

/**
 * What stands for a collection. It is saved as one string, [key], so the database needs no more
 * than one column: `symbol:<name>`, `emoji:<emoji>` or `photo:<relative path>`.
 */
sealed interface CollectionIcon {
    val key: String

    /** One of the app's drawn symbols, by name. */
    data class Symbol(val name: String) : CollectionIcon {
        override val key get() = "$SYMBOL_PREFIX$name"
    }

    data class Emoji(val text: String) : CollectionIcon {
        override val key get() = "$EMOJI_PREFIX$text"
    }

    /** A picked image kept in the app's image storage. */
    data class Photo(val path: String) : CollectionIcon {
        override val key get() = "$PHOTO_PREFIX$path"
    }
}

private const val SYMBOL_PREFIX = "symbol:"
private const val EMOJI_PREFIX = "emoji:"
private const val PHOTO_PREFIX = "photo:"

/** The icon a saved key stands for, or null for no key or one this version does not know. */
fun collectionIconOf(key: String?): CollectionIcon? = when {
    key == null -> null
    key.startsWith(SYMBOL_PREFIX) -> key.removePrefix(SYMBOL_PREFIX).takeIf { it.isNotBlank() }?.let(CollectionIcon::Symbol)
    key.startsWith(EMOJI_PREFIX) -> key.removePrefix(EMOJI_PREFIX).takeIf { it.isNotBlank() }?.let(CollectionIcon::Emoji)
    key.startsWith(PHOTO_PREFIX) -> key.removePrefix(PHOTO_PREFIX).takeIf { it.isNotBlank() }?.let(CollectionIcon::Photo)
    else -> null
}
