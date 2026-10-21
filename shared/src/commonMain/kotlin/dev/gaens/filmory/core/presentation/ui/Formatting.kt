package dev.gaens.filmory.core.presentation.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L

/** A position in a movie or episode as h:mm:ss, for example 1:31:45. */
fun formatTimestamp(millis: Long): String {
    val totalSeconds = millis / MILLIS_PER_SECOND
    val hours = totalSeconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR)
    val minutes = totalSeconds / SECONDS_PER_MINUTE % MINUTES_PER_HOUR
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "$hours:${minutes.twoDigits()}:${seconds.twoDigits()}"
}

/** Parses h:mm:ss, m:ss or plain seconds back to milliseconds. Null when the text is not a time. */
fun parseTimestamp(text: String): Long? {
    val parts = text.trim().split(':')
    if (parts.size > 3 || parts.any { it.isEmpty() }) return null
    val numbers = parts.map { it.toLongOrNull()?.takeIf { value -> value >= 0 } ?: return null }
    if (numbers.drop(1).any { it >= SECONDS_PER_MINUTE }) return null
    val seconds = numbers.fold(0L) { total, part -> total * SECONDS_PER_MINUTE + part }
    return seconds * MILLIS_PER_SECOND
}

private fun Long.twoDigits(): String = toString().padStart(2, '0')

// shortcut: month names are English only; switch to resource-backed names when the app is translated.
private val DayMonthYear = LocalDate.Format {
    day(padding = Padding.NONE)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

private val MonthYear = LocalDate.Format {
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

fun LocalDate.formatDayMonthYear(): String = format(DayMonthYear)

fun LocalDate.formatMonthYear(): String = format(MonthYear)

fun Instant.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    toLocalDateTime(timeZone).date

fun Instant.formatDate(): String = toLocalDate().formatDayMonthYear()

/** "12 Oct 2026, 9:41 PM". */
fun Instant.formatDateTime(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val hour12 = (dateTime.hour + 11) % 12 + 1
    val period = if (dateTime.hour < 12) "AM" else "PM"
    return "${dateTime.date.formatDayMonthYear()}, $hour12:${dateTime.minute.toLong().twoDigits()} $period"
}
