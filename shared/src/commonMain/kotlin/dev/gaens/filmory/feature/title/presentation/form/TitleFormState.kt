package dev.gaens.filmory.feature.title.presentation.form

import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.feature.title.domain.TitleForm
import dev.gaens.filmory.feature.title.domain.TitleFormError
import dev.gaens.filmory.feature.title.domain.TitleType

data class TitleFormState(
    val isEditing: Boolean = false,
    val type: TitleType = TitleType.MOVIE,
    val name: String = "",
    val year: String = "",
    val genre: String = "",
    val creator: String = "",
    val runtime: String = "",
    val overview: String = "",
    val episodesPerSeason: List<String> = listOf(""),
    val errors: Set<TitleFormError> = emptySet(),
    val isSaving: Boolean = false,
) {
    fun toForm() = TitleForm(
        type = type,
        name = name,
        year = year.trim().toIntOrNull(),
        genre = genre,
        creator = creator,
        runtimeMinutes = runtime.trim().toIntOrNull(),
        overview = overview,
        // Unparseable counts become 0 so validation reports them.
        episodesPerSeason = episodesPerSeason.map { it.trim().toIntOrNull() ?: 0 },
    )
}

sealed interface TitleFormAction {
    data object OnCloseClick : TitleFormAction
    data object OnSaveClick : TitleFormAction
    data class OnTypeChange(val type: TitleType) : TitleFormAction
    data class OnNameChange(val value: String) : TitleFormAction
    data class OnYearChange(val value: String) : TitleFormAction
    data class OnGenreChange(val value: String) : TitleFormAction
    data class OnCreatorChange(val value: String) : TitleFormAction
    data class OnRuntimeChange(val value: String) : TitleFormAction
    data class OnOverviewChange(val value: String) : TitleFormAction
    data class OnEpisodeCountChange(val seasonIndex: Int, val value: String) : TitleFormAction
    data object OnAddSeasonClick : TitleFormAction
    data object OnRemoveSeasonClick : TitleFormAction
}

sealed interface TitleFormEvent {
    data class Saved(val titleId: String) : TitleFormEvent
    data class ShowMessage(val message: UiText) : TitleFormEvent
}
