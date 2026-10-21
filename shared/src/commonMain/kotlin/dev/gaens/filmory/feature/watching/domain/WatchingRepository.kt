package dev.gaens.filmory.feature.watching.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface WatchingRepository {
    fun observeWatchings(titleId: String): Flow<List<Watching>>
    fun observeAllWatchings(): Flow<List<Watching>>
    suspend fun getWatching(id: String): Watching?
    suspend fun getWatchings(titleId: String): List<Watching>
    suspend fun upsertWatching(watching: Watching): EmptyResult<DataError.Local>
    suspend fun deleteWatching(id: String): EmptyResult<DataError.Local>
}
