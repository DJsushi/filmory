package dev.gaens.filmory.core.presentation.ui

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormattingTest {

    @Test
    fun `timestamps format as hours, minutes and seconds`() {
        assertEquals("0:24:10", formatTimestamp(1_450_000))
        assertEquals("1:31:45", formatTimestamp(5_505_000))
        assertEquals("0:00:00", formatTimestamp(0))
    }

    @Test
    fun `timestamps parse from h mm ss, m ss and seconds`() {
        assertEquals(1_450_000L, parseTimestamp("0:24:10"))
        assertEquals(1_450_000L, parseTimestamp("24:10"))
        assertEquals(42_000L, parseTimestamp("42"))
        assertEquals(5_505_000L, parseTimestamp(formatTimestamp(5_505_000)))
    }

    @Test
    fun `invalid timestamps do not parse`() {
        assertNull(parseTimestamp(""))
        assertNull(parseTimestamp("1:75:00"))
        assertNull(parseTimestamp("a:10"))
        assertNull(parseTimestamp("1:2:3:4"))
        assertNull(parseTimestamp("-5"))
    }

    @Test
    fun `dates format like the mockup`() {
        assertEquals("3 Mar 2024", LocalDate(2024, 3, 3).formatDayMonthYear())
        assertEquals("October 2026", LocalDate(2026, 10, 1).formatMonthYear())
    }
}
