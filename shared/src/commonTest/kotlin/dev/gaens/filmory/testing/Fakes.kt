package dev.gaens.filmory.testing

import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.domain.ImageResizer
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.collection.domain.inDisplayOrder
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.settings.domain.AppSettings
import dev.gaens.filmory.feature.settings.domain.SettingsRepository
import dev.gaens.filmory.feature.note.domain.NoteComment
import dev.gaens.filmory.feature.note.domain.NoteImageStorage
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private val Saved: EmptyResult<DataError.Local> = Result.Success(Unit)

class FakeTitleRepository(titles: List<Title> = emptyList()) : TitleRepository {
    val titles = MutableStateFlow(titles)

    override fun observeTitles(): Flow<List<Title>> = titles
    override fun observeTitle(id: String): Flow<Title?> = titles.map { list -> list.find { it.id == id } }
    override suspend fun getTitle(id: String): Title? = titles.value.find { it.id == id }
    override suspend fun getTitleByTmdbId(tmdbId: Long, type: TitleType): Title? =
        titles.value.find { it.tmdbId == tmdbId && it.type == type }
    override suspend fun upsertTitle(title: Title): EmptyResult<DataError.Local> {
        titles.update { list -> list.filterNot { it.id == title.id } + title }
        return Saved
    }
    override suspend fun deleteTitle(id: String): EmptyResult<DataError.Local> {
        titles.update { list -> list.filterNot { it.id == id } }
        return Saved
    }
}

class FakeWatchingRepository(watchings: List<Watching> = emptyList()) : WatchingRepository {
    val watchings = MutableStateFlow(watchings)

    override fun observeWatchings(titleId: String): Flow<List<Watching>> =
        watchings.map { list -> list.filter { it.titleId == titleId } }
    override fun observeAllWatchings(): Flow<List<Watching>> = watchings
    override suspend fun getWatching(id: String): Watching? = watchings.value.find { it.id == id }
    override suspend fun getWatchings(titleId: String): List<Watching> =
        watchings.value.filter { it.titleId == titleId }
    override suspend fun upsertWatching(watching: Watching): EmptyResult<DataError.Local> {
        watchings.update { list -> list.filterNot { it.id == watching.id } + watching }
        return Saved
    }
    override suspend fun deleteWatching(id: String): EmptyResult<DataError.Local> {
        watchings.update { list -> list.filterNot { it.id == id } }
        return Saved
    }
}

class FakeEpisodeRepository(seasons: List<Season> = emptyList()) : EpisodeRepository {
    val seasons = MutableStateFlow(seasons)
    val checks = MutableStateFlow<List<EpisodeCheck>>(emptyList())

    override fun observeSeasons(titleId: String): Flow<List<Season>> =
        seasons.map { list -> list.filter { it.titleId == titleId } }
    override suspend fun getSeasons(titleId: String): List<Season> =
        seasons.value.filter { it.titleId == titleId }
    override suspend fun replaceSeasons(titleId: String, seasons: List<Season>): EmptyResult<DataError.Local> {
        this.seasons.update { list -> list.filterNot { it.titleId == titleId } + seasons }
        return Saved
    }
    override fun observeAllSeasons(): Flow<List<Season>> = seasons
    override fun observeChecks(watchingId: String): Flow<List<EpisodeCheck>> =
        checks.map { list -> list.filter { it.watchingId == watchingId } }
    override fun observeAllChecks(): Flow<List<EpisodeCheck>> = checks
    override suspend fun checkEpisode(check: EpisodeCheck): EmptyResult<DataError.Local> {
        checks.update { list ->
            if (list.any { it.watchingId == check.watchingId && it.episodeId == check.episodeId }) list else list + check
        }
        return Saved
    }
    override suspend fun uncheckEpisode(watchingId: String, episodeId: String): EmptyResult<DataError.Local> {
        checks.update { list -> list.filterNot { it.watchingId == watchingId && it.episodeId == episodeId } }
        return Saved
    }
}

