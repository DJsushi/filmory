package dev.gaens.filmory.feature.title.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaens.filmory.core.presentation.richtext.MarkdownText
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ChipRow
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryCard
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryChip
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryFab
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.FilmorySegmentedRow
import dev.gaens.filmory.core.presentation.designsystem.components.HeroHeader
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.components.StarRating
import dev.gaens.filmory.core.presentation.designsystem.components.StatusBadge
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.formatDate
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.collection.presentation.picker.CollectionPickerSheet
import dev.gaens.filmory.feature.note.presentation.components.NoteCard
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_back
import dev.gaens.filmory.resources.action_delete
import dev.gaens.filmory.resources.action_more
import dev.gaens.filmory.resources.detail_add_collection
import dev.gaens.filmory.resources.detail_add_note
import dev.gaens.filmory.resources.detail_add_watching
import dev.gaens.filmory.resources.detail_all_episodes
import dev.gaens.filmory.resources.detail_all_watchings
import dev.gaens.filmory.resources.detail_created_by
import dev.gaens.filmory.resources.detail_delete_title
import dev.gaens.filmory.resources.detail_delete_title_message
import dev.gaens.filmory.resources.detail_directed_by
import dev.gaens.filmory.resources.detail_edit_title
import dev.gaens.filmory.resources.detail_no_notes
import dev.gaens.filmory.resources.detail_notes
import dev.gaens.filmory.resources.detail_runtime
import dev.gaens.filmory.resources.detail_runtime_minutes
import dev.gaens.filmory.resources.detail_season_count
import dev.gaens.filmory.resources.detail_season_episode_short
import dev.gaens.filmory.resources.detail_series_notes
import dev.gaens.filmory.resources.detail_watch_later_add
import dev.gaens.filmory.resources.detail_watch_later_remove
import dev.gaens.filmory.resources.detail_watching_not_started
import dev.gaens.filmory.resources.detail_watching_number
import dev.gaens.filmory.resources.detail_watchings
import dev.gaens.filmory.resources.detail_wishlist_add
import dev.gaens.filmory.resources.detail_wishlist_remove
import dev.gaens.filmory.resources.ic_add
import dev.gaens.filmory.resources.ic_back
import dev.gaens.filmory.resources.ic_clock
import dev.gaens.filmory.resources.ic_folder
import dev.gaens.filmory.resources.ic_heart
import dev.gaens.filmory.resources.ic_heart_filled
import dev.gaens.filmory.resources.ic_more
import dev.gaens.filmory.resources.ic_watched
import dev.gaens.filmory.resources.note_count
import dev.gaens.filmory.resources.note_copied
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val MINUTES_PER_HOUR = 60

@Composable
fun TitleDetailScreenRoot(
    titleId: String,
    onBackClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onAddNoteClick: (episodeId: String?) -> Unit,
    onEditTitleClick: () -> Unit,
    viewModel: TitleDetailViewModel = koinViewModel { parametersOf(titleId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TitleDetailEvent.TitleDeleted -> onBackClick()
            is TitleDetailEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asStringAsync())
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        TitleDetailScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    TitleDetailAction.OnBackClick -> onBackClick()
                    is TitleDetailAction.OnNoteClick -> onNoteClick(action.noteId)
                    TitleDetailAction.OnAddNoteClick -> onAddNoteClick(state.currentEpisodeIdForNotes())
                    is TitleDetailAction.OnNoteCopyClick -> {
                        clipboard.setText(AnnotatedString(action.text))
                        scope.launch { snackbarHostState.showSnackbar(getString(Res.string.note_copied)) }
                    }
                    else -> Unit
                }
                viewModel.onAction(action)
            },
            onEditTitleClick = onEditTitleClick,
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

