package dev.gaens.filmory.feature.title.presentation

import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.watching.domain.WatchingState
import kotlinx.datetime.LocalDate

sealed interface TitleDetailAction {
    data object OnBackClick : TitleDetailAction
    data object OnWishlistClick : TitleDetailAction
    data object OnWatchLaterClick : TitleDetailAction
    data object OnDeleteTitleClick : TitleDetailAction
    data object OnDeleteTitleConfirm : TitleDetailAction
    data object OnOverviewClick : TitleDetailAction
    data class OnStatusChange(val status: TitleStatus) : TitleDetailAction

    data object OnCollectionPickerClick : TitleDetailAction
    data class OnCollectionToggle(val collectionId: String) : TitleDetailAction

    data object OnAddWatchingClick : TitleDetailAction
    data class OnWatchingClick(val watchingId: String) : TitleDetailAction
    data class OnDraftStateChange(val state: WatchingState) : TitleDetailAction
    data class OnDraftStartDateChange(val date: LocalDate?) : TitleDetailAction
    data class OnDraftFinishDateChange(val date: LocalDate?) : TitleDetailAction
    data class OnDraftRatingChange(val halfStars: Int?) : TitleDetailAction
    data class OnDraftReviewChange(val review: String) : TitleDetailAction
    data object OnDraftSave : TitleDetailAction
    data object OnDraftDeleteClick : TitleDetailAction
    data object OnDeleteWatchingConfirm : TitleDetailAction

    data object OnNextEpisodeClick : TitleDetailAction
    data class OnEpisodeCheckClick(val episodeId: String) : TitleDetailAction
    data class OnEpisodeClick(val episodeId: String) : TitleDetailAction
    data class OnSeasonClick(val seasonNumber: Int) : TitleDetailAction
    data object OnMarkSeasonClick : TitleDetailAction

    data class OnNoteWatchingFilterChange(val watchingId: String?) : TitleDetailAction
    data class OnNoteEpisodeFilterChange(val filter: NoteEpisodeFilter) : TitleDetailAction
    data class OnNoteClick(val noteId: String) : TitleDetailAction
    data class OnNoteFavoriteClick(val noteId: String) : TitleDetailAction
    data class OnNoteCopyClick(val text: String) : TitleDetailAction
    data object OnAddNoteClick : TitleDetailAction

    data object OnSheetDismiss : TitleDetailAction
}

sealed interface TitleDetailEvent {
    data object TitleDeleted : TitleDetailEvent
    data class ShowMessage(val message: UiText) : TitleDetailEvent
}
