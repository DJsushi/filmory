package dev.gaens.filmory.feature.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.LocalReducedMotion
import dev.gaens.filmory.core.presentation.designsystem.components.BadgeColors
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryBadge
import dev.gaens.filmory.core.presentation.designsystem.components.FilmorySegmentedRow
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.components.SettingSwitch
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.feature.episode.domain.placeMarker
import dev.gaens.filmory.feature.settings.domain.MarkerTarget
import dev.gaens.filmory.feature.settings.domain.SeriesSettings
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.detail_you_are_here
import dev.gaens.filmory.resources.ic_check
import dev.gaens.filmory.resources.settings_marker_backwards
import dev.gaens.filmory.resources.settings_marker_backwards_off_hint
import dev.gaens.filmory.resources.settings_marker_backwards_on_hint
import dev.gaens.filmory.resources.settings_marker_example_ahead
import dev.gaens.filmory.resources.settings_marker_example_behind
import dev.gaens.filmory.resources.settings_marker_example_result
import dev.gaens.filmory.resources.settings_marker_moves
import dev.gaens.filmory.resources.settings_marker_moves_hint
import dev.gaens.filmory.resources.settings_marker_next
import dev.gaens.filmory.resources.settings_marker_next_hint
import dev.gaens.filmory.resources.settings_marker_stays
import dev.gaens.filmory.resources.settings_marker_stays_hint
import dev.gaens.filmory.resources.settings_marker_target
import dev.gaens.filmory.resources.settings_marker_ticked
import dev.gaens.filmory.resources.settings_marker_ticked_hint
import dev.gaens.filmory.resources.settings_marker_when
import dev.gaens.filmory.resources.settings_series
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val EXAMPLE_EPISODES = 5

/**
 * The Series card on the Profile tab: what ticking an episode does to the "You are here" marker.
 * Choosing "Marker moves" reveals the details, and a live example shows what the choices do.
 */
@Composable
fun SeriesSettingsSection(series: SeriesSettings, onAction: (SettingsAction) -> Unit) {
    SectionHeader(stringResource(Res.string.settings_series))
    SettingsCard(Modifier.rise(3)) {
        Text(stringResource(Res.string.settings_marker_when), style = MaterialTheme.typography.bodyLarge)
        FilmorySegmentedRow(
            options = listOf(stringResource(Res.string.settings_marker_stays), stringResource(Res.string.settings_marker_moves)),
            selectedIndex = if (series.markerFollowsTicks) 1 else 0,
            onSelect = { onAction(SettingsAction.OnMarkerFollowsTicksChange(it == 1)) },
        )
        Hint(stringResource(if (series.markerFollowsTicks) Res.string.settings_marker_moves_hint else Res.string.settings_marker_stays_hint))

        val isReduced = LocalReducedMotion.current
        AnimatedVisibility(
            visible = series.markerFollowsTicks,
            enter = if (isReduced) EnterTransition.None else expandVertically() + fadeIn(),
            exit = if (isReduced) ExitTransition.None else shrinkVertically() + fadeOut(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.settings_marker_target), style = MaterialTheme.typography.bodyLarge)
                val targets = MarkerTarget.entries
                FilmorySegmentedRow(
                    options = targets.map {
                        stringResource(if (it == MarkerTarget.NEXT) Res.string.settings_marker_next else Res.string.settings_marker_ticked)
                    },
                    selectedIndex = targets.indexOf(series.markerTarget),
                    onSelect = { onAction(SettingsAction.OnMarkerTargetChange(targets[it])) },
                )
                Hint(
                    stringResource(
                        if (series.markerTarget == MarkerTarget.NEXT) Res.string.settings_marker_next_hint else Res.string.settings_marker_ticked_hint,
                    ),
                )
                SettingSwitch(
                    label = stringResource(Res.string.settings_marker_backwards),
                    checked = series.markerMovesBackwards,
                    onCheckedChange = { onAction(SettingsAction.OnMarkerMovesBackwardsChange(it)) },
                )
                Hint(
                    stringResource(
                        if (series.markerMovesBackwards) Res.string.settings_marker_backwards_on_hint else Res.string.settings_marker_backwards_off_hint,
                    ),
                )
            }
        }
        Example(stringResource(Res.string.settings_marker_example_ahead), series, markerOn = 3, ticked = 3, alreadyTicked = setOf(1, 2))
        AnimatedVisibility(
            visible = series.markerFollowsTicks,
            enter = if (isReduced) EnterTransition.None else expandVertically() + fadeIn(),
            exit = if (isReduced) ExitTransition.None else shrinkVertically() + fadeOut(),
        ) {
            Example(stringResource(Res.string.settings_marker_example_behind), series, markerOn = 4, ticked = 1, alreadyTicked = setOf(2, 3))
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/**
 * A small live example: [EXAMPLE_EPISODES] episodes, the marker on [markerOn], and the user ticking
 * [ticked]. The result comes from the same rule the title screen uses.
 */
@Composable
private fun Example(title: String, series: SeriesSettings, markerOn: Int, ticked: Int, alreadyTicked: Set<Int>) {
    val ids = (1..EXAMPLE_EPISODES).map { it.toString() }
    val markerAfter = placeMarker(ids, markerOn.toString(), ticked.toString(), series)?.toInt() ?: markerOn
    val checked = alreadyTicked + ticked
    val result = stringResource(Res.string.settings_marker_example_result, markerAfter)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$title $result" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (episode in 1..EXAMPLE_EPISODES) {
                ExampleEpisode(episode, isChecked = episode in checked, isMarker = episode == markerAfter)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilmoryBadge(
                text = stringResource(Res.string.detail_you_are_here),
                colors = BadgeColors(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary),
            )
            Text(result, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ExampleEpisode(number: Int, isChecked: Boolean, isMarker: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val fill by animateColorAsState(if (isChecked) primary else MaterialTheme.colorScheme.surfaceContainerHigh)
    val markerColor by animateColorAsState(if (isMarker) primary else MaterialTheme.colorScheme.surfaceContainerHigh)
    Column(
        modifier = Modifier.clearAndSetSemantics {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(fill, CircleShape)
                .then(if (isChecked) Modifier else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)),
            contentAlignment = Alignment.Center,
        ) {
            if (isChecked) {
                Icon(painterResource(Res.drawable.ic_check), null, Modifier.size(16.dp), MaterialTheme.colorScheme.onPrimary)
            }
        }
        Text(number.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.size(width = 18.dp, height = 4.dp).background(markerColor, CircleShape))
    }
}