class FakeNoteRepository(notes: List<Note> = emptyList()) : NoteRepository {
    val notes = MutableStateFlow(notes)
    val comments = MutableStateFlow<List<NoteComment>>(emptyList())

    override fun observeNotes(): Flow<List<Note>> = notes
    override fun observeNotes(titleId: String): Flow<List<Note>> =
        notes.map { list -> list.filter { it.titleId == titleId } }
    override fun observeNote(id: String): Flow<Note?> = notes.map { list -> list.find { it.id == id } }
    override suspend fun upsertNote(note: Note): EmptyResult<DataError.Local> {
        notes.update { list -> list.filterNot { it.id == note.id } + note }
        return Saved
    }
    override suspend fun deleteNote(id: String): EmptyResult<DataError.Local> {
        notes.update { list -> list.filterNot { it.id == id } }
        return Saved
    }
    override fun observeComments(noteId: String): Flow<List<NoteComment>> =
        comments.map { list -> list.filter { it.noteId == noteId } }
    override suspend fun addComment(comment: NoteComment): EmptyResult<DataError.Local> {
        comments.update { it + comment }
        return Saved
    }
    override suspend fun deleteComment(id: String): EmptyResult<DataError.Local> {
        comments.update { list -> list.filterNot { it.id == id } }
        return Saved
    }
}

class FakeCollectionRepository(collections: List<TitleCollection> = emptyList()) :
    CollectionRepository {
    val collections = MutableStateFlow(collections)

    override fun observeCollections(): Flow<List<TitleCollection>> = collections.map { it.inDisplayOrder() }
    override suspend fun reorder(orderedIds: List<String>): EmptyResult<DataError.Local> {
        collections.update { list ->
            list.map { collection -> orderedIds.indexOf(collection.id).let { if (it == -1) collection else collection.copy(sortOrder = it + 1) } }
        }
        return Saved
    }
    override suspend fun upsertCollection(
        collection: TitleCollection,
    ): EmptyResult<DataError.Local> {
        collections.update { list ->
            val existing = list.find { it.id == collection.id }
            list.filterNot { it.id == collection.id } + collection.copy(titleIds = existing?.titleIds.orEmpty())
        }
        return Saved
    }
    override suspend fun deleteCollection(id: String): EmptyResult<DataError.Local> {
        collections.update { list -> list.filterNot { it.id == id } }
        return Saved
    }
    override suspend fun addTitle(collectionId: String, titleId: String): EmptyResult<DataError.Local> {
        collections.update { list ->
            list.map { if (it.id == collectionId && titleId !in it.titleIds) it.copy(titleIds = it.titleIds + titleId) else it }
        }
        return Saved
    }
    override suspend fun removeTitle(collectionId: String, titleId: String): EmptyResult<DataError.Local> {
        collections.update { list ->
            list.map { if (it.id == collectionId) it.copy(titleIds = it.titleIds - titleId) else it }
        }
        return Saved
    }
}

class FakeSettingsRepository(settings: AppSettings = AppSettings()) : SettingsRepository {
    val settings = MutableStateFlow(settings)

    override fun observeSettings(): Flow<AppSettings> = settings
    override suspend fun update(transform: (AppSettings) -> AppSettings) = settings.update(transform)
}

class FakeNoteImageStorage : NoteImageStorage {
    val files = mutableMapOf<String, ByteArray>()
    private var saved = 0

    override suspend fun save(bytes: ByteArray): Result<String, DataError.Local> {
        val path = "note_images/${++saved}.jpg"
        files[path] = bytes
        return Result.Success(path)
    }
    override suspend fun delete(relativePath: String) {
        files.remove(relativePath)
    }
    override fun resolve(relativePath: String): String = "/files/$relativePath"
}

class FakeImageResizer : ImageResizer {
    var result: ByteArray? = byteArrayOf(1, 2, 3)
    override suspend fun centerCropSquare(bytes: ByteArray, sizePx: Int): ByteArray? = result
}
