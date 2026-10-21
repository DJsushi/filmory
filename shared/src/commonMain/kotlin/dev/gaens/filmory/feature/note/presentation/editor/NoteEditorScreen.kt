package dev.gaens.filmory.feature.note.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.formatDateTime
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.core.presentation.ui.rememberImagePicker
import dev.gaens.filmory.core.presentation.ui.toLocalDate
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_cancel
import dev.gaens.filmory.resources.action_close
import dev.gaens.filmory.resources.action_done
import dev.gaens.filmory.resources.action_save
import dev.gaens.filmory.resources.detail_watching_number
import dev.gaens.filmory.resources.ic_close
import dev.gaens.filmory.resources.ic_image
import dev.gaens.filmory.resources.note_detail_image
import dev.gaens.filmory.resources.note_editor_add
import dev.gaens.filmory.resources.note_editor_add_image
import dev.gaens.filmory.resources.note_editor_clear
import dev.gaens.filmory.resources.note_editor_edit
import dev.gaens.filmory.resources.note_editor_episode
import dev.gaens.filmory.resources.note_editor_episode_number
import dev.gaens.filmory.resources.note_editor_new_watching
import dev.gaens.filmory.resources.note_editor_remove_image
import dev.gaens.filmory.resources.note_editor_season
import dev.gaens.filmory.resources.note_editor_season_number
import dev.gaens.filmory.resources.note_editor_text
import dev.gaens.filmory.resources.note_editor_timestamp
import dev.gaens.filmory.resources.note_editor_timestamp_error
import dev.gaens.filmory.resources.note_editor_timestamp_hint
import dev.gaens.filmory.resources.note_editor_type
import dev.gaens.filmory.resources.note_editor_watching
import dev.gaens.filmory.resources.note_editor_whole_series
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Instant

