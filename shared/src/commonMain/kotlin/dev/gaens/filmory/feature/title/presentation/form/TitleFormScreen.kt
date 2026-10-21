package dev.gaens.filmory.feature.title.presentation.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.FilmorySegmentedRow
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.title.domain.TitleFormError
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_close
import dev.gaens.filmory.resources.action_save
import dev.gaens.filmory.resources.ic_close
import dev.gaens.filmory.resources.title_form_add
import dev.gaens.filmory.resources.title_form_add_season
import dev.gaens.filmory.resources.title_form_creator
import dev.gaens.filmory.resources.title_form_director
import dev.gaens.filmory.resources.title_form_edit
import dev.gaens.filmory.resources.title_form_error_episodes
import dev.gaens.filmory.resources.title_form_error_name
import dev.gaens.filmory.resources.title_form_error_runtime
import dev.gaens.filmory.resources.title_form_error_seasons
import dev.gaens.filmory.resources.title_form_error_year
import dev.gaens.filmory.resources.title_form_genre
import dev.gaens.filmory.resources.title_form_name
import dev.gaens.filmory.resources.title_form_overview
import dev.gaens.filmory.resources.title_form_remove_season
import dev.gaens.filmory.resources.title_form_runtime
import dev.gaens.filmory.resources.title_form_season_episodes
import dev.gaens.filmory.resources.title_form_seasons
import dev.gaens.filmory.resources.title_form_year
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TitleFormScreenRoot(
    titleId: String?,
    onClose: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: TitleFormViewModel = koinViewModel { parametersOf(titleId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is TitleFormEvent.Saved -> onSaved(event.titleId)
            is TitleFormEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        TitleFormScreen(
            state = state,
            onAction = { action ->
                if (action == TitleFormAction.OnCloseClick) onClose()
                viewModel.onAction(action)
            },
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

@Composable
fun TitleFormScreen(
    state: TitleFormState,
    onAction: (TitleFormAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        FilmoryAppBar(
            modifier = Modifier.padding(horizontal = 12.dp),
            navigationIcon = {
                FilmoryIconButton(Res.drawable.ic_close, stringResource(Res.string.action_close), {
                    onAction(TitleFormAction.OnCloseClick)
                })
            },
            actions = {
                Text(
                    text = stringResource(if (state.isEditing) Res.string.title_form_edit else Res.string.title_form_add),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                Button(onClick = { onAction(TitleFormAction.OnSaveClick) }, enabled = !state.isSaving) {
                    Text(stringResource(Res.string.action_save))
                }
            },
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (!state.isEditing) {
                val types = TitleType.entries
                FilmorySegmentedRow(
                    options = types.map { it.label() },
                    selectedIndex = types.indexOf(state.type),
                    onSelect = { onAction(TitleFormAction.OnTypeChange(types[it])) },
                )
            }
            FormField(
                value = state.name,
                label = Res.string.title_form_name,
                error = Res.string.title_form_error_name.takeIf { TitleFormError.BLANK_NAME in state.errors },
                capitalization = KeyboardCapitalization.Words,
            ) { onAction(TitleFormAction.OnNameChange(it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField(
                    value = state.year,
                    label = Res.string.title_form_year,
                    error = Res.string.title_form_error_year.takeIf { TitleFormError.INVALID_YEAR in state.errors },
                    isNumber = true,
                    modifier = Modifier.weight(1f),
                ) { onAction(TitleFormAction.OnYearChange(it)) }
                FormField(
                    value = state.genre,
                    label = Res.string.title_form_genre,
                    capitalization = KeyboardCapitalization.Sentences,
                    modifier = Modifier.weight(1f),
                ) { onAction(TitleFormAction.OnGenreChange(it)) }
            }
            FormField(
                value = state.creator,
                label = if (state.type == TitleType.MOVIE) Res.string.title_form_director else Res.string.title_form_creator,
                capitalization = KeyboardCapitalization.Words,
            ) { onAction(TitleFormAction.OnCreatorChange(it)) }
            if (state.type == TitleType.MOVIE) {
                FormField(
                    value = state.runtime,
                    label = Res.string.title_form_runtime,
                    error = Res.string.title_form_error_runtime.takeIf { TitleFormError.INVALID_RUNTIME in state.errors },
                    isNumber = true,
                ) { onAction(TitleFormAction.OnRuntimeChange(it)) }
            }
            OutlinedTextField(
                value = state.overview,
                onValueChange = { onAction(TitleFormAction.OnOverviewChange(it)) },
                label = { Text(stringResource(Res.string.title_form_overview)) },
                minLines = 3,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.type == TitleType.SERIES) SeasonsEditor(state, onAction)
        }
    }
}

@Composable
private fun SeasonsEditor(state: TitleFormState, onAction: (TitleFormAction) -> Unit) {
    Text(stringResource(Res.string.title_form_seasons), style = MaterialTheme.typography.headlineSmall)
    val error = when {
        TitleFormError.NO_SEASONS in state.errors -> Res.string.title_form_error_seasons
        TitleFormError.INVALID_EPISODE_COUNT in state.errors -> Res.string.title_form_error_episodes
        else -> null
    }
    state.episodesPerSeason.forEachIndexed { index, count ->
        val label = stringResource(Res.string.title_form_season_episodes, index + 1)
        OutlinedTextField(
            value = count,
            onValueChange = { onAction(TitleFormAction.OnEpisodeCountChange(index, it)) },
            label = { Text(label) },
            isError = error != null && count.trim().toIntOrNull()?.let { it in 1..999 } != true,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (error != null) Text(stringResource(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = { onAction(TitleFormAction.OnAddSeasonClick) }) {
            Text(stringResource(Res.string.title_form_add_season))
        }
        if (state.episodesPerSeason.size > 1) {
            TextButton(onClick = { onAction(TitleFormAction.OnRemoveSeasonClick) }) {
                Text(stringResource(Res.string.title_form_remove_season))
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    label: StringResource,
    modifier: Modifier = Modifier,
    error: StringResource? = null,
    isNumber: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text,
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview
@Composable
private fun TitleFormScreenPreview() {
    FilmoryTheme { TitleFormScreen(TitleFormState(type = TitleType.SERIES, episodesPerSeason = listOf("8", "")), onAction = {}) }
}
