package dev.gaens.filmory.core.data

import kotlin.time.Instant

internal fun Long.toInstant(): Instant = Instant.fromEpochMilliseconds(this)

internal fun Instant.toEpochMillis(): Long = toEpochMilliseconds()
