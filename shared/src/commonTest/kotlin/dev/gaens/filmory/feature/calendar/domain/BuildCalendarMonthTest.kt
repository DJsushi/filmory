package dev.gaens.filmory.feature.calendar.domain

import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.settings.domain.CalendarSettings
import dev.gaens.filmory.feature.settings.domain.WeekStart
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.testing.aNote
import dev.gaens.filmory.testing.aTitle
import dev.gaens.filmory.testing.aWatching
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BuildCalendarMonthTest {
    private val build = BuildCalendarMonth()
    private val zone = TimeZone.UTC
    private val october = YearMonth(2026, Month.OCTOBER)
    private val today = LocalDate(2026, 10, 20)
    private fun day(d: Int) = LocalDate(2026, 10, d).atStartOfDayIn(zone)

    private val titles = listOf(aTitle(id = "a"), aTitle(id = "b"))
    private val watchings = listOf(
        aWatching(id = "wa", titleId = "a", state = WatchingState.FINISHED, startedAt = day(5), finishedAt = day(7))
            .copy(rating = Rating(9)),
        aWatching(id = "wb", titleId = "b", state = WatchingState.IN_PROGRESS, startedAt = day(18)),
    )
    private val notes = listOf(aNote(id = "n", watchingId = "wb", titleId = "b").copy(createdAt = day(12)))
    private val checks = listOf(EpisodeCheck("c", "wb", "e1", day(19)))

    private fun month(settings: CalendarSettings = CalendarSettings()) =
        build(october, settings, titles, watchings, notes, checks, today, zone)

    private fun CalendarMonth.day(d: Int) = weeks.flatten().filterNotNull().single { it.date.day == d }

    @Test
    fun `weeks start on monday or sunday with blanks before the first`() {
        // 1 October 2026 is a Thursday.
        assertEquals(3, month().weeks.first().takeWhile { it == null }.size)
        assertEquals(4, month(CalendarSettings(weekStart = WeekStart.SUNDAY)).weeks.first().takeWhile { it == null }.size)
        assertTrue(month().weeks.all { it.size == 7 })
    }

    @Test
    fun `a finished watching shows on its finish date with its rating and a badge`() {
        val entry = month().day(7).entries.single()

        assertEquals(EntryKind.FINISHED, entry.kind)
        assertEquals(Rating(9), entry.rating)
        assertEquals(EntryBadge.COMPLETED, entry.badge)
    }

    @Test
    fun `logged dates cover starts, notes and checked episodes`() {
        val month = month()

        assertEquals(listOf("a"), month.day(5).entries.map { it.title.id })
        assertEquals(listOf("b"), month.day(12).entries.map { it.title.id })
        assertEquals(listOf("b"), month.day(19).entries.map { it.title.id })
        assertTrue(month.day(13).entries.isEmpty())
    }

    @Test
    fun `ranges run from start to finish, and to today while in progress`() {
        val month = month(CalendarSettings(showLoggedDates = false))

        assertEquals(RangeMark("a", isStart = true, isEnd = false), month.day(5).ranges.single())
        assertEquals(RangeMark("a", isStart = false, isEnd = false), month.day(6).ranges.single())
        assertEquals(RangeMark("a", isStart = false, isEnd = true), month.day(7).ranges.single())
        assertEquals(RangeMark("b", isStart = false, isEnd = true), month.day(20).ranges.single())
        assertTrue(month.day(21).ranges.isEmpty())
    }

    @Test
    fun `each switch turns its kind of mark off`() {
        val off = month(CalendarSettings(showFinishDates = false, showRanges = false, showLoggedDates = false))

        assertTrue(off.weeks.flatten().filterNotNull().all { it.entries.isEmpty() && it.ranges.isEmpty() })
        assertNull(month(CalendarSettings(showFinishDates = false)).day(7).entries.single().badge)
    }
}
