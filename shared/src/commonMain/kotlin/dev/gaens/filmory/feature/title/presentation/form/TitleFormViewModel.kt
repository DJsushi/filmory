package dev.gaens.filmory.feature.title.presentation.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.presentation.ui.asUiText
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.title.domain.SaveTitleForm
import dev.gaens.filmory.feature.title.domain.SaveTitleFormError
import dev.gaens.filmory.feature.title.domain.TitleFormError
import dev.gaens.filmory.feature.title.domain.TitleRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_SEASONS = 100

/** Adds a title by hand when [titleId] is null, otherwise edits that title. */
class TitleFormViewModel(
    private val titleId: String?,
    private val titleRepository: TitleRepository,
    private val episodeRepository: EpisodeRepository,
    private val saveTitleForm: SaveTitleForm,
) : ViewModel() {

    private val _state = MutableStateFlow(TitleFormState(isEditing = titleId != null))
    val state = _state.asStateFlow()

    private val eventChannel = Channel<TitleFormEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        if (titleId != null) viewModelScope.launch { load(titleId) }
    }

    fun onAction(action: TitleFormAction) {
        when (action) {
            is TitleFormAction.OnTypeChange -> if (!_state.value.isEditing) _state.update { it.copy(type = action.type) }
            is TitleFormAction.OnNameChange -> _state.update { it.copy(name = action.value) }
            is TitleFormAction.OnYearChange -> _state.update { it.copy(year = action.value) }
            is TitleFormAction.OnGenreChange -> _state.update { it.copy(genre = action.value) }
            is TitleFormAction.OnCreatorChange -> _state.update { it.copy(creator = action.value) }
            is TitleFormAction.OnRuntimeChange -> _state.update { it.copy(runtime = action.value) }
            is TitleFormAction.OnOverviewChange -> _state.update { it.copy(overview = action.value) }
            is TitleFormAction.OnEpisodeCountChange -> _state.update { state ->
                state.copy(episodesPerSeason = state.episodesPerSeason.toMutableList().also { it[action.seasonIndex] = action.value })
            }
            TitleFormAction.OnAddSeasonClick -> _state.update { state ->
                if (state.episodesPerSeason.size >= MAX_SEASONS) state else state.copy(episodesPerSeason = state.episodesPerSeason + "")
            }
            TitleFormAction.OnRemoveSeasonClick -> _state.update { state ->
                if (state.episodesPerSeason.size <= 1) state else state.copy(episodesPerSeason = state.episodesPerSeason.dropLast(1))
            }
            TitleFormAction.OnSaveClick -> save()
            TitleFormAction.OnCloseClick -> Unit
        }
    }

    private suspend fun load(id: String) {
        val title = titleRepository.getTitle(id) ?: return
        val seasons = episodeRepository.getSeasons(id)
        _state.update {
            it.copy(
                type = title.type,
                name = title.name,
                year = title.year?.toString().orEmpty(),
                genre = title.genre.orEmpty(),
                creator = title.creator.orEmpty(),
                runtime = title.runtimeMinutes?.toString().orEmpty(),
                overview = title.overview.orEmpty(),
                episodesPerSeason = seasons.map { season -> season.episodes.size.toString() }.ifEmpty { listOf("") },
            )
        }
    }

    private fun save() {
        val state = _state.value
        if (state.isSaving) return
        val unparsed = buildSet {
            if (state.year.isNotBlank() && state.year.trim().toIntOrNull() == null) add(TitleFormError.INVALID_YEAR)
            if (state.runtime.isNotBlank() && state.runtime.trim().toIntOrNull() == null) add(TitleFormError.INVALID_RUNTIME)
        }
        val errors = saveTitleForm.validate(state.toForm()) + unparsed
        if (errors.isNotEmpty()) return _state.update { it.copy(errors = errors) }

        _state.update { it.copy(isSaving = true, errors = emptySet()) }
        viewModelScope.launch {
            when (val result = saveTitleForm(state.toForm(), titleId)) {
                is Result.Success -> eventChannel.send(TitleFormEvent.Saved(result.data))
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    when (val error = result.error) {
                        is SaveTitleFormError.Invalid -> _state.update { it.copy(errors = error.errors) }
                        is SaveTitleFormError.Storage -> eventChannel.send(TitleFormEvent.ShowMessage(error.error.asUiText()))
                    }
                }
            }
        }
    }
}
