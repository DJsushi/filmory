package dev.gaens.filmory.feature.note.data

import dev.gaens.filmory.core.data.toEpochMillis
import dev.gaens.filmory.core.data.toInstant
import dev.gaens.filmory.core.database.entity.NoteCommentEntity
import dev.gaens.filmory.core.database.entity.NoteEntity
import dev.gaens.filmory.core.database.entity.NoteRow
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.domain.NoteComment
import dev.gaens.filmory.feature.note.domain.NoteType

fun NoteRow.toNote(): Note = Note(
    id = note.id,
    watchingId = note.watchingId,
    titleId = titleId,
    episodeId = note.episodeId,
    type = NoteType.valueOf(note.type),
    text = note.text,
    timestampMillis = note.timestampMillis,
    imagePath = note.imagePath,
    isFavorite = note.isFavorite,
    commentCount = commentCount,
    createdAt = note.createdAtEpochMillis.toInstant(),
)

fun Note.toNoteEntity(): NoteEntity = NoteEntity(
    id = id,
    watchingId = watchingId,
    episodeId = episodeId,
    type = type.name,
    text = text,
    timestampMillis = timestampMillis,
    imagePath = imagePath,
    isFavorite = isFavorite,
    createdAtEpochMillis = createdAt.toEpochMillis(),
)

fun NoteCommentEntity.toNoteComment(): NoteComment = NoteComment(
    id = id,
    noteId = noteId,
    text = text,
    createdAt = createdAtEpochMillis.toInstant(),
)

fun NoteComment.toNoteCommentEntity(): NoteCommentEntity = NoteCommentEntity(
    id = id,
    noteId = noteId,
    text = text,
    createdAtEpochMillis = createdAt.toEpochMillis(),
)
