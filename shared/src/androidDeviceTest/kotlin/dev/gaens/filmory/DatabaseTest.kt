package dev.gaens.filmory

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import dev.gaens.filmory.core.data.AppDirectories
import dev.gaens.filmory.core.database.AppDatabase
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.backup.data.ZipBackupRepository
import dev.gaens.filmory.feature.backup.domain.BackupError
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.time.Clock
import dev.gaens.filmory.feature.collection.data.CreateDefaultCollectionCallback
import dev.gaens.filmory.feature.collection.data.OfflineFirstCollectionRepository
import dev.gaens.filmory.feature.episode.data.OfflineFirstEpisodeRepository
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.note.data.OfflineFirstNoteRepository
import dev.gaens.filmory.feature.note.domain.NoteComment
import dev.gaens.filmory.feature.title.data.OfflineFirstTitleRepository
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.data.OfflineFirstWatchingRepository
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.feature.episode.domain.Episode
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.note.domain.NoteType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

class DatabaseTest {
    private lateinit var database: AppDatabase
    private lateinit var titles: OfflineFirstTitleRepository
    private lateinit var watchings: OfflineFirstWatchingRepository
    private lateinit var episodes: OfflineFirstEpisodeRepository
    private lateinit var notes: OfflineFirstNoteRepository
    private lateinit var collections: OfflineFirstCollectionRepository

