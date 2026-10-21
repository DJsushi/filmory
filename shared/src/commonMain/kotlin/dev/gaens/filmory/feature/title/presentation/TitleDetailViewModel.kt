package dev.gaens.filmory.feature.title.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.core.domain.IdGenerator
import dev.gaens.filmory.core.domain.util.DataError
import dev.gaens.filmory.core.domain.util.Result
import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.core.presentation.ui.asUiText
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.episode.domain.AdvanceEpisode
import dev.gaens.filmory.feature.episode.domain.EpisodeCheck
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.note.domain.NoteRepository
import dev.gaens.filmory.feature.title.domain.ChangeTitleStatus
import dev.gaens.filmory.feature.title.domain.ChangeTitleStatusError
import dev.gaens.filmory.feature.title.domain.DeleteTitle
import dev.gaens.filmory.feature.title.domain.Title
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.ToggleWatchLater
import dev.gaens.filmory.feature.watching.domain.DeleteWatching
import dev.gaens.filmory.feature.watching.domain.EnsureActiveWatching
import dev.gaens.filmory.feature.watching.domain.Rating
import dev.gaens.filmory.feature.watching.domain.SaveWatching
import dev.gaens.filmory.feature.watching.domain.SaveWatchingError
import dev.gaens.filmory.feature.watching.domain.StartWatching
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import dev.gaens.filmory.feature.watching.domain.withState
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.error_finish_before_start
import dev.gaens.filmory.resources.error_movie_cannot_be_watching
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock

