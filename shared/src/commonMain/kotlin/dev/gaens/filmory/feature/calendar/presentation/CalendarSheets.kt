package dev.gaens.filmory.feature.calendar.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.FilmorySegmentedRow
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.SettingSwitch
import dev.gaens.filmory.core.presentation.designsystem.components.StarRating
import dev.gaens.filmory.core.presentation.ui.formatDayMonthYear
import dev.gaens.filmory.feature.calendar.domain.CalendarEntry
import dev.gaens.filmory.feature.settings.domain.CalendarDayLayout
import dev.gaens.filmory.feature.settings.domain.CalendarSettings
import dev.gaens.filmory.feature.settings.domain.WeekStart
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.calendar_day_empty
import dev.gaens.filmory.resources.calendar_finish_dates
import dev.gaens.filmory.resources.calendar_layout
import dev.gaens.filmory.resources.calendar_layout_grid
import dev.gaens.filmory.resources.calendar_layout_stack
import dev.gaens.filmory.resources.calendar_logged_dates
import dev.gaens.filmory.resources.calendar_monday
import dev.gaens.filmory.resources.calendar_ranges
import dev.gaens.filmory.resources.calendar_settings
import dev.gaens.filmory.resources.calendar_show_stars
import dev.gaens.filmory.resources.calendar_state_badge
import dev.gaens.filmory.resources.calendar_sunday
import dev.gaens.filmory.resources.calendar_vertical_border
import dev.gaens.filmory.resources.calendar_week_start
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSettingsSheet(settings: CalendarSettings, onAction: (CalendarAction) -> Unit) {
    val change = { updated: CalendarSettings -> onAction(CalendarAction.OnSettingsChange(updated)) }
    ModalBottomSheet(
        onDismissRequest = { onAction(CalendarAction.OnSheetDismiss) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(Res.string.calendar_settings), style = MaterialTheme.typography.headlineSmall)
            SettingSwitch(stringResource(Res.string.calendar_finish_dates), settings.showFinishDates) {
                change(settings.copy(showFinishDates = it))
            }
            SettingSwitch(stringResource(Res.string.calendar_ranges), settings.showRanges) {
                change(settings.copy(showRanges = it))
            }
            SettingSwitch(stringResource(Res.string.calendar_logged_dates), settings.showLoggedDates) {
                change(settings.copy(showLoggedDates = it))
            }
            SettingSwitch(stringResource(Res.string.calendar_show_stars), settings.showStars) {
                change(settings.copy(showStars = it))
            }
            SettingSwitch(stringResource(Res.string.calendar_vertical_border), settings.showVerticalBorder) {
                change(settings.copy(showVerticalBorder = it))
            }
            SettingSwitch(stringResource(Res.string.calendar_state_badge), settings.showStateBadge) {
                change(settings.copy(showStateBadge = it))
            }
            Text(stringResource(Res.string.calendar_layout), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            FilmorySegmentedRow(
                options = listOf(stringResource(Res.string.calendar_layout_stack), stringResource(Res.string.calendar_layout_grid)),
                selectedIndex = if (settings.dayLayout == CalendarDayLayout.STACK) 0 else 1,
                onSelect = { change(settings.copy(dayLayout = if (it == 0) CalendarDayLayout.STACK else CalendarDayLayout.GRID)) },
            )
            Text(stringResource(Res.string.calendar_week_start), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            FilmorySegmentedRow(
                options = listOf(stringResource(Res.string.calendar_monday), stringResource(Res.string.calendar_sunday)),
                selectedIndex = if (settings.weekStart == WeekStart.MONDAY) 0 else 1,
                onSelect = { change(settings.copy(weekStart = if (it == 0) WeekStart.MONDAY else WeekStart.SUNDAY)) },
            )
        }
    }
}

/** The titles logged on one day; tapping one opens it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySheet(date: LocalDate, entries: List<CalendarEntry>, onAction: (CalendarAction) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { onAction(CalendarAction.OnSheetDismiss) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                text = date.formatDayMonthYear(),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp),
            )
            if (entries.isEmpty()) {
                Text(
                    text = stringResource(Res.string.calendar_day_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                )
            }
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(CalendarAction.OnTitleClick(entry.title.id)) }
                        .padding(horizontal = ScreenPadding, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PosterArt(entry.title.id, entry.title.name, entry.title.posterUrl, Modifier.width(40.dp), showName = false)
                    Column(Modifier.weight(1f)) {
                        Text(entry.title.name, style = MaterialTheme.typography.titleMedium)
                        entry.rating?.let { StarRating(it.stars, starSize = 12.dp) }
                    }
                }
            }
        }
    }
}
