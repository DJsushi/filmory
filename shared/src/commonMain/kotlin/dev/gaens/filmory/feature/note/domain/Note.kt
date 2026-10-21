package dev.gaens.filmory.feature.note.domain

import kotlin.time.Instant

/**
 * A note on a watching, optionally on one episode. [titleId] and [commentCount] are filled when
 * reading and ignored when saving.
 */
data class Note(
    val id: String,
    val watchingId: String,
    val titleId: String,
    val episodeId: String?,
    val type: NoteType,
    val text: String,
    val timestampMillis: Long?,
    val imagePath: String?,
    val isFavorite: Boolean,
    val commentCount: Int,
    val createdAt: Instant,
)

enum class NoteType { SCENE, SUMMARY, THOUGHT, QUESTION }

data class NoteComment(
    val id: String,
    val noteId: String,
    val text: String,
    val createdAt: Instant,
)
