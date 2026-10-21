package dev.gaens.filmory.feature.title.domain

import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Error
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.feature.episode.domain.Episode
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.episode.domain.Season
import kotlin.time.Clock
import kotlin.time.Instant

/** What the user typed into the add or edit title form. */
data class TitleForm(
    val type: TitleType,
    val name: String,
    val year: Int?,
    val genre: String?,
    val creator: String?,
    val runtimeMinutes: Int?,
    val overview: String?,
    /** Series only: the number of episodes in each season, season 1 first. */
    val episodesPerSeason: List<Int>,
)

enum class TitleFormError { BLANK_NAME, INVALID_YEAR, INVALID_RUNTIME, NO_SEASONS, INVALID_EPISODE_COUNT }

sealed interface SaveTitleFormError : Error {
    data class Invalid(val errors: Set<TitleFormError>) : SaveTitleFormError
    data class Storage(val error: DataError.Local) : SaveTitleFormError
}

/**
 * Creates a title by hand (works offline) or edits one. Seasons and episodes that already exist keep
 * their ids, so check marks and episode notes survive changing the counts.
 */
class SaveTitleForm(
    private val titleRepository: TitleRepository,
    private val episodeRepository: EpisodeRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock = Clock.System,
) {
    fun validate(form: TitleForm): Set<TitleFormError> = buildSet {
        if (form.name.isBlank()) add(TitleFormError.BLANK_NAME)
        if (form.year != null && form.year !in YEAR_RANGE) add(TitleFormError.INVALID_YEAR)
        if (form.runtimeMinutes != null && form.runtimeMinutes !in RUNTIME_RANGE) add(TitleFormError.INVALID_RUNTIME)
        if (form.type == TitleType.SERIES) {
            if (form.episodesPerSeason.isEmpty() || form.episodesPerSeason.size > MAX_SEASONS) add(TitleFormError.NO_SEASONS)
            if (form.episodesPerSeason.any { it !in EPISODE_RANGE }) add(TitleFormError.INVALID_EPISODE_COUNT)
        }
    }

    suspend operator fun invoke(form: TitleForm, existingId: String?): Result<String, SaveTitleFormError> {
        val errors = validate(form)
        if (errors.isNotEmpty()) return Result.Error(SaveTitleFormError.Invalid(errors))

        val now = clock.now()
        val existing = existingId?.let { titleRepository.getTitle(it) }
        val title = (existing ?: newTitle(form.type, now)).copy(
            name = form.name.trim(),
            year = form.year,
            genre = form.genre?.trim()?.ifEmpty { null },
            creator = form.creator?.trim()?.ifEmpty { null },
            runtimeMinutes = form.runtimeMinutes.takeIf { form.type == TitleType.MOVIE },
            overview = form.overview?.trim()?.ifEmpty { null },
            updatedAt = now,
        )
        val saved = titleRepository.upsertTitle(title)
        if (saved is Result.Error) return Result.Error(SaveTitleFormError.Storage(saved.error))

        if (title.type == TitleType.SERIES) {
            val seasons = mergeSeasons(title.id, episodeRepository.getSeasons(title.id), form.episodesPerSeason)
            val savedSeasons = episodeRepository.replaceSeasons(title.id, seasons)
            if (savedSeasons is Result.Error) return Result.Error(SaveTitleFormError.Storage(savedSeasons.error))
        }
        return Result.Success(title.id)
    }

    private fun newTitle(type: TitleType, now: Instant) = Title(
        id = idGenerator.newId(),
        type = type,
        name = "",
        year = null,
        genre = null,
        runtimeMinutes = null,
        creator = null,
        overview = null,
        posterUrl = null,
        tmdbId = null,
        status = TitleStatus.NOT_WATCHED,
        isWishlisted = false,
        watchLaterPosition = null,
        addedAt = now,
        updatedAt = now,
    )

    private fun mergeSeasons(titleId: String, current: List<Season>, episodesPerSeason: List<Int>): List<Season> =
        episodesPerSeason.mapIndexed { index, episodeCount ->
            val number = index + 1
            val existing = current.find { it.number == number }
            val seasonId = existing?.id ?: idGenerator.newId()
            Season(
                id = seasonId,
                titleId = titleId,
                number = number,
                episodes = (1..episodeCount).map { episodeNumber ->
                    existing?.episodes?.find { it.number == episodeNumber }
                        ?: Episode(idGenerator.newId(), seasonId, episodeNumber, null, null)
                },
            )
        }

    private companion object {
        val YEAR_RANGE = 1870..2100
        val RUNTIME_RANGE = 1..2000
        val EPISODE_RANGE = 1..999
        const val MAX_SEASONS = 100
    }
}