/** New notes on a series default to the episode the user is on. */
private fun TitleDetailState.currentEpisodeIdForNotes(): String? =
    series?.seasons?.flatMap { it.episodes }?.find { it.isCurrent }?.id

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TitleDetailScreen(
    state: TitleDetailState,
    onAction: (TitleDetailAction) -> Unit,
    onEditTitleClick: () -> Unit = {},
) {
    val title = state.title ?: return
    // The shared transition scope lets the "You are here" badge glide from one episode row to the next.
    SharedTransitionLayout {
        val sharedScope = this
        Box(Modifier.fillMaxSize()) {
            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), modifier = Modifier.fillMaxSize()) {
                item(key = "hero") { DetailHero(title, onAction, onEditTitleClick) }
                item(key = "status") { StatusSelector(title, onAction) }
                item(key = "overview") { Overview(title.overview, state.isOverviewExpanded, onAction) }
                item(key = "collections") { CollectionChips(state.collections, onAction) }
                if (title.type == TitleType.SERIES) seriesSection(state, sharedScope, onAction)
                watchingsSection(state, onAction)
                notesSection(state, onAction)
            }
            FilmoryFab(
                text = stringResource(Res.string.detail_add_note),
                onClick = { onAction(TitleDetailAction.OnAddNoteClick) },
                modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(16.dp),
            )
        }
    }
    TitleDetailSheets(state, onAction)
}

@Composable
private fun DetailHero(title: TitleDetailUi, onAction: (TitleDetailAction) -> Unit, onEditTitleClick: () -> Unit) {
    HeroHeader {
        FilmoryAppBar(
            navigationIcon = {
                FilmoryIconButton(Res.drawable.ic_back, stringResource(Res.string.action_back), {
                    onAction(TitleDetailAction.OnBackClick)
                })
            },
            actions = {
                FilmoryIconButton(
                    icon = if (title.isWatchLater) Res.drawable.ic_watched else Res.drawable.ic_clock,
                    contentDescription = stringResource(
                        if (title.isWatchLater) Res.string.detail_watch_later_remove else Res.string.detail_watch_later_add,
                    ),
                    onClick = { onAction(TitleDetailAction.OnWatchLaterClick) },
                )
                FilmoryIconButton(
                    icon = if (title.isWishlisted) Res.drawable.ic_heart_filled else Res.drawable.ic_heart,
                    contentDescription = stringResource(
                        if (title.isWishlisted) Res.string.detail_wishlist_remove else Res.string.detail_wishlist_add,
                    ),
                    onClick = { onAction(TitleDetailAction.OnWishlistClick) },
                )
                MoreMenu(onAction, onEditTitleClick)
            },
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            PosterArt(title.id, title.name, title.posterUrl, Modifier.width(112.dp).rise(0))
            Column(Modifier.weight(1f).rise(1), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title.name, style = MaterialTheme.typography.headlineMedium)
                Text(title.factsLine(), style = MaterialTheme.typography.bodyLarge)
                if (title.creator != null) {
                    Text(
                        text = stringResource(
                            if (title.type == TitleType.MOVIE) Res.string.detail_directed_by else Res.string.detail_created_by,
                            title.creator,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                StatusBadge(title.status)
            }
        }
    }
}

@Composable
private fun TitleDetailUi.factsLine(): String = listOfNotNull(
    year?.toString(),
    genre,
    when (type) {
        TitleType.MOVIE -> runtimeMinutes?.let { minutes ->
            if (minutes >= MINUTES_PER_HOUR) {
                stringResource(Res.string.detail_runtime, minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)
            } else {
                stringResource(Res.string.detail_runtime_minutes, minutes)
            }
        }
        TitleType.SERIES -> pluralStringResource(Res.plurals.detail_season_count, seasonCount, seasonCount)
    },
).joinToString(" · ")

@Composable
private fun MoreMenu(onAction: (TitleDetailAction) -> Unit, onEditTitleClick: () -> Unit) {
    var isOpen by remember { mutableStateOf(false) }
    Box {
        FilmoryIconButton(Res.drawable.ic_more, stringResource(Res.string.action_more), { isOpen = true })
        DropdownMenu(expanded = isOpen, onDismissRequest = { isOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.detail_edit_title)) },
                onClick = {
                    isOpen = false
                    onEditTitleClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.detail_delete_title)) },
                onClick = {
                    isOpen = false
                    onAction(TitleDetailAction.OnDeleteTitleClick)
                },
            )
        }
    }
}

