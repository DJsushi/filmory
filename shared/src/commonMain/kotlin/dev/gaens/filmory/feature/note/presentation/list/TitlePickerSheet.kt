package dev.gaens.filmory.feature.note.presentation.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ChipRow
import dev.gaens.filmory.core.presentation.designsystem.components.TitleListRow
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.library.presentation.components.CollectionFilterChip
import dev.gaens.filmory.feature.library.presentation.components.StatusFilterChip
import dev.gaens.filmory.feature.library.presentation.components.TitleSearchField
import dev.gaens.filmory.feature.library.presentation.components.TypeFilterChips
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.notes_pick_title
import dev.gaens.filmory.resources.notes_pick_title_empty
import dev.gaens.filmory.resources.notes_pick_title_no_match
import org.jetbrains.compose.resources.stringResource

private const val SHEET_HEIGHT_FRACTION = 0.9f

/**
 * "Add a note to": every title, with the library's search and filters to find the right one.
 * The search starts unfocused so the sheet does not open with the keyboard over the list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitlePickerSheet(state: NotesState, onAction: (NotesAction) -> Unit) {
    val filters = state.pickerFilters
    ModalBottomSheet(
        onDismissRequest = { onAction(NotesAction.OnTitlePickerDismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.fillMaxHeight(SHEET_HEIGHT_FRACTION).imePadding()) {
            Text(
                text = stringResource(Res.string.notes_pick_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp),
            )
            if (!state.hasTitles) {
                EmptyMessage(stringResource(Res.string.notes_pick_title_empty))
                return@Column
            }
            TitleSearchField(
                text = filters.searchText,
                onTextChange = { onAction(NotesAction.OnPickerSearchChange(it)) },
                onClear = if (filters.searchText.isEmpty()) null else ({ onAction(NotesAction.OnPickerSearchChange("")) }),
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp),
            )
            ChipRow(Modifier.padding(vertical = 8.dp)) {
                StatusFilterChip(filters.status) { onAction(NotesAction.OnPickerStatusChange(it)) }
                TypeFilterChips(filters.type) { onAction(NotesAction.OnPickerTypeClick(it)) }
                CollectionFilterChip(state.pickerCollections, filters.collectionId) {
                    onAction(NotesAction.OnPickerCollectionChange(it))
                }
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 24.dp)) {
                if (state.titles.isEmpty()) {
                    item(key = "noMatch") { EmptyMessage(stringResource(Res.string.notes_pick_title_no_match)) }
                }
                items(state.titles, key = { it.id }) { title ->
                    TitleListRow(
                        id = title.id,
                        name = title.name,
                        details = listOfNotNull(title.type.label(), title.year?.toString()).joinToString(" · "),
                        posterUrl = title.posterUrl,
                        status = title.status,
                        onClick = { onAction(NotesAction.OnTitlePicked(title.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 16.dp),
    )
}
