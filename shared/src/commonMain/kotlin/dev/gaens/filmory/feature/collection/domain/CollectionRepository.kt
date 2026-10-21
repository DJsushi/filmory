package dev.gaens.filmory.feature.collection.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun observeCollections(): Flow<List<TitleCollection>>
    suspend fun upsertCollection(collection: TitleCollection): EmptyResult<DataError.Local>
    suspend fun deleteCollection(id: String): EmptyResult<DataError.Local>

    /** Puts the collections in [orderedIds] in that order, after pinned ones are placed first. */
    suspend fun reorder(orderedIds: List<String>): EmptyResult<DataError.Local>
    suspend fun addTitle(collectionId: String, titleId: String): EmptyResult<DataError.Local>
    suspend fun removeTitle(collectionId: String, titleId: String): EmptyResult<DataError.Local>
}
