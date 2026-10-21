package dev.gaens.filmory.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaens.filmory.feature.collection.domain.CollectionRepository
import dev.gaens.filmory.feature.episode.domain.EpisodeRepository
import dev.gaens.filmory.feature.title.domain.MoveWatchLater
import dev.gaens.filmory.feature.title.domain.TitleRepository
import dev.gaens.filmory.feature.title.domain.ToggleWatchLater
import dev.gaens.filmory.feature.watching.domain.FindContinueWatching
import dev.gaens.filmory.feature.watching.domain.WatchingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private const val AFTERNOON_STARTS_AT = 12
private const val EVENING_STARTS_AT = 18

class HomeViewModel(
    private val titleRepository: TitleRepository,
    private val watchingRepository: WatchingRepository,
    private val episodeRepository: EpisodeRepository,
    private val collectionRepository: CollectionRepository,
    private val findContinueWatching: FindContinueWatching,
    private val toggleWatchLater: ToggleWatchLater,
    private val moveWatchLater: MoveWatchLater,
    private val clock: Clock,
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(HomeState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadToday()
                observeData()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeState())

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnEditWatchLaterClick -> _state.update { it.copy(isEditingWatchLater = true) }
            HomeAction.OnWatchLaterEditorDismiss -> _state.update { it.copy(isEditingWatchLater = false) }
            is HomeAction.OnMoveWatchLater -> viewModelScope.launch {
                moveWatchLater(action.titleId, action.moveEarlier)
            }
            is HomeAction.OnRemoveWatchLater -> viewModelScope.launch { toggleWatchLater(action.titleId) }
            else -> Unit
        }
    }

    private fun loadToday() {
        val now = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dayPeriod = when {
            now.hour < AFTERNOON_STARTS_AT -> DayPeriod.MORNING
            now.hour < EVENING_STARTS_AT -> DayPeriod.AFTERNOON
            else -> DayPeriod.EVENING
        }
        _state.update { it.copy(dayPeriod = dayPeriod, today = now.date) }
    }

    private fun observeData() {
        val titles = titleRepository.observeTitles()
        val continueWatching = combine(titles, watchingRepository.observeAllWatchings(), findContinueWatching::invoke)
        combine(
            titles,
            continueWatching,
            episodeRepository.observeAllSeasons(),
            episodeRepository.observeAllChecks(),
            collectionRepository.observeCollections(),
            ::HomeData,
        )
            .onEach { data -> _state.update { it.withData(data) } }
            .launchIn(viewModelScope)
    }
}
