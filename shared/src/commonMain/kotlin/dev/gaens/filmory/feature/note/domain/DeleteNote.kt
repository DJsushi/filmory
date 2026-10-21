package dev.gaens.filmory.feature.note.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import kotlinx.coroutines.flow.first

/** Deletes a note with its comments and its image file. */
class DeleteNote(
    private val noteRepository: NoteRepository,
    private val imageStorage: NoteImageStorage,
) {
    suspend operator fun invoke(noteId: String): EmptyResult<DataError.Local> {
        val note = noteRepository.observeNote(noteId).first() ?: return Result.Error(DataError.Local.NOT_FOUND)
        val deleted = noteRepository.deleteNote(noteId)
        if (deleted is Result.Success) note.imagePath?.let { imageStorage.delete(it) }
        return deleted
    }
}