    private val now = Instant.fromEpochMilliseconds(1_000)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder<AppDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .addCallback(CreateDefaultCollectionCallback)
            .build()
        titles = OfflineFirstTitleRepository(database.titleDao())
        watchings = OfflineFirstWatchingRepository(database.watchingDao())
        episodes = OfflineFirstEpisodeRepository(database.episodeDao())
        notes = OfflineFirstNoteRepository(database.noteDao())
        collections = OfflineFirstCollectionRepository(database.collectionDao())
    }

    @After
    fun tearDown() = database.close()

    private val title = Title(
        id = "t", type = TitleType.SERIES, name = "Show", year = 2020, genre = null, runtimeMinutes = null,
        creator = null, overview = null, posterUrl = null,
        tmdbId = null, status = TitleStatus.NOT_WATCHED, isWishlisted = false, watchLaterPosition = null,
        addedAt = now, updatedAt = now,
    )
    private val watching = Watching(
        id = "w", titleId = "t", state = WatchingState.IN_PROGRESS, startedAt = now, finishedAt = null,
        rating = Rating(9), review = null, currentEpisodeId = null, createdAt = now,
    )
    private val seasons = listOf(
        Season("s1", "t", 1, listOf(Episode("e1", "s1", 1, "Pilot", 48), Episode("e2", "s1", 2, null, null))),
    )
    private val note = Note(
        id = "n", watchingId = "w", titleId = "t", episodeId = "e1", type = NoteType.SCENE, text = "x",
        timestampMillis = 5_000, imagePath = null, isFavorite = false, commentCount = 0, createdAt = now,
    )

    private suspend fun seed() {
        titles.upsertTitle(title)
        episodes.replaceSeasons("t", seasons)
        watchings.upsertWatching(watching)
        notes.upsertNote(note)
    }

    @Test
    fun new_database_has_the_default_collection() = runBlocking {
        val collection = collections.observeCollections().first().single()

        assertEquals("favorites", collection.id)
    }

    @Test
    fun seasons_come_back_with_ordered_episodes() = runBlocking {
        seed()

        assertEquals(seasons, episodes.getSeasons("t"))
    }

    @Test
    fun a_note_row_carries_its_title_and_comment_count() = runBlocking {
        seed()
        notes.addComment(NoteComment("c1", "n", "hm", now))
        notes.addComment(NoteComment("c2", "n", "ok", now))

        val read = notes.observeNote("n").first()

        assertEquals("t", read?.titleId)
        assertEquals(2, read?.commentCount)
        assertEquals(2, notes.observeNotes("t").first().single().commentCount)
    }

    @Test
    fun checking_the_same_episode_twice_keeps_one_check() = runBlocking {
        seed()

        episodes.checkEpisode(EpisodeCheck("c1", "w", "e1", now))
        episodes.checkEpisode(EpisodeCheck("c2", "w", "e1", now))

        assertEquals(1, episodes.observeChecks("w").first().size)
    }

    @Test
    fun deleting_a_title_removes_everything_under_it() = runBlocking {
        seed()
        episodes.checkEpisode(EpisodeCheck("c1", "w", "e1", now))
        notes.addComment(NoteComment("c1", "n", "hm", now))
        collections.addTitle("favorites", "t")

        titles.deleteTitle("t")

        assertTrue(watchings.getWatchings("t").isEmpty())
        assertTrue(episodes.getSeasons("t").isEmpty())
        assertTrue(episodes.observeAllChecks().first().isEmpty())
        assertTrue(notes.observeNotes().first().isEmpty())
        assertTrue(notes.observeComments("n").first().isEmpty())
        assertTrue(collections.observeCollections().first().single().titleIds.isEmpty())
    }

    @Test
    fun replacing_seasons_clears_the_pointer_and_checks_of_removed_episodes() = runBlocking {
        seed()
        watchings.upsertWatching(watching.copy(currentEpisodeId = "e1"))
        episodes.checkEpisode(EpisodeCheck("c1", "w", "e1", now))

        episodes.replaceSeasons("t", listOf(Season("s9", "t", 1, listOf(Episode("e9", "s9", 1, null, null)))))

        assertNull(watchings.getWatching("w")?.currentEpisodeId)
        assertTrue(episodes.observeAllChecks().first().isEmpty())
        assertNull(notes.observeNote("n").first()?.episodeId)
    }

    @Test
    fun replacing_seasons_keeps_checks_on_kept_episodes() = runBlocking {
        seed()
        episodes.checkEpisode(EpisodeCheck("c1", "w", "e1", now))

        val grown = Season("s1", "t", 1, seasons.single().episodes + Episode("e3", "s1", 3, null, null))
        episodes.replaceSeasons("t", listOf(grown))

        assertEquals(listOf("e1", "e2", "e3"), episodes.getSeasons("t").single().episodes.map { it.id })
        assertEquals(1, episodes.observeChecks("w").first().size)
        assertEquals("e1", notes.observeNote("n").first()?.episodeId)
    }

    @Test
    fun backup_round_trip_restores_data_and_images_and_rejects_garbage() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val root = java.io.File(context.cacheDir, "backup-test").apply { deleteRecursively(); mkdirs() }.absolutePath.toPath()
        val directories = AppDirectories(root / "files", root / "cache")
        val image = directories.files / "note_images/a.jpg"
        FileSystem.SYSTEM.createDirectories(image.parent!!)
        FileSystem.SYSTEM.write(image) { write(byteArrayOf(7, 8, 9)) }
        val backups = ZipBackupRepository(database.backupDao(), directories, FileSystem.SYSTEM, Clock.System)

        seed()
        notes.upsertNote(note.copy(imagePath = "note_images/a.jpg"))
        episodes.checkEpisode(EpisodeCheck("c1", "w", "e1", now))
        val file = (backups.createBackup() as Result.Success).data

        titles.deleteTitle("t")
        FileSystem.SYSTEM.delete(image)
        assertTrue(titles.observeTitles().first().isEmpty())

        assertEquals(Result.Success(Unit), backups.restore(file))
        assertEquals(title, titles.getTitle("t"))
        assertEquals(seasons, episodes.getSeasons("t"))
        assertEquals(1, episodes.observeChecks("w").first().size)
        assertEquals("note_images/a.jpg", notes.observeNote("n").first()?.imagePath)
        assertEquals(listOf<Byte>(7, 8, 9), FileSystem.SYSTEM.read(image) { readByteArray() }.toList())

        val garbage = (root / "garbage.filmorybkp").also { FileSystem.SYSTEM.write(it) { writeUtf8("not a zip") } }
        assertEquals(Result.Error(BackupError.INVALID_FILE), backups.restore(garbage.toString()))
        assertEquals(title, titles.getTitle("t"))
    }

    @Test
    fun collection_membership_is_added_and_removed() = runBlocking {
        seed()

        collections.addTitle("favorites", "t")
        collections.addTitle("favorites", "t")
        assertEquals(listOf("t"), collections.observeCollections().first().single().titleIds)

        collections.removeTitle("favorites", "t")
        assertTrue(collections.observeCollections().first().single().titleIds.isEmpty())
    }
}
