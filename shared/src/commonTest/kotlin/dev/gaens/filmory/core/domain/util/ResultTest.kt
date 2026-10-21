package dev.gaens.filmory.core.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ResultTest {

    @Test
    fun `map transforms a success`() {
        val result: Result<Int, DataError.Local> = Result.Success(2)

        assertEquals(Result.Success(4), result.map { it * 2 })
    }

    @Test
    fun `map keeps an error untouched`() {
        val result: Result<Int, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)

        assertEquals(Result.Error(DataError.Local.NOT_FOUND), result.map { it * 2 })
    }

    @Test
    fun `asEmptyDataResult drops the data`() {
        val result: Result<Int, DataError.Local> = Result.Success(2)

        assertEquals(Result.Success(Unit), result.asEmptyDataResult())
    }

    @Test
    fun `onSuccess runs only for success and onError only for error`() {
        val calls = mutableListOf<String>()
        val success: Result<Int, DataError.Local> = Result.Success(1)
        val failure: Result<Int, DataError.Local> = Result.Error(DataError.Local.UNKNOWN)

        success.onSuccess { calls += "success" }.onError { calls += "error" }
        failure.onSuccess { calls += "success" }.onError { calls += "error" }

        assertEquals(listOf("success", "error"), calls)
    }
}
