package dev.gaens.filmory.feature.library.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_close
import dev.gaens.filmory.resources.ic_close
import dev.gaens.filmory.resources.library_all_collections
import dev.gaens.filmory.resources.library_all_statuses
import dev.gaens.filmory.resources.library_collection_all
import dev.gaens.filmory.resources.library_collection_filter
import dev.gaens.filmory.resources.library_movies
import dev.gaens.filmory.resources.library_search_hint
import dev.gaens.filmory.resources.library_series
import dev.gaens.filmory.resources.library_status_all
import dev.gaens.filmory.resources.library_status_filter
import org.jetbrains.compose.resources.stringResource

// The search field and filter chips of a title list. The library, the collection screen and the
// "add a note to" picker all use these, so a title is found the same way everywhere.

/** A collection that titles can be filtered by. */
data class CollectionOptionUi(val id: String, val name: String)

/**
 * The search field above a title list. [onClear] shows a clear button; pass null to hide it.
 * [requestFocus] puts the cursor in the field, and the keyboard up, when it first appears.
 */
@Composable
fun TitleSearchField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    requestFocus: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    if (requestFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        placeholder = { Text(stringResource(Res.string.library_search_hint)) },
        trailingIcon = onClear?.let { clear ->
            { FilmoryIconButton(Res.drawable.ic_close, stringResource(Res.string.action_close), clear) }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
    )
}

/** Movies and Series chips. Tapping the selected one clears it, which is up to [onClick]. */
@Composable
fun RowScope.TypeFilterChips(selected: TitleType?, onClick: (TitleType) -> Unit) {
    FilmoryChip(
        label = stringResource(Res.string.library_movies),
        selected = selected == TitleType.MOVIE,
        onClick = { onClick(TitleType.MOVIE) },
    )
    FilmoryChip(
        label = stringResource(Res.string.library_series),
        selected = selected == TitleType.SERIES,
        onClick = { onClick(TitleType.SERIES) },
    )
}

/** A chip that opens a menu of all statuses. Null means every status. */
@Composable
fun StatusFilterChip(selected: TitleStatus?, onChange: (TitleStatus?) -> Unit) {
    FilterMenuChip(
        label = if (selected == null) {
            stringResource(Res.string.library_status_all)
        } else {
            stringResource(Res.string.library_status_filter, selected.label())
        },
        selected = true,
    ) { close ->
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.library_all_statuses)) },
            onClick = { close(); onChange(null) },
        )
        TitleStatus.entries.forEach { status ->
            DropdownMenuItem(text = { Text(status.label()) }, onClick = { close(); onChange(status) })
        }
    }
}

/** A chip that opens a menu of the user's collections. Null means any title, in a collection or not. */
@Composable
fun CollectionFilterChip(collections: List<CollectionOptionUi>, selectedId: String?, onChange: (String?) -> Unit) {
    val selected = collections.find { it.id == selectedId }
    FilterMenuChip(
        label = if (selected == null) {
            stringResource(Res.string.library_collection_all)
        } else {
            stringResource(Res.string.library_collection_filter, selected.name)
        },
        selected = selected != null,
    ) { close ->
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.library_all_collections)) },
            onClick = { close(); onChange(null) },
        )
        collections.forEach { collection ->
            DropdownMenuItem(text = { Text(collection.name) }, onClick = { close(); onChange(collection.id) })
        }
    }
}

/** A chip with a dropdown menu; [items] gets a function that closes the menu. */
@Composable
private fun FilterMenuChip(label: String, selected: Boolean, items: @Composable (close: () -> Unit) -> Unit) {
    var isMenuOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryChip(label = label, selected = selected, onClick = { isMenuOpen = true })
        DropdownMenu(expanded = isMenuOpen, onDismissRequest = { isMenuOpen = false }) {
            items { isMenuOpen = false }
        }
    }
}
