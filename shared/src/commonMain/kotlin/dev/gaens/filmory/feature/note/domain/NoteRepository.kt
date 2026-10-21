package dev.gaens.filmory.feature.note.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    fun observeNotes(titleId: String): Flow<List<Note>>
    fun observeNote(id: String): Flow<Note?>
    suspend fun upsertNote(note: Note): EmptyResult<DataError.Local>
    suspend fun deleteNote(id: String): EmptyResult<DataError.Local>

    fun observeComments(noteId: String): Flow<List<NoteComment>>
    suspend fun addComment(comment: NoteComment): EmptyResult<DataError.Local>
    suspend fun deleteComment(id: String): EmptyResult<DataError.Local>
}
