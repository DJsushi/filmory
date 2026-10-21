package dev.gaens.filmory.feature.note.data

import dev.gaens.filmory.core.data.safeDatabaseWrite
import dev.gaens.filmory.core.database.dao.NoteDao
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.domain.NoteComment
import dev.gaens.filmory.feature.note.domain.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstNoteRepository(
    private val noteDao: NoteDao,
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeNotes().map { rows -> rows.map { it.toNote() } }

    override fun observeNotes(titleId: String): Flow<List<Note>> =
        noteDao.observeNotes(titleId).map { rows -> rows.map { it.toNote() } }

    override fun observeNote(id: String): Flow<Note?> = noteDao.observeNote(id).map { it?.toNote() }

    override suspend fun upsertNote(note: Note): EmptyResult<DataError.Local> =
        safeDatabaseWrite { noteDao.upsertNote(note.toNoteEntity()) }

    override suspend fun deleteNote(id: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { noteDao.deleteNote(id) }

    override fun observeComments(noteId: String): Flow<List<NoteComment>> =
        noteDao.observeComments(noteId).map { list -> list.map { it.toNoteComment() } }

    override suspend fun addComment(comment: NoteComment): EmptyResult<DataError.Local> =
        safeDatabaseWrite { noteDao.upsertComment(comment.toNoteCommentEntity()) }

    override suspend fun deleteComment(id: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { noteDao.deleteComment(id) }
}