@Composable
private fun StatusSelector(title: TitleDetailUi, onAction: (TitleDetailAction) -> Unit) {
    val options = TitleStatus.entries.filter { title.type == TitleType.SERIES || it != TitleStatus.WATCHING }
    FilmorySegmentedRow(
        options = options.map { it.label() },
        selectedIndex = options.indexOf(title.status),
        onSelect = { onAction(TitleDetailAction.OnStatusChange(options[it])) },
        modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 22.dp).rise(2),
    )
}

@Composable
private fun Overview(overview: String?, isExpanded: Boolean, onAction: (TitleDetailAction) -> Unit) {
    if (overview.isNullOrBlank()) return
    MarkdownText(
        text = overview,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp)
            .clickable { onAction(TitleDetailAction.OnOverviewClick) }
            .rise(3),
    )
}

@Composable
private fun CollectionChips(collections: List<CollectionToggleUi>, onAction: (TitleDetailAction) -> Unit) {
    ChipRow(Modifier.padding(top = 12.dp).rise(4)) {
        collections.filter { it.containsTitle }.forEach { collection ->
            FilmoryChip(
                label = collection.name,
                selected = false,
                icon = Res.drawable.ic_folder,
                onClick = { onAction(TitleDetailAction.OnCollectionPickerClick) },
            )
        }
        FilmoryChip(
            label = stringResource(Res.string.detail_add_collection),
            selected = false,
            onClick = { onAction(TitleDetailAction.OnCollectionPickerClick) },
        )
    }
}

private fun LazyListScope.watchingsSection(state: TitleDetailState, onAction: (TitleDetailAction) -> Unit) {
    item(key = "watchingsHeader") {
        SectionHeader(
            title = stringResource(Res.string.detail_watchings),
            action = {
                FilledTonalButton(onClick = { onAction(TitleDetailAction.OnAddWatchingClick) }) {
                    Icon(painterResource(Res.drawable.ic_add), null, Modifier.size(18.dp))
                    Text(stringResource(Res.string.detail_add_watching), Modifier.padding(start = 8.dp))
                }
            },
        )
    }
    items(state.watchings, key = { "watching-${it.id}" }) { watching ->
        WatchingCard(watching, { onAction(TitleDetailAction.OnWatchingClick(watching.id)) })
    }
}

