package dev.gaens.filmory.feature.calendar.domain

import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.note.domain.Note
import dev.gaens.filmory.feature.settings.domain.CalendarSettings
import dev.gaens.filmory.feature.settings.domain.WeekStart
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingState
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

data class CalendarMonth(
    val month: YearMonth,
    /** Full weeks; days outside the month are null. */
    val weeks: List<List<CalendarDay?>>,
)

data class CalendarDay(
    val date: LocalDate,
    val entries: List<CalendarEntry>,
    val ranges: List<RangeMark>,
)

enum class EntryKind { FINISHED, LOGGED }

enum class EntryBadge { COMPLETED, DROPPED }

data class CalendarEntry(
    val title: Title,
    val kind: EntryKind,
    val rating: Rating?,
    val badge: EntryBadge?,
)

/** Where a day sits in a start-to-finish span of one watching. */
data class RangeMark(val titleId: String, val isStart: Boolean, val isEnd: Boolean)

/**
 * Places titles on the days of a month, following the three calendar switches: finish dates,
 * start-to-finish ranges and logged dates (notes, checked episodes, start and finish dates).
 */
class BuildCalendarMonth {

    @Suppress("LongParameterList")
    operator fun invoke(
        month: YearMonth,
        settings: CalendarSettings,
        titles: List<Title>,
        watchings: List<Watching>,
        notes: List<Note>,
        checks: List<EpisodeCheck>,
        today: LocalDate,
        timeZone: TimeZone,
    ): CalendarMonth {
        val titlesById = titles.associateBy { it.id }
        val watchingsById = watchings.associateBy { it.id }
        fun Instant.date() = toLocalDateTime(timeZone).date

        val finished = if (settings.showFinishDates) {
            watchings.filter { it.state == WatchingState.FINISHED }.mapNotNull { watching ->
                val date = watching.finishedAt?.date() ?: return@mapNotNull null
                val title = titlesById[watching.titleId] ?: return@mapNotNull null
                date to CalendarEntry(title, EntryKind.FINISHED, watching.rating, EntryBadge.COMPLETED)
            }
        } else {
            emptyList()
        }

        val logged = if (settings.showLoggedDates) {
            val activity = buildList {
                watchings.forEach { watching ->
                    watching.startedAt?.let { add(watching to it) }
                    watching.finishedAt?.let { add(watching to it) }
                }
                notes.forEach { note -> watchingsById[note.watchingId]?.let { add(it to note.createdAt) } }
                // A skipped episode was not watched that day.
                checks.filterNot { it.isSkipped }.forEach { check ->
                    watchingsById[check.watchingId]?.let { add(it to check.checkedAt) }
                }
            }
            activity.mapNotNull { (watching, instant) ->
                val title = titlesById[watching.titleId] ?: return@mapNotNull null
                val badge = if (watching.state == WatchingState.DROPPED) EntryBadge.DROPPED else null
                instant.date() to CalendarEntry(title, EntryKind.LOGGED, null, badge)
            }
        } else {
            emptyList()
        }

        val entriesByDate = (finished + logged)
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, entries) -> entries.withoutDuplicates() }

        val rangesByDate = if (settings.showRanges) rangeMarks(watchings, today) { it.date() } else emptyMap()

        val days = (1..month.numberOfDays).map { day ->
            val date = LocalDate(month.year, month.month, day)
            CalendarDay(date, entriesByDate[date].orEmpty(), rangesByDate[date].orEmpty())
        }
        return CalendarMonth(month, days.toWeeks(settings.weekStart))
    }

    /** A finished entry wins over logged ones for the same title on the same day. */
    private fun List<CalendarEntry>.withoutDuplicates(): List<CalendarEntry> =
        groupBy { it.title.id }.map { (_, forTitle) -> forTitle.firstOrNull { it.kind == EntryKind.FINISHED } ?: forTitle.first() }

    private fun rangeMarks(
        watchings: List<Watching>,
        today: LocalDate,
        dateOf: (Instant) -> LocalDate,
    ): Map<LocalDate, List<RangeMark>> {
        val marks = mutableMapOf<LocalDate, MutableList<RangeMark>>()
        watchings.forEach { watching ->
            val start = watching.startedAt?.let(dateOf) ?: return@forEach
            val end = watching.finishedAt?.let(dateOf)
                ?: if (watching.state == WatchingState.IN_PROGRESS) today else return@forEach
            if (end < start) return@forEach
            var date = start
            repeat(start.daysUntil(end) + 1) {
                marks.getOrPut(date) { mutableListOf() } += RangeMark(watching.titleId, date == start, date == end)
                date = date.plus(DatePeriod(days = 1))
            }
        }
        return marks
    }

    private fun List<CalendarDay>.toWeeks(weekStart: WeekStart): List<List<CalendarDay?>> {
        val firstDayOfWeek = if (weekStart == WeekStart.MONDAY) DayOfWeek.MONDAY else DayOfWeek.SUNDAY
        val leading = (first().date.dayOfWeek.ordinal - firstDayOfWeek.ordinal + DAYS_PER_WEEK) % DAYS_PER_WEEK
        val cells: List<CalendarDay?> = List(leading) { null } + this
        val trailing = (DAYS_PER_WEEK - cells.size % DAYS_PER_WEEK) % DAYS_PER_WEEK
        return (cells + List(trailing) { null }).chunked(DAYS_PER_WEEK)
    }

    private companion object {
        const val DAYS_PER_WEEK = 7
    }
}
