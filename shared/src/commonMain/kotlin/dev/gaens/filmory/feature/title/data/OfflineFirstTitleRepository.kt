package dev.gaens.filmory.feature.title.data

import dev.gaens.filmory.core.data.safeDatabaseWrite
import dev.gaens.filmory.core.database.dao.TitleDao
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstTitleRepository(
    private val titleDao: TitleDao,
) : TitleRepository {

    override fun observeTitles(): Flow<List<Title>> =
        titleDao.observeTitles().map { titles -> titles.map { it.toTitle() } }

    override fun observeTitle(id: String): Flow<Title?> =
        titleDao.observeTitle(id).map { it?.toTitle() }

    override suspend fun getTitle(id: String): Title? = titleDao.getTitle(id)?.toTitle()

    override suspend fun upsertTitle(title: Title): EmptyResult<DataError.Local> =
        safeDatabaseWrite { titleDao.upsertTitle(title.toTitleEntity()) }

    override suspend fun deleteTitle(id: String): EmptyResult<DataError.Local> =
        safeDatabaseWrite { titleDao.deleteTitle(id) }
}
