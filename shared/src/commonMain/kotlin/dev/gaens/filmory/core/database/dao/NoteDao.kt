package dev.gaens.filmory.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.gaens.filmory.core.database.entity.NoteCommentEntity
import dev.gaens.filmory.core.database.entity.NoteEntity
import dev.gaens.filmory.core.database.entity.NoteRow
import kotlinx.coroutines.flow.Flow

private const val NOTE_ROWS =
    "SELECT notes.*, watchings.titleId AS titleId, " +
        "(SELECT COUNT(*) FROM note_comments WHERE note_comments.noteId = notes.id) AS commentCount " +
        "FROM notes JOIN watchings ON watchings.id = notes.watchingId"

@Dao
interface NoteDao {
    @Query("$NOTE_ROWS ORDER BY notes.createdAtEpochMillis DESC")
    fun observeNotes(): Flow<List<NoteRow>>

    @Query("$NOTE_ROWS WHERE watchings.titleId = :titleId ORDER BY notes.createdAtEpochMillis DESC")
    fun observeNotes(titleId: String): Flow<List<NoteRow>>

    @Query("$NOTE_ROWS WHERE notes.id = :id")
    fun observeNote(id: String): Flow<NoteRow?>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("SELECT * FROM note_comments WHERE noteId = :noteId ORDER BY createdAtEpochMillis")
    fun observeComments(noteId: String): Flow<List<NoteCommentEntity>>

    @Upsert
    suspend fun upsertComment(comment: NoteCommentEntity)

    @Query("DELETE FROM note_comments WHERE id = :id")
    suspend fun deleteComment(id: String)
}
