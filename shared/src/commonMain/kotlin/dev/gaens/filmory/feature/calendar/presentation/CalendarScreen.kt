package dev.gaens.filmory.feature.calendar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ChipRow
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.ScreenHeader
import dev.gaens.filmory.core.presentation.designsystem.components.StarRating
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.core.presentation.ui.formatMonthYear
import dev.gaens.filmory.core.presentation.ui.rememberImageSharer
import dev.gaens.filmory.feature.calendar.domain.CalendarDay
import dev.gaens.filmory.feature.calendar.domain.CalendarEntry
import dev.gaens.filmory.feature.calendar.domain.EntryBadge
import dev.gaens.filmory.feature.calendar.domain.EntryKind
import dev.gaens.filmory.feature.settings.domain.CalendarDayLayout
import dev.gaens.filmory.feature.settings.domain.CalendarSettings
import dev.gaens.filmory.feature.settings.domain.WeekStart
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.calendar_export
import dev.gaens.filmory.resources.calendar_finish_dates
import dev.gaens.filmory.resources.calendar_legend
import dev.gaens.filmory.resources.calendar_logged_dates
import dev.gaens.filmory.resources.calendar_next_month
import dev.gaens.filmory.resources.calendar_previous_month
import dev.gaens.filmory.resources.calendar_ranges
import dev.gaens.filmory.resources.calendar_settings
import dev.gaens.filmory.resources.calendar_subtitle
import dev.gaens.filmory.resources.calendar_title
import dev.gaens.filmory.resources.ic_check
import dev.gaens.filmory.resources.ic_chevron_left
import dev.gaens.filmory.resources.ic_chevron_right
import dev.gaens.filmory.resources.ic_download
import dev.gaens.filmory.resources.ic_flag
import dev.gaens.filmory.resources.ic_tune
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DayOfWeekNames
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalendarScreenRoot(
    onBackClick: () -> Unit,
    onTitleClick: (String) -> Unit,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val calendarLayer = rememberGraphicsLayer()
    val shareImage = rememberImageSharer()
    val scope = rememberCoroutineScope()
    CalendarScreen(
        state = state,
        calendarLayer = calendarLayer,
        onAction = { action ->
            when (action) {
                CalendarAction.OnBackClick -> onBackClick()
                is CalendarAction.OnTitleClick -> onTitleClick(action.titleId)
                CalendarAction.OnExportClick -> scope.launch {
                    shareImage(calendarLayer.toImageBitmap(), "filmory-${state.month}.png")
                }
                else -> Unit
            }
            viewModel.onAction(action)
        },
    )
}

@Composable
fun CalendarScreen(
    state: CalendarState,
    onAction: (CalendarAction) -> Unit,
    calendarLayer: GraphicsLayer? = null,
) {
    val settings = state.settings
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item(key = "header") {
            ScreenHeader(
                title = stringResource(Res.string.calendar_title),
                subtitle = stringResource(Res.string.calendar_subtitle),
                onBackClick = { onAction(CalendarAction.OnBackClick) },
                actions = {
                    FilmoryIconButton(Res.drawable.ic_tune, stringResource(Res.string.calendar_settings), {
                        onAction(CalendarAction.OnSettingsClick)
                    })
                    FilmoryIconButton(Res.drawable.ic_download, stringResource(Res.string.calendar_export), {
                        onAction(CalendarAction.OnExportClick)
                    })
                },
            )
        }
        item(key = "switches") {
            ChipRow(Modifier.padding(top = 12.dp).rise(2)) {
                FilmoryChip(stringResource(Res.string.calendar_finish_dates), settings.showFinishDates, {
                    onAction(CalendarAction.OnSettingsChange(settings.copy(showFinishDates = !settings.showFinishDates)))
                })
                FilmoryChip(stringResource(Res.string.calendar_ranges), settings.showRanges, {
                    onAction(CalendarAction.OnSettingsChange(settings.copy(showRanges = !settings.showRanges)))
                })
                FilmoryChip(stringResource(Res.string.calendar_logged_dates), settings.showLoggedDates, {
                    onAction(CalendarAction.OnSettingsChange(settings.copy(showLoggedDates = !settings.showLoggedDates)))
                })
            }
        }
        item(key = "month") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = ScreenPadding + 4.dp, end = ScreenPadding - 12.dp, top = 12.dp, bottom = 8.dp).rise(3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.month?.let { LocalDate(it.year, it.month, 1).formatMonthYear() }.orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
                FilmoryIconButton(Res.drawable.ic_chevron_left, stringResource(Res.string.calendar_previous_month), {
                    onAction(CalendarAction.OnPreviousMonthClick)
                })
                FilmoryIconButton(Res.drawable.ic_chevron_right, stringResource(Res.string.calendar_next_month), {
                    onAction(CalendarAction.OnNextMonthClick)
                })
            }
        }
        item(key = "grid") {
            val captured = if (calendarLayer == null) {
                Modifier
            } else {
                Modifier.drawWithContent {
                    calendarLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(calendarLayer)
                }
            }
            MonthGrid(state, onAction, Modifier.padding(horizontal = 16.dp).rise(4).then(captured))
        }
        item(key = "legend") {
            Text(
                text = stringResource(Res.string.calendar_legend),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = ScreenPadding - 4.dp, vertical = 12.dp),
            )
        }
    }
    if (state.isSettingsOpen) CalendarSettingsSheet(settings, onAction)
    val selected = state.selectedDate
    if (selected != null) {
        val day = state.calendar?.weeks?.flatten()?.find { it?.date == selected }
        DaySheet(selected, day?.entries.orEmpty(), onAction)
    }
}

