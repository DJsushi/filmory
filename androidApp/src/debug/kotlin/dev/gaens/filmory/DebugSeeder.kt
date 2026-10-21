package dev.gaens.filmory

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.episode.domain.AdvanceEpisode
import dev.gaens.filmory.feature.episode.domain.Episode
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.note.domain.AddNote
import dev.gaens.filmory.feature.note.domain.NoteComment
import dev.gaens.filmory.feature.note.domain.NoteDraft
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.title.domain.ToggleWatchLater
import dev.gaens.filmory.feature.watching.domain.ChangeWatchingState
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.StartWatching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.core.domain.util.Result
import kotlinx.coroutines.flow.first
import org.koin.core.Koin
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class DebugSeeder(koin: Koin) {
    private val titles: TitleRepository = koin.get()
    private val watchings: WatchingRepository = koin.get()
    private val episodes: EpisodeRepository = koin.get()
    private val notes: NoteRepository = koin.get()
    private val collections: CollectionRepository = koin.get()
    private val startWatching: StartWatching = koin.get()
    private val changeState: ChangeWatchingState = koin.get()
    private val advanceEpisode: AdvanceEpisode = koin.get()
    private val addNote: AddNote = koin.get()
    private val toggleWatchLater: ToggleWatchLater = koin.get()
    private val ids: IdGenerator = koin.get()
    private val now = Clock.System.now()

    suspend fun seed() {
        if (titles.observeTitles().first().isNotEmpty()) return

        val severance = series("Severance", 2022, "Thriller", "Dan Erickson", listOf(9, 10), daysAgo = 40)
        val bear = series("The Bear", 2022, "Comedy", "Christopher Storer", listOf(8, 10), daysAgo = 30)
        val andor = series("Andor", 2022, "Sci-fi", "Tony Gilroy", listOf(12), daysAgo = 60)
        val dark = series("Dark", 2017, "Mystery", "Baran bo Odar", listOf(10, 8, 8), daysAgo = 90)
        val succession = series("Succession", 2018, "Drama", "Jesse Armstrong", listOf(10), daysAgo = 120)
        val arrival = movie("Arrival", 2016, "Sci-fi", 116, "Denis Villeneuve", daysAgo = 200)
        val dune = movie("Dune: Part Two", 2024, "Sci-fi", 166, "Denis Villeneuve", daysAgo = 50)
        val pastLives = movie("Past Lives", 2023, "Drama", 106, "Celine Song", daysAgo = 70)
        val bladeRunner = movie("Blade Runner 2049", 2017, "Sci-fi", 164, "Denis Villeneuve", daysAgo = 15)
        val parasite = movie("Parasite", 2019, "Thriller", 132, "Bong Joon-ho", daysAgo = 5)
        val oppenheimer = movie("Oppenheimer", 2023, "Drama", 180, "Christopher Nolan", daysAgo = 3)

        // Severance: in progress, a few episodes in.
        val severanceWatching = start(severance)
        repeat(13) { advanceEpisode(severanceWatching) }
        rate(severanceWatching, 9, "Rewatching with a friend. Pausing after each episode to talk.")

        val bearWatching = start(bear)
        repeat(5) { advanceEpisode(bearWatching) }

        val andorWatching = start(andor)
        repeat(4) { advanceEpisode(andorWatching) }
        changeState(andorWatching, WatchingState.ON_HOLD)

        val darkWatching = start(dark)
        repeat(3) { advanceEpisode(darkWatching) }
        changeState(darkWatching, WatchingState.DROPPED)

        finish(start(succession), 10)

        // Arrival: watched twice.
        val arrivalFirst = start(arrival)
        finish(arrivalFirst, 8, "Quiet and strange. Loved the sound design.")
        val arrivalSecond = start(arrival)
        finish(arrivalSecond, 9, "Hit harder the second time. The ending reframes everything.")
        finish(start(dune), 8)
        finish(start(pastLives), 10)
        finish(start(bladeRunner), 9)
        finish(start(parasite), 10)

        titles.upsertTitle(requireNotNull(titles.getTitle(oppenheimer)).copy(isWishlisted = true))
        titles.upsertTitle(requireNotNull(titles.getTitle(pastLives)).copy(isWishlisted = true))
        listOf(arrival, pastLives, andor).forEach { toggleWatchLater(it) }

        note(arrival, arrivalSecond, NoteType.SCENE, 1_450_000, "The first time the heptapod ink ring appears. The whole theater held its breath.", 2)
        note(arrival, arrivalSecond, NoteType.THOUGHT, 5_505_000, "Knowing the twist makes every scene with the daughter unbearable in a good way.", 0)
        note(arrival, arrivalFirst, NoteType.SUMMARY, null, "Language shapes perception of time. Louise chooses her future knowingly.", 1)
        note(severance, severanceWatching, NoteType.SCENE, 1_450_000, "The elevator reveal. I rewound it twice.", 1)
        note(pastLives, null, NoteType.QUESTION, null, "Would Nora have said the same thing if the subway ride had lasted another minute?", 0)

        val favorites = TitleCollection.DEFAULT_COLLECTION_ID
        listOf(arrival, pastLives, parasite, succession).forEach { collections.addTitle(favorites, it) }
        val rainyDay = collection("Rainy day", listOf(dune, bladeRunner))
        val slowBurners = collection("Slow burners", listOf(arrival, andor, dark))
        check(rainyDay.isNotEmpty() && slowBurners.isNotEmpty())
    }

    private suspend fun movie(name: String, year: Int, genre: String, runtime: Int, creator: String, daysAgo: Int): String =
        title(TitleType.MOVIE, name, year, genre, runtime, creator, daysAgo)

    private suspend fun series(
        name: String,
        year: Int,
        genre: String,
        creator: String,
        episodesPerSeason: List<Int>,
        daysAgo: Int,
    ): String {
        val id = title(TitleType.SERIES, name, year, genre, null, creator, daysAgo)
        val seasons = episodesPerSeason.mapIndexed { index, count ->
            val seasonId = ids.newId()
            Season(
                id = seasonId,
                titleId = id,
                number = index + 1,
                episodes = (1..count).map { number -> Episode(ids.newId(), seasonId, number, "Episode $number", 48) },
            )
        }
        episodes.replaceSeasons(id, seasons)
        return id
    }

    private suspend fun title(
        type: TitleType,
        name: String,
        year: Int,
        genre: String,
        runtime: Int?,
        creator: String,
        daysAgo: Int,
    ): String {
        val added = now - daysAgo.days
        val title = Title(
            id = ids.newId(),
            type = type,
            name = name,
            year = year,
            genre = genre,
            runtimeMinutes = runtime,
            creator = creator,
            overview = "A sample description for $name, written for the debug seed data.",
            posterUrl = null,
            tmdbId = null,
            status = TitleStatus.NOT_WATCHED,
            isWishlisted = false,
            watchLaterPosition = null,
            addedAt = added,
            updatedAt = added,
        )
        titles.upsertTitle(title)
        return title.id
    }

    private suspend fun start(titleId: String): String =
        (startWatching(titleId) as Result.Success).data.id

    private suspend fun finish(watchingId: String, halfStars: Int, review: String? = null) {
        changeState(watchingId, WatchingState.FINISHED)
        rate(watchingId, halfStars, review)
    }

    private suspend fun rate(watchingId: String, halfStars: Int, review: String?) {
        val watching = watchings.getWatching(watchingId) ?: return
        watchings.upsertWatching(watching.copy(rating = Rating(halfStars), review = review))
    }

    private suspend fun note(
        titleId: String,
        watchingId: String?,
        type: NoteType,
        timestamp: Long?,
        text: String,
        commentCount: Int,
    ) {
        val draft = NoteDraft(titleId, watchingId, null, type, text, timestamp, null, now - (commentCount + 1).hours)
        val note = (addNote(draft) as Result.Success).data
        repeat(commentCount) { index ->
            notes.addComment(NoteComment(ids.newId(), note.id, "Comment ${index + 1} on this note.", now - index.hours))
        }
    }

    private suspend fun collection(name: String, titleIds: List<String>): String {
        val id = ids.newId()
        collections.upsertCollection(TitleCollection(id, name, emptyList(), now))
        titleIds.forEach { collections.addTitle(id, it) }
        return id
    }
}
