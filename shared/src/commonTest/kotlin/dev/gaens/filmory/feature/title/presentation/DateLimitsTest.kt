package dev.gaens.filmory.feature.title.presentation

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateLimitsTest {
    private val today = LocalDate(2026, 10, 21)

    @Test
    fun `finish may be the start day up to today`() {
        val limits = finishDateLimits(started = LocalDate(2026, 10, 10), today = today)

        assertFalse(limits.allows(LocalDate(2026, 10, 9)))
        assertTrue(limits.allows(LocalDate(2026, 10, 10)))
        assertTrue(limits.allows(today))
        assertFalse(limits.allows(LocalDate(2026, 10, 22)))
    }

    @Test
    fun `finish without a start only stops at today`() {
        val limits = finishDateLimits(started = null, today = today)

        assertTrue(limits.allows(LocalDate(1999, 1, 1)))
        assertFalse(limits.allows(LocalDate(2026, 10, 22)))
    }

    @Test
    fun `start may be up to the finish day, or today when not finished`() {
        val finished = startDateLimits(finished = LocalDate(2026, 10, 10), today = today)
        val open = startDateLimits(finished = null, today = today)

        assertTrue(finished.allows(LocalDate(2026, 10, 10)))
        assertFalse(finished.allows(LocalDate(2026, 10, 11)))
        assertTrue(open.allows(today))
        assertFalse(open.allows(LocalDate(2026, 10, 22)))
    }

    @Test
    fun `a start date in the future does not lock the finish picker`() {
        val limits = finishDateLimits(started = LocalDate(2027, 1, 1), today = today)

        assertTrue(limits.allows(today))
    }

    @Test
    fun `years outside the limits are not selectable`() {
        val limits = finishDateLimits(started = LocalDate(2025, 3, 1), today = today)

        assertFalse(limits.allowsYear(2024))
        assertTrue(limits.allowsYear(2025))
        assertFalse(limits.allowsYear(2027))
    }
}