@Suppress("LongParameterList")
class TitleDetailViewModel(
    private val titleId: String,
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val episodeRepository: EpisodeRepository,
    private val noteRepository: NoteRepository,
    private val collectionRepository: CollectionRepository,
    private val changeTitleStatus: ChangeTitleStatus,
    private val toggleWatchLater: ToggleWatchLater,
    private val startWatching: StartWatching,
    private val saveWatching: SaveWatching,
    private val deleteWatching: DeleteWatching,
    private val ensureActiveWatching: EnsureActiveWatching,
    private val advanceEpisode: AdvanceEpisode,
    private val deleteTitleWithImages: DeleteTitle,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : ViewModel() {

    private var hasLoadedInitialData = false
    private val data = MutableStateFlow<TitleDetailData?>(null)
    private val _state = MutableStateFlow(TitleDetailState())

    val state = combine(_state, data) { ui, data -> if (data == null) ui else ui.withData(data) }
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                observeData()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), TitleDetailState())

    private val eventChannel = Channel<TitleDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    @Suppress("CyclomaticComplexMethod")
    fun onAction(action: TitleDetailAction) {
        when (action) {
            TitleDetailAction.OnWishlistClick -> updateTitle { it.copy(isWishlisted = !it.isWishlisted) }
            TitleDetailAction.OnWatchLaterClick -> launchReporting { toggleWatchLater(titleId) }
            TitleDetailAction.OnDeleteTitleClick -> openSheet(TitleDetailSheet.DeleteTitle)
            TitleDetailAction.OnDeleteTitleConfirm -> deleteTitle()
            TitleDetailAction.OnOverviewClick -> _state.update { it.copy(isOverviewExpanded = !it.isOverviewExpanded) }
            is TitleDetailAction.OnStatusChange -> changeStatus(action.status)
            TitleDetailAction.OnCollectionPickerClick -> openSheet(TitleDetailSheet.CollectionPicker)
            is TitleDetailAction.OnCollectionToggle -> toggleCollection(action.collectionId)
            TitleDetailAction.OnAddWatchingClick -> launchReporting { startWatching(titleId) }
            is TitleDetailAction.OnWatchingClick -> editWatching(action.watchingId)
            is TitleDetailAction.OnDraftStateChange -> updateDraft { it.withState(action.state, clock.now()) }
            is TitleDetailAction.OnDraftStartDateChange -> updateDraft { it.copy(startedAt = action.date?.toInstant()) }
            is TitleDetailAction.OnDraftFinishDateChange -> updateDraft { it.copy(finishedAt = action.date?.toInstant()) }
            is TitleDetailAction.OnDraftRatingChange -> updateDraft { it.copy(rating = action.halfStars?.let(::Rating)) }
            is TitleDetailAction.OnDraftReviewChange -> updateDraft { it.copy(review = action.review.ifBlank { null }) }
            TitleDetailAction.OnDraftSave -> saveDraft()
            TitleDetailAction.OnDraftDeleteClick -> draft()?.let { openSheet(TitleDetailSheet.DeleteWatching(it.id)) }
            TitleDetailAction.OnDeleteWatchingConfirm -> deleteDraftWatching()
            TitleDetailAction.OnNextEpisodeClick -> withActiveWatching { advanceEpisode(it.id) }
            is TitleDetailAction.OnEpisodeCheckClick -> toggleEpisodeCheck(action.episodeId)
            is TitleDetailAction.OnEpisodeClick -> withActiveWatching {
                watchingRepository.upsertWatching(it.copy(currentEpisodeId = action.episodeId))
            }
            is TitleDetailAction.OnSeasonClick -> _state.update { it.copy(selectedSeasonNumber = action.seasonNumber) }
            TitleDetailAction.OnMarkSeasonClick -> markSelectedSeason()
            is TitleDetailAction.OnNoteWatchingFilterChange ->
                _state.update { it.copy(noteWatchingFilter = action.watchingId) }
            is TitleDetailAction.OnNoteEpisodeFilterChange -> _state.update { it.copy(noteEpisodeFilter = action.filter) }
            is TitleDetailAction.OnNoteFavoriteClick -> toggleNoteFavorite(action.noteId)
            TitleDetailAction.OnSheetDismiss -> _state.update { it.copy(sheet = null) }
            else -> Unit
        }
    }

    private fun observeData() {
        val titleData = combine(
            titleRepository.observeTitle(titleId).filterNotNull(),
            watchingRepository.observeWatchings(titleId),
            episodeRepository.observeSeasons(titleId),
            episodeRepository.observeAllChecks(),
            noteRepository.observeNotes(titleId),
        ) { title, watchings, seasons, checks, notes -> TitleDetailData(title, watchings, seasons, checks, notes, emptyList()) }
        combine(titleData, collectionRepository.observeCollections()) { detail, collections ->
            detail.copy(collections = collections)
        }
            .onEach { data.value = it }
            .launchIn(viewModelScope)
    }

    private fun openSheet(sheet: TitleDetailSheet) = _state.update { it.copy(sheet = sheet) }

    private fun draft(): Watching? = (_state.value.sheet as? TitleDetailSheet.EditWatching)?.draft

    private fun updateDraft(transform: (Watching) -> Watching) {
        val draft = draft() ?: return
        openSheet(TitleDetailSheet.EditWatching(transform(draft)))
    }

    private fun editWatching(watchingId: String) {
        val watching = data.value?.watchings?.find { it.id == watchingId } ?: return
        openSheet(TitleDetailSheet.EditWatching(watching))
    }

    private fun saveDraft() {
        val draft = draft() ?: return
        viewModelScope.launch {
            when (val result = saveWatching(draft)) {
                is Result.Success -> _state.update { it.copy(sheet = null) }
                is Result.Error -> showMessage(
                    when (val error = result.error) {
                        SaveWatchingError.FinishBeforeStart -> UiText.Resource(Res.string.error_finish_before_start)
                        is SaveWatchingError.Storage -> error.error.asUiText()
                    },
                )
            }
        }
    }

    private fun deleteDraftWatching() {
        val sheet = _state.value.sheet as? TitleDetailSheet.DeleteWatching ?: return
        _state.update { it.copy(sheet = null) }
        launchReporting { deleteWatching(sheet.watchingId) }
    }

    private fun changeStatus(status: TitleStatus) {
        viewModelScope.launch {
            val result = changeTitleStatus(titleId, status)
            if (result is Result.Error) {
                showMessage(
                    when (val error = result.error) {
                        ChangeTitleStatusError.MovieCannotBeWatching ->
                            UiText.Resource(Res.string.error_movie_cannot_be_watching)
                        is ChangeTitleStatusError.Storage -> error.error.asUiText()
                    },
                )
            }
        }
    }

    private fun updateTitle(transform: (Title) -> Title) {
        val title = data.value?.title ?: return
        launchReporting { titleRepository.upsertTitle(transform(title).copy(updatedAt = clock.now())) }
    }

    private fun deleteTitle() {
        _state.update { it.copy(sheet = null) }
        viewModelScope.launch {
            when (val result = deleteTitleWithImages(titleId)) {
                is Result.Success -> eventChannel.send(TitleDetailEvent.TitleDeleted)
                is Result.Error -> showMessage(result.error.asUiText())
            }
        }
    }

    private fun toggleCollection(collectionId: String) {
        val collection = data.value?.collections?.find { it.id == collectionId } ?: return
        launchReporting {
            if (titleId in collection.titleIds) {
                collectionRepository.removeTitle(collectionId, titleId)
            } else {
                collectionRepository.addTitle(collectionId, titleId)
            }
        }
    }

    private fun toggleEpisodeCheck(episodeId: String) {
        withActiveWatching { watching ->
            val isChecked = data.value?.checks?.any { it.watchingId == watching.id && it.episodeId == episodeId } == true
            if (isChecked) {
                episodeRepository.uncheckEpisode(watching.id, episodeId)
            } else {
                episodeRepository.checkEpisode(EpisodeCheck(idGenerator.newId(), watching.id, episodeId, clock.now()))
            }
        }
    }

    private fun markSelectedSeason() {
        val season = state.value.series?.seasons?.find { it.number == state.value.selectedSeasonNumber } ?: return
        withActiveWatching { watching ->
            season.episodes.filterNot { it.isChecked }.forEach { episode ->
                val checked = episodeRepository.checkEpisode(EpisodeCheck(idGenerator.newId(), watching.id, episode.id, clock.now()))
                if (checked is Result.Error) return@withActiveWatching checked
            }
            Result.Success(Unit)
        }
    }

    private fun toggleNoteFavorite(noteId: String) {
        val note = data.value?.notes?.find { it.id == noteId } ?: return
        launchReporting { noteRepository.upsertNote(note.copy(isFavorite = !note.isFavorite)) }
    }

    /** Runs an episode action on the current watching, starting one first when needed. */
    private fun withActiveWatching(block: suspend (Watching) -> Result<*, DataError.Local>) {
        launchReporting {
            when (val active = ensureActiveWatching(titleId)) {
                is Result.Error -> active
                is Result.Success -> block(active.data)
            }
        }
    }

    private fun launchReporting(block: suspend () -> Result<*, DataError.Local>) {
        viewModelScope.launch {
            val result = block()
            if (result is Result.Error) showMessage(result.error.asUiText())
        }
    }

    private suspend fun showMessage(message: UiText) = eventChannel.send(TitleDetailEvent.ShowMessage(message))

    private fun LocalDate.toInstant() = atStartOfDayIn(TimeZone.currentSystemDefault())
}