@Composable
private fun WatchingCard(watching: WatchingUi, onClick: () -> Unit) {
    FilmoryCard(
        modifier = Modifier.fillMaxWidth().padding(start = ScreenPadding, end = ScreenPadding, bottom = 14.dp).rise(5),
        onClick = onClick,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.detail_watching_number, watching.number),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (watching.stars != null) StarRating(watching.stars)
            }
            val date = (watching.finishedAt ?: watching.startedAt)?.formatDate()
                ?: stringResource(Res.string.detail_watching_not_started)
            Text(
                text = "$date · ${watching.state.label()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (watching.review != null) {
                MarkdownText(watching.review, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun LazyListScope.notesSection(state: TitleDetailState, onAction: (TitleDetailAction) -> Unit) {
    item(key = "notesHeader") {
        SectionHeader(
            title = stringResource(Res.string.detail_notes),
            action = {
                Text(
                    text = pluralStringResource(Res.plurals.note_count, state.noteCount, state.noteCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        NoteFilters(state, onAction)
    }
    if (state.notes.isEmpty()) {
        item(key = "noNotes") {
            Text(
                text = stringResource(Res.string.detail_no_notes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 12.dp),
            )
        }
    }
    items(state.notes, key = { "note-${it.id}" }) { note ->
        NoteCard(
            note = note,
            onClick = { onAction(TitleDetailAction.OnNoteClick(note.id)) },
            onFavoriteClick = { onAction(TitleDetailAction.OnNoteFavoriteClick(note.id)) },
            onCopyClick = { onAction(TitleDetailAction.OnNoteCopyClick(note.text)) },
            modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, bottom = 14.dp),
        )
    }
}

@Composable
private fun NoteFilters(state: TitleDetailState, onAction: (TitleDetailAction) -> Unit) {
    if (state.watchings.size > 1) {
        ChipRow(Modifier.padding(bottom = 8.dp)) {
            FilmoryChip(
                label = stringResource(Res.string.detail_all_watchings),
                selected = state.noteWatchingFilter.isEmpty(),
                onClick = { onAction(TitleDetailAction.OnNoteWatchingFilterClear) },
            )
            state.watchings.forEach { watching ->
                FilmoryChip(
                    label = stringResource(Res.string.detail_watching_number, watching.number),
                    selected = watching.id in state.noteWatchingFilter,
                    onClick = { onAction(TitleDetailAction.OnNoteWatchingToggle(watching.id)) },
                )
            }
        }
    }
    if (state.title?.type == TitleType.SERIES && state.noteCount > 0) {
        ChipRow(Modifier.padding(bottom = 8.dp)) {
            FilmoryChip(
                label = stringResource(Res.string.detail_all_episodes),
                selected = state.noteEpisodeFilter == NoteEpisodeFilter.All,
                onClick = { onAction(TitleDetailAction.OnNoteEpisodeFilterChange(NoteEpisodeFilter.All)) },
            )
            state.noteEpisodeChips.forEach { chip ->
                val filter = NoteEpisodeFilter.Episode(chip.episodeId)
                FilmoryChip(
                    label = stringResource(Res.string.detail_season_episode_short, chip.seasonNumber, chip.episodeNumber),
                    selected = state.noteEpisodeFilter == filter,
                    onClick = { onAction(TitleDetailAction.OnNoteEpisodeFilterChange(filter)) },
                )
            }
            FilmoryChip(
                label = stringResource(Res.string.detail_series_notes),
                selected = state.noteEpisodeFilter == NoteEpisodeFilter.SeriesOnly,
                onClick = { onAction(TitleDetailAction.OnNoteEpisodeFilterChange(NoteEpisodeFilter.SeriesOnly)) },
            )
        }
    }
}

@Composable
private fun TitleDetailSheets(state: TitleDetailState, onAction: (TitleDetailAction) -> Unit) {
    val dismiss = { onAction(TitleDetailAction.OnSheetDismiss) }
    when (val sheet = state.sheet) {
        is TitleDetailSheet.EditWatching -> WatchingEditorSheet(sheet, onAction)
        TitleDetailSheet.CollectionPicker -> state.title?.id?.let { CollectionPickerSheet(it, onDismiss = dismiss) }
        TitleDetailSheet.MarkerSettings -> MarkerSettingsSheet(onDismiss = dismiss)
        TitleDetailSheet.DeleteTitle -> ConfirmDialog(
            title = stringResource(Res.string.detail_delete_title),
            message = stringResource(Res.string.detail_delete_title_message),
            confirmLabel = stringResource(Res.string.action_delete),
            onConfirm = { onAction(TitleDetailAction.OnDeleteTitleConfirm) },
            onDismiss = dismiss,
        )
        is TitleDetailSheet.DeleteWatching -> DeleteWatchingDialog(onAction)
        null -> Unit
    }
}

@Preview
@Composable
private fun TitleDetailScreenPreview() {
    FilmoryTheme {
        TitleDetailScreen(
            state = TitleDetailState(
                title = TitleDetailUi(
                    id = "1", name = "Arrival", posterUrl = null, type = TitleType.MOVIE, year = 2016, genre = "Sci-fi",
                    runtimeMinutes = 116, creator = "Denis Villeneuve", overview = "A linguist is recruited.",
                    status = TitleStatus.WATCHED, isWishlisted = false, isWatchLater = true, seasonCount = 0,
                ),
                watchings = listOf(
                    WatchingUi("w", 1, dev.gaens.filmory.feature.watching.domain.WatchingState.FINISHED, null, null, 4.5f, "Good."),
                ),
            ),
            onAction = {},
        )
    }
}
