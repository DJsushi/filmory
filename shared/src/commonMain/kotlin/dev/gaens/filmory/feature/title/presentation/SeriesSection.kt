package dev.gaens.filmory.feature.title.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.BadgeColors
import dev.gaens.filmory.core.presentation.designsystem.components.ChipRow
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryBadge
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryCard
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.GrowingProgressBar
import dev.gaens.filmory.core.presentation.designsystem.components.SectionAction
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.popIn
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.detail_currently_on
import dev.gaens.filmory.resources.detail_episode_checked
import dev.gaens.filmory.resources.detail_episode_not_watched
import dev.gaens.filmory.resources.detail_episode_skipped
import dev.gaens.filmory.resources.detail_episodes_checked
import dev.gaens.filmory.resources.detail_episodes_skipped
import dev.gaens.filmory.resources.detail_episodes_watched
import dev.gaens.filmory.resources.detail_finish_and_next
import dev.gaens.filmory.resources.detail_finish_last
import dev.gaens.filmory.resources.detail_mark_all
import dev.gaens.filmory.resources.detail_no_episodes
import dev.gaens.filmory.resources.detail_not_started_yet
import dev.gaens.filmory.resources.detail_runtime_minutes
import dev.gaens.filmory.resources.detail_season_chip
import dev.gaens.filmory.resources.detail_seasons
import dev.gaens.filmory.resources.detail_start_watching
import dev.gaens.filmory.resources.detail_you_are_here
import dev.gaens.filmory.resources.home_season_episode
import dev.gaens.filmory.resources.ic_check
import dev.gaens.filmory.resources.ic_skip
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun LazyListScope.seriesSection(state: TitleDetailState, onAction: (TitleDetailAction) -> Unit) {
    val series = state.series
    if (series == null) {
        item(key = "noEpisodes") {
            Text(
                text = stringResource(Res.string.detail_no_episodes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 20.dp),
            )
        }
        return
    }
    item(key = "progress") { ProgressCard(series, onAction) }
    item(key = "seasonsHeader") {
        SectionHeader(
            title = stringResource(Res.string.detail_seasons),
            action = { SectionAction(stringResource(Res.string.detail_mark_all), { onAction(TitleDetailAction.OnMarkSeasonClick) }) },
        )
        ChipRow {
            series.seasons.forEach { season ->
                FilmoryChip(
                    label = stringResource(Res.string.detail_season_chip, season.number, season.checkedCount, season.episodes.size),
                    selected = season.number == state.selectedSeasonNumber,
                    onClick = { onAction(TitleDetailAction.OnSeasonClick(season.number)) },
                )
            }
        }
    }
    val episodes = series.seasons.find { it.number == state.selectedSeasonNumber }?.episodes.orEmpty()
    items(episodes, key = { "episode-${it.id}" }) { episode -> EpisodeRow(episode, onAction) }
}

@Composable
private fun ProgressCard(series: SeriesProgressUi, onAction: (TitleDetailAction) -> Unit) {
    FilmoryCard(Modifier.fillMaxWidth().padding(start = ScreenPadding, end = ScreenPadding, top = 20.dp).rise(4)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(Res.string.detail_currently_on),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val season = series.currentSeasonNumber
            val episode = series.currentEpisodeNumber
            Text(
                text = if (season != null && episode != null) {
                    stringResource(Res.string.home_season_episode, season, episode)
                } else {
                    stringResource(Res.string.detail_not_started_yet)
                },
                style = MaterialTheme.typography.titleMedium,
            )
            val total = series.episodeCount.coerceAtLeast(1).toFloat()
            GrowingProgressBar(series.watchedCount / total, secondary = series.skippedCount / total)
            Text(
                text = stringResource(Res.string.detail_episodes_checked, series.checkedCount, series.episodeCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Legend(MaterialTheme.colorScheme.primary, stringResource(Res.string.detail_episodes_watched, series.watchedCount))
                Legend(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    stringResource(Res.string.detail_episodes_skipped, series.skippedCount),
                )
            }
            Button(onClick = { onAction(TitleDetailAction.OnNextEpisodeClick) }) {
                Icon(painterResource(Res.drawable.ic_check), null, Modifier.size(18.dp))
                Text(
                    text = when {
                        episode == null -> stringResource(Res.string.detail_start_watching)
                        series.isOnLastEpisode -> stringResource(Res.string.detail_finish_last, episode)
                        else -> stringResource(Res.string.detail_finish_and_next, episode)
                    },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

/** A colour dot with a count, explaining one colour of the progress bar. */
@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EpisodeRow(episode: EpisodeUi, onAction: (TitleDetailAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        EpisodeCheck(
            mark = episode.mark,
            label = stringResource(
                when (episode.mark) {
                    EpisodeMark.NOT_WATCHED -> Res.string.detail_episode_not_watched
                    EpisodeMark.WATCHED -> Res.string.detail_episode_checked
                    EpisodeMark.SKIPPED -> Res.string.detail_episode_skipped
                },
                episode.number,
            ),
            onClick = { onAction(TitleDetailAction.OnEpisodeCheckClick(episode.id)) },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    // The marker badge is the feedback; a ripple on the text looks like a stray splash.
                    indication = null,
                    onClick = { onAction(TitleDetailAction.OnEpisodeClick(episode.id)) },
                )
                .padding(vertical = 6.dp),
        ) {
            Text(
                text = listOfNotNull(episode.number.toString() + ".", episode.name).joinToString(" "),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (episode.runtimeMinutes != null) {
                Text(
                    text = stringResource(Res.string.detail_runtime_minutes, episode.runtimeMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (episode.isCurrent) {
            FilmoryBadge(
                text = stringResource(Res.string.detail_you_are_here),
                colors = BadgeColors(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.padding(end = 12.dp),
            )
        }
    }
}

/** The round mark of an episode: empty, watched or skipped. A new mark pops in. */
@Composable
private fun EpisodeCheck(mark: EpisodeMark, label: String, onClick: () -> Unit) {
    val markAtStart = remember { mark }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                // The pop-in of the mark is the feedback.
                indication = null,
                role = Role.Checkbox,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        val colors = MaterialTheme.colorScheme
        val popIn = if (mark == markAtStart) Modifier else Modifier.popIn()
        when (mark) {
            EpisodeMark.NOT_WATCHED -> Box(Modifier.size(26.dp).border(2.dp, colors.outline, CircleShape))
            EpisodeMark.WATCHED -> MarkedCircle(Res.drawable.ic_check, colors.primary, colors.onPrimary, popIn)
            EpisodeMark.SKIPPED -> MarkedCircle(Res.drawable.ic_skip, colors.tertiaryContainer, colors.onTertiaryContainer, popIn)
        }
    }
}

@Composable
private fun MarkedCircle(icon: DrawableResource, fill: Color, onFill: Color, modifier: Modifier) {
    Box(modifier.size(26.dp).background(fill, CircleShape), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon), null, Modifier.size(16.dp), onFill)
    }
}