@Composable
fun NoteEditorScreenRoot(
    titleId: String?,
    episodeId: String?,
    noteId: String?,
    onClose: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel { parametersOf(titleId, episodeId, noteId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NoteEditorEvent.Close -> onClose()
            is NoteEditorEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        NoteEditorScreen(state, viewModel::onAction)
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    onAction: (NoteEditorAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        FilmoryAppBar(
            modifier = Modifier.padding(horizontal = 12.dp),
            navigationIcon = {
                FilmoryIconButton(Res.drawable.ic_close, stringResource(Res.string.action_close), {
                    onAction(NoteEditorAction.OnCloseClick)
                })
            },
            actions = {
                Text(
                    text = stringResource(if (state.isEditing) Res.string.note_editor_edit else Res.string.note_editor_add),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                Button(onClick = { onAction(NoteEditorAction.OnSaveClick) }, enabled = state.canSave) {
                    Text(stringResource(Res.string.action_save))
                }
            },
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            BigCover(state)
            if (state.watchingNumbers.isNotEmpty()) WatchingField(state, onAction)
            TypeField(state.noteType, onAction)
            if (state.titleType == TitleType.SERIES && state.seasons.isNotEmpty()) EpisodeFields(state, onAction)
            TimestampField(state, onAction)
            OutlinedTextField(
                value = state.text,
                onValueChange = { onAction(NoteEditorAction.OnTextChange(it)) },
                label = { Text(stringResource(Res.string.note_editor_text)) },
                minLines = 5,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            )
            ImageAndDate(state, onAction)
        }
    }
}

@Composable
private fun BigCover(state: NoteEditorState) {
    Column(Modifier.fillMaxWidth().rise(0), horizontalAlignment = Alignment.CenterHorizontally) {
        PosterArt(
            seed = state.titleId,
            name = state.titleName,
            posterUrl = state.titlePosterUrl,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.width(170.dp).softShadow(MaterialTheme.shapes.medium),
        )
        Text(state.titleName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 14.dp))
        Text(
            text = state.titleType.label(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
    )
}

@Composable
private fun WatchingField(state: NoteEditorState, onAction: (NoteEditorAction) -> Unit) {
    Column {
        FieldLabel(stringResource(Res.string.note_editor_watching))
        val selected = state.watchingNumbers.find { it.id == state.selectedWatchingId }
        DropdownField(
            value = selected?.let { stringResource(Res.string.detail_watching_number, it.number) }
                ?: stringResource(Res.string.note_editor_new_watching),
            options = state.watchingNumbers.map { stringResource(Res.string.detail_watching_number, it.number) },
            onSelect = { onAction(NoteEditorAction.OnWatchingChange(state.watchingNumbers[it].id)) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeField(selected: NoteType, onAction: (NoteEditorAction) -> Unit) {
    Column {
        FieldLabel(stringResource(Res.string.note_editor_type))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NoteType.entries.forEach { type ->
                FilmoryChip(
                    label = type.label(),
                    selected = type == selected,
                    showCheckWhenSelected = false,
                    onClick = { onAction(NoteEditorAction.OnTypeChange(type)) },
                )
            }
        }
    }
}

@Composable
private fun EpisodeFields(state: NoteEditorState, onAction: (NoteEditorAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            FieldLabel(stringResource(Res.string.note_editor_season))
            val wholeSeries = stringResource(Res.string.note_editor_whole_series)
            val seasonLabels = state.seasons.map { stringResource(Res.string.note_editor_season_number, it.number) }
            DropdownField(
                value = state.selectedSeasonNumber?.let { stringResource(Res.string.note_editor_season_number, it) } ?: wholeSeries,
                options = listOf(wholeSeries) + seasonLabels,
                onSelect = { index ->
                    onAction(NoteEditorAction.OnSeasonChange(if (index == 0) null else state.seasons[index - 1].number))
                },
            )
        }
        val season = state.seasons.find { it.number == state.selectedSeasonNumber }
        if (season != null) {
            Column(Modifier.weight(1f)) {
                FieldLabel(stringResource(Res.string.note_editor_episode))
                val episode = season.episodes.find { it.id == state.selectedEpisodeId }
                DropdownField(
                    value = episode?.let { stringResource(Res.string.note_editor_episode_number, it.number) }.orEmpty(),
                    options = season.episodes.map { option ->
                        listOfNotNull(stringResource(Res.string.note_editor_episode_number, option.number), option.name)
                            .joinToString(" · ")
                    },
                    onSelect = { onAction(NoteEditorAction.OnEpisodeChange(season.episodes[it].id)) },
                )
            }
        }
    }
}

/** A tonal button that opens a menu of [options]. */
@Composable
private fun DropdownField(value: String, options: List<String>, onSelect: (Int) -> Unit) {
    var isOpen by remember { mutableStateOf(false) }
    Box {
        FilledTonalButton(onClick = { isOpen = true }, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text(value, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        }
        DropdownMenu(expanded = isOpen, onDismissRequest = { isOpen = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        isOpen = false
                        onSelect(index)
                    },
                )
            }
        }
    }
}

@Composable
private fun TimestampField(state: NoteEditorState, onAction: (NoteEditorAction) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.timestampText,
            onValueChange = { onAction(NoteEditorAction.OnTimestampChange(it)) },
            label = { Text(stringResource(Res.string.note_editor_timestamp)) },
            placeholder = { Text(stringResource(Res.string.note_editor_timestamp_hint)) },
            isError = !state.isTimestampValid,
            supportingText = if (state.isTimestampValid) null else {
                { Text(stringResource(Res.string.note_editor_timestamp_error)) }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(1f),
        )
        if (state.timestampText.isNotEmpty()) {
            TextButton(onClick = { onAction(NoteEditorAction.OnTimestampChange("")) }) {
                Text(stringResource(Res.string.note_editor_clear))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageAndDate(state: NoteEditorState, onAction: (NoteEditorAction) -> Unit) {
    val pickImage = rememberImagePicker { onAction(NoteEditorAction.OnImagePicked(it)) }
    var isDatePickerOpen by remember { mutableStateOf(false) }
    if (state.imageUrl != null) {
        Box {
            AsyncImage(
                model = state.imageUrl,
                contentDescription = stringResource(Res.string.note_detail_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.medium),
            )
            FilmoryIconButton(
                icon = Res.drawable.ic_close,
                contentDescription = stringResource(Res.string.note_editor_remove_image),
                onClick = { onAction(NoteEditorAction.OnImageRemove) },
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.imageUrl == null) {
            FilledTonalButton(onClick = pickImage) {
                Icon(painterResource(Res.drawable.ic_image), null, Modifier.size(18.dp))
                Text(stringResource(Res.string.note_editor_add_image), Modifier.padding(start = 8.dp))
            }
        }
        FilledTonalButton(onClick = { isDatePickerOpen = true }) { Text(state.createdAt.formatDateTime()) }
    }
    if (isDatePickerOpen) {
        val initial = state.createdAt.toLocalDate().atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDatePickerOpen = false
                        pickerState.selectedDateMillis?.let {
                            onAction(NoteEditorAction.OnDateChange(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date))
                        }
                    },
                ) { Text(stringResource(Res.string.action_done)) }
            },
            dismissButton = { TextButton(onClick = { isDatePickerOpen = false }) { Text(stringResource(Res.string.action_cancel)) } },
        ) { DatePicker(pickerState) }
    }
}

@Preview
@Composable
private fun NoteEditorScreenPreview() {
    FilmoryTheme {
        NoteEditorScreen(
            state = NoteEditorState(titleId = "1", titleName = "Severance", titleType = TitleType.SERIES, text = "The elevator."),
            onAction = {},
        )
    }
}
