package dev.gaens.filmory.feature.title.domain

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface TitleRepository {
    fun observeTitles(): Flow<List<Title>>
    fun observeTitle(id: String): Flow<Title?>
    suspend fun getTitle(id: String): Title?
    suspend fun upsertTitle(title: Title): EmptyResult<DataError.Local>
    suspend fun deleteTitle(id: String): EmptyResult<DataError.Local>
}
