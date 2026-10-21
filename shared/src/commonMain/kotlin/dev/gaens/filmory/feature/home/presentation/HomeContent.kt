package dev.gaens.filmory.feature.home.presentation

import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.collection.presentation.toLook
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.episode.domain.Season
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.watchLaterQueue
import dev.gaens.filmory.feature.watching.domain.ContinueWatching

/** Everything Home shows that comes from the database. */
data class HomeData(
    val titles: List<Title>,
    val continueWatching: List<ContinueWatching>,
    val seasons: List<Season>,
    val checks: List<EpisodeCheck>,
    val collections: List<TitleCollection>,
)

fun HomeState.withData(data: HomeData, resolvePhoto: (relativePath: String) -> String): HomeState {
    val existingIds = data.titles.mapTo(HashSet()) { it.id }
    return copy(
        continueWatching = data.continueWatching.map { it.toUi(data.seasons, data.checks) },
        watchLater = data.titles.watchLaterQueue().map { it.toThumbUi() },
        wishlistCount = data.titles.count { it.isWishlisted },
        collections = data.collections.map { collection ->
            CollectionUi(
                id = collection.id,
                name = collection.name,
                titleCount = collection.titleIds.count { it in existingIds },
                look = collection.toLook(resolvePhoto),
            )
        },
        onHoldCount = data.titles.count { it.status == TitleStatus.ON_HOLD },
        droppedCount = data.titles.count { it.status == TitleStatus.DROPPED },
        libraryCount = data.titles.size,
    )
}

fun Title.toThumbUi() = TitleThumbUi(id = id, name = name, posterUrl = posterUrl)

private fun ContinueWatching.toUi(seasons: List<Season>, checks: List<EpisodeCheck>): ContinueWatchingUi {
    val titleSeasons = seasons.filter { it.titleId == title.id }
    val episodeCount = titleSeasons.sumOf { it.episodes.size }
    val currentSeason = titleSeasons.find { season -> season.episodes.any { it.id == watching.currentEpisodeId } }
    val currentEpisode = currentSeason?.episodes?.find { it.id == watching.currentEpisodeId }
    val checkedCount = checks.count { it.watchingId == watching.id }
    return ContinueWatchingUi(
        titleId = title.id,
        name = title.name,
        posterUrl = title.posterUrl,
        type = title.type,
        seasonNumber = currentSeason?.number,
        episodeNumber = currentEpisode?.number,
        progress = if (episodeCount > 0) checkedCount.toFloat() / episodeCount else null,
    )
}
