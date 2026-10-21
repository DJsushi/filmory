package dev.gaens.filmory.feature.watching.data

import dev.gaens.filmory.core.data.safeDatabaseWrite
import dev.gaens.filmory.core.database.dao.WatchingDao
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstWatchingRepository(
    private val watchingDao: WatchingDao,
) : WatchingRepository {

    override fun observeWatchings(titleId: String): Flow<List<Watching>> =
        watchingDao.observeWatchings(titleId).map { list -> list.map { it.toWatching() } }

    override fun observeAllWatchings(): Flow<List<Watching>> =
        watchingDao.observeAllWatchings().map { list -> list.map { it.toWatching() } }

    override suspend fun getWatching(id: String): Watching? = watchingDao.getWatching(id)?.toWatching()

    override suspend fun getWatchings(titleId: String): List<Watching> =
        watchingDao.getWatchings(titleId).map { it.toWatching() }

    override suspend fun upsertWatching(watching: Watching): EmptyResult<DataError.Local> =
        safeDatabaseWrite { watchingDao.upsertWatching(watching.toWatchingEntity()) }

    override suspend fun deleteWatching(id: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { watchingDao.deleteWatching(id) }
}
