package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class Resource(val resource: StringResource, val args: List<Any> = emptyList()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is Resource -> stringResource(resource, *args.toTypedArray())
    }

    suspend fun asStringAsync(): String = when (this) {
        is DynamicString -> value
        is Resource -> getString(resource, *args.toTypedArray())
    }
}
