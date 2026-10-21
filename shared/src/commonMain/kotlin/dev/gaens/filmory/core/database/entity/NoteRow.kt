package dev.gaens.filmory.core.database.entity

import androidx.room.Embedded

/** A note joined with the title of its watching and its comment count. */
data class NoteRow(
    @Embedded val note: NoteEntity,
    val titleId: String,
    val commentCount: Int,
)
