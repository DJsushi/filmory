package dev.gaens.filmory.feature.title.data

import dev.gaens.filmory.core.database.entity.EpisodeEntity
import dev.gaens.filmory.core.database.entity.NoteEntity
import dev.gaens.filmory.core.database.entity.NoteRow
import dev.gaens.filmory.core.database.entity.SeasonEntity
import dev.gaens.filmory.feature.episode.data.toSeasons
import dev.gaens.filmory.feature.note.data.toNote
import dev.gaens.filmory.feature.note.data.toNoteEntity
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.data.toWatching
import dev.gaens.filmory.feature.watching.data.toWatchingEntity
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.testing.aNote
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.at
import dev.gaens.filmory.testing.aWatching
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersTest {

    @Test
    fun `title survives a round trip through its entity`() {
        val title = aTitle(type = TitleType.MOVIE, status = TitleStatus.ON_HOLD)
            .copy(isWishlisted = true, watchLaterPosition = 2, tmdbId = 603, genre = "Sci-fi", runtimeMinutes = 116)

        assertEquals(title, title.toTitleEntity().toTitle())
    }

    @Test
    fun `watching survives a round trip including rating and dates`() {
        val watching = aWatching(currentEpisodeId = "e1", startedAt = at(10), finishedAt = at(20))
            .copy(rating = Rating(7), review = "good")

        assertEquals(watching, watching.toWatchingEntity().toWatching())
    }

    @Test
    fun `note row maps title id and comment count`() {
        val note = aNote().copy(commentCount = 3)
        val row = NoteRow(note.toNoteEntity(), titleId = "title", commentCount = 3)

        assertEquals(note, row.toNote())
    }

    @Test
    fun `seasons group their episodes in order`() {
        val seasons = toSeasons(
            seasons = listOf(SeasonEntity("s1", "t", 1), SeasonEntity("s2", "t", 2)),
            episodes = listOf(
                EpisodeEntity("e1", "s1", 1, "Pilot", 48),
                EpisodeEntity("e2", "s1", 2, null, null),
                EpisodeEntity("e3", "s2", 1, null, null),
            ),
        )

        assertEquals(listOf("e1", "e2"), seasons[0].episodes.map { it.id })
        assertEquals(listOf("e3"), seasons[1].episodes.map { it.id })
        assertEquals("Pilot", seasons[0].episodes[0].name)
    }

    @Test
    fun `note entity keeps only stored fields`() {
        val entity: NoteEntity = aNote().toNoteEntity()

        assertEquals("watching", entity.watchingId)
        assertEquals(5L, entity.createdAtEpochMillis)
    }
}
