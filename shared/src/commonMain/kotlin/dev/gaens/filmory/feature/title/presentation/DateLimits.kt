package dev.gaens.filmory.feature.title.presentation

import kotlinx.datetime.LocalDate

/** The days a watching date picker lets the user choose, inclusive. A null end is open. */
data class DateLimits(val earliest: LocalDate? = null, val latest: LocalDate? = null) {
    fun allows(date: LocalDate): Boolean =
        (earliest == null || date >= earliest) && (latest == null || date <= latest)

    fun allowsYear(year: Int): Boolean =
        (earliest == null || year >= earliest.year) && (latest == null || year <= latest.year)
}

/** A watching starts today at the latest, and never after it finished. */
fun startDateLimits(finished: LocalDate?, today: LocalDate): DateLimits =
    DateLimits(latest = minOf(finished ?: today, today))

/** A watching finishes from its start date to today. */
fun finishDateLimits(started: LocalDate?, today: LocalDate): DateLimits =
    DateLimits(earliest = started?.let { minOf(it, today) }, latest = today)
