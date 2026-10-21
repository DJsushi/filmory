package dev.gaens.filmory.feature.title.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
import dev.gaens.filmory.feature.note.domain.NoteRepository
import kotlinx.coroutines.flow.first

/** Deletes a title. The database cascades to its records; note image files are removed here. */
class DeleteTitle(
    private val titleRepository: TitleRepository,
    private val noteRepository: NoteRepository,
    private val imageStorage: NoteImageStorage,
) {
    suspend operator fun invoke(titleId: String): EmptyResult<DataError.Local> {
        val imagePaths = noteRepository.observeNotes(titleId).first().mapNotNull { it.imagePath }
        val deleted = titleRepository.deleteTitle(titleId)
        if (deleted is Result.Success) imagePaths.forEach { imageStorage.delete(it) }
        return deleted
    }
}
