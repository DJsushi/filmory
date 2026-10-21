package dev.gaens.filmory.feature.title.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
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
import dev.gaens.filmory.resources.detail_episodes_checked
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
            GrowingProgressBar(if (series.episodeCount == 0) 0f else series.checkedCount.toFloat() / series.episodeCount)
            Text(
                text = stringResource(Res.string.detail_episodes_checked, series.checkedCount, series.episodeCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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

@Composable
private fun EpisodeRow(episode: EpisodeUi, onAction: (TitleDetailAction) -> Unit) {
    val background by animateColorAsState(
        if (episode.isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .defaultMinSize(minHeight = 60.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        EpisodeCheck(
            isChecked = episode.isChecked,
            label = stringResource(Res.string.detail_episode_checked, episode.number),
            onClick = { onAction(TitleDetailAction.OnEpisodeCheckClick(episode.id)) },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onAction(TitleDetailAction.OnEpisodeClick(episode.id)) }
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

/** The round check of an episode. A new check pops in. */
@Composable
private fun EpisodeCheck(isChecked: Boolean, label: String, onClick: () -> Unit) {
    val wasCheckedAtStart = remember { isChecked }
    Box(
        modifier = Modifier
            .size(44.dp)
            .toggleable(value = isChecked, role = Role.Checkbox, onValueChange = { onClick() }),
        contentAlignment = Alignment.Center,
    ) {
        if (isChecked) {
            Box(
                modifier = Modifier
                    .then(if (wasCheckedAtStart) Modifier else Modifier.popIn())
                    .size(26.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(Res.drawable.ic_check), label, Modifier.size(16.dp), MaterialTheme.colorScheme.onPrimary)
            }
        } else {
            Box(Modifier.size(26.dp).border(2.dp, MaterialTheme.colorScheme.outline, CircleShape))
        }
    }
}
