package dev.gaens.filmory.feature.collection.data

import dev.gaens.filmory.core.data.safeDatabaseWrite
import dev.gaens.filmory.core.database.dao.CollectionDao
import dev.gaens.filmory.core.database.entity.CollectionTitleEntity
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class OfflineFirstCollectionRepository(
    private val collectionDao: CollectionDao,
) : CollectionRepository {

    override fun observeCollections(): Flow<List<TitleCollection>> =
        combine(collectionDao.observeCollections(), collectionDao.observeCollectionTitles(), ::toCollections)

    /** Saves the name only. Membership changes go through [addTitle] and [removeTitle]. */
    override suspend fun upsertCollection(collection: TitleCollection): EmptyResult<DataError.Local> =
        safeDatabaseWrite { collectionDao.upsertCollection(collection.toCollectionEntity()) }

    override suspend fun deleteCollection(id: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { collectionDao.deleteCollection(id) }

    override suspend fun addTitle(collectionId: String, titleId: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { collectionDao.addTitle(CollectionTitleEntity(collectionId, titleId)) }

    override suspend fun removeTitle(collectionId: String, titleId: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { collectionDao.removeTitle(collectionId, titleId) }
}
