package dev.gaens.filmory.core.data

import androidx.sqlite.SQLiteException
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.EmptyResult
import dev.gaens.filmory.core.domain.util.Result
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Runs a database write and turns storage failures into a [DataError.Local]. */
internal suspend inline fun safeDatabaseWrite(write: () -> Unit): EmptyResult<DataError.Local> =
    try {
        write()
        Result.Success(Unit)
    } catch (e: SQLiteException) {
        val isDiskFull = e.message?.contains("full", ignoreCase = true) == true
        Result.Error(if (isDiskFull) DataError.Local.DISK_FULL else DataError.Local.UNKNOWN)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Result.Error(DataError.Local.UNKNOWN)
    }