private val GridShape = RoundedCornerShape(22.dp)
private val SundayColor = Color(0xFFBA1A1A)

@Composable
private fun MonthGrid(state: CalendarState, onAction: (CalendarAction) -> Unit, modifier: Modifier = Modifier) {
    val calendar = state.calendar ?: return
    val settings = state.settings
    Column(
        modifier = modifier
            .softShadow(GridShape)
            .clip(GridShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(
                if (settings.showVerticalBorder) {
                    Modifier.border(BorderWidth, MaterialTheme.colorScheme.outlineVariant, GridShape)
                } else {
                    Modifier
                },
            ),
    ) {
        Row {
            weekDays(settings.weekStart).forEach { dayOfWeek ->
                Text(
                    text = DayOfWeekNames.ENGLISH_ABBREVIATED.names[dayOfWeek.ordinal],
                    style = MaterialTheme.typography.labelMedium,
                    color = if (dayOfWeek == DayOfWeek.SUNDAY) SundayColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(top = 10.dp, bottom = 6.dp),
                )
            }
        }
        val borderColor = MaterialTheme.colorScheme.outlineVariant
        calendar.weeks.forEachIndexed { weekIndex, week ->
            Row {
                week.forEachIndexed { dayIndex, day ->
                    val showEnd = settings.showVerticalBorder && dayIndex < week.lastIndex
                    val showBottom = settings.showVerticalBorder && weekIndex < calendar.weeks.lastIndex
                    Box(Modifier.weight(1f).height(DayHeight).innerBorders(borderColor, showEnd, showBottom)) {
                        if (day != null) DayCell(day, day.date == state.today, settings, onAction)
                    }
                }
            }
        }
    }
}

private val DayHeight = 76.dp
private val BorderWidth = 0.5.dp

/** Draws only the right and bottom lines, so a line shared by two cells is drawn once at full width. */
private fun Modifier.innerBorders(color: Color, showEnd: Boolean, showBottom: Boolean): Modifier = drawBehind {
    val width = BorderWidth.toPx()
    if (showEnd) drawLine(color, Offset(size.width - width / 2, 0f), Offset(size.width - width / 2, size.height), width)
    if (showBottom) drawLine(color, Offset(0f, size.height - width / 2), Offset(size.width, size.height - width / 2), width)
}

private fun weekDays(weekStart: WeekStart): List<DayOfWeek> {
    val first = if (weekStart == WeekStart.MONDAY) DayOfWeek.MONDAY else DayOfWeek.SUNDAY
    return List(7) { DayOfWeek.entries[(first.ordinal + it) % 7] }
}

@Composable
private fun DayCell(day: CalendarDay, isToday: Boolean, settings: CalendarSettings, onAction: (CalendarAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onAction(CalendarAction.OnDayClick(day.date)) }
            .padding(top = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(24.dp)) {
            day.ranges.firstOrNull()?.let { range ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = if (range.isStart) 8.dp else 0.dp, end = if (range.isEnd) 8.dp else 0.dp)
                        .height(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(
                                topStart = if (range.isStart) 12.dp else 0.dp,
                                bottomStart = if (range.isStart) 12.dp else 0.dp,
                                topEnd = if (range.isEnd) 12.dp else 0.dp,
                                bottomEnd = if (range.isEnd) 12.dp else 0.dp,
                            ),
                        ),
                )
            }
            val isSunday = day.date.dayOfWeek == DayOfWeek.SUNDAY
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(if (isToday) Modifier.background(MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.date.day.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        isSunday -> SundayColor
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
        if (day.entries.isNotEmpty()) DayPosters(day.entries, settings)
    }
}

@Composable
private fun DayPosters(entries: List<CalendarEntry>, settings: CalendarSettings) {
    val first = entries.first()
    when {
        entries.size == 1 || settings.dayLayout == CalendarDayLayout.STACK -> Box(Modifier.padding(top = 2.dp)) {
            if (entries.size > 1) {
                MiniPoster(entries[1], Modifier.offset(x = 8.dp, y = 2.dp).rotate(8f), settings)
            }
            MiniPoster(first, Modifier, settings)
        }
        else -> Column(Modifier.padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            entries.take(MAX_GRID_POSTERS).chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEach { MiniPoster(it, Modifier, settings, small = true) }
                }
            }
        }
    }
    val stars = first.rating?.stars
    if (settings.showStars && first.kind == EntryKind.FINISHED && stars != null && entries.size == 1) {
        StarRating(stars, starSize = 6.dp)
    }
}

private const val MAX_GRID_POSTERS = 4

@Composable
private fun MiniPoster(entry: CalendarEntry, modifier: Modifier, settings: CalendarSettings, small: Boolean = false) {
    val width = if (small) 16.dp else 28.dp
    Box(modifier) {
        PosterArt(
            seed = entry.title.id,
            name = entry.title.name,
            posterUrl = entry.title.posterUrl,
            shape = RoundedCornerShape(5.dp),
            showName = false,
            modifier = Modifier.size(width = width, height = width * 4 / 3),
        )
        val badge = entry.badge
        if (settings.showStateBadge && badge != null && !small) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(12.dp)
                    .background(
                        if (badge == EntryBadge.COMPLETED) FilmoryTheme.colors.watchedContainer else MaterialTheme.colorScheme.errorContainer,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (badge == EntryBadge.COMPLETED) Res.drawable.ic_check else Res.drawable.ic_flag),
                    contentDescription = null,
                    modifier = Modifier.size(8.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalendarScreenPreview() {
    FilmoryTheme { CalendarScreen(CalendarState(), onAction = {}) }
}
