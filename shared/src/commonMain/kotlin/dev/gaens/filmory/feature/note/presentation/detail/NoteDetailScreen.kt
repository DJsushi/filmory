package dev.gaens.filmory.feature.note.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryAppBar
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryCard
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.SectionHeader
import dev.gaens.filmory.core.presentation.designsystem.components.TypeBadge
import dev.gaens.filmory.core.presentation.designsystem.rise
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.core.presentation.ui.ObserveAsEvents
import dev.gaens.filmory.core.presentation.ui.formatDateTime
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.note.presentation.contextLabel
import dev.gaens.filmory.feature.note.presentation.components.PositionChip
import dev.gaens.filmory.feature.note.presentation.positionLabel
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_back
import dev.gaens.filmory.resources.action_delete
import dev.gaens.filmory.resources.comment_count
import dev.gaens.filmory.resources.ic_back
import dev.gaens.filmory.resources.ic_delete
import dev.gaens.filmory.resources.ic_edit
import dev.gaens.filmory.resources.ic_send
import dev.gaens.filmory.resources.note_add_comment
import dev.gaens.filmory.resources.note_detail_comments
import dev.gaens.filmory.resources.note_detail_delete
import dev.gaens.filmory.resources.note_detail_delete_comment
import dev.gaens.filmory.resources.note_detail_delete_message
import dev.gaens.filmory.resources.note_detail_edit
import dev.gaens.filmory.resources.note_detail_image
import dev.gaens.filmory.resources.note_detail_no_comments
import dev.gaens.filmory.resources.note_detail_send
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteDetailScreenRoot(
    noteId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onTitleClick: (String) -> Unit,
    viewModel: NoteDetailViewModel = koinViewModel { parametersOf(noteId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NoteDetailEvent.NoteDeleted -> onBackClick()
            is NoteDetailEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        NoteDetailScreen(
            state = state,
            onAction = { action ->
                when (action) {
                    NoteDetailAction.OnBackClick -> onBackClick()
                    NoteDetailAction.OnEditClick -> onEditClick()
                    is NoteDetailAction.OnTitleClick -> onTitleClick(action.titleId)
                    else -> Unit
                }
                viewModel.onAction(action)
            },
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.TopCenter).statusBarsPadding())
    }
}

private val BigText = TextStyle(fontSize = 18.sp, lineHeight = 28.sp)
private val ComposerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun NoteDetailScreen(
    state: NoteDetailState,
    onAction: (NoteDetailAction) -> Unit,
) {
    val note = state.note
    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(Modifier.weight(1f).statusBarsPadding(), contentPadding = PaddingValues(bottom = 16.dp)) {
            item(key = "bar") {
                FilmoryAppBar(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    navigationIcon = {
                        FilmoryIconButton(Res.drawable.ic_back, stringResource(Res.string.action_back), {
                            onAction(NoteDetailAction.OnBackClick)
                        })
                    },
                    actions = {
                        FilmoryIconButton(Res.drawable.ic_edit, stringResource(Res.string.note_detail_edit), {
                            onAction(NoteDetailAction.OnEditClick)
                        })
                        FilmoryIconButton(Res.drawable.ic_delete, stringResource(Res.string.note_detail_delete), {
                            onAction(NoteDetailAction.OnDeleteClick)
                        })
                    },
                )
            }
            if (note != null) {
                item(key = "note") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction(NoteDetailAction.OnTitleClick(note.titleId)) }
                            .padding(start = ScreenPadding, end = ScreenPadding, top = 8.dp)
                            .rise(0),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PosterArt(note.titleId, note.titleName, note.titlePosterUrl, Modifier.width(56.dp), showName = false)
                        Column {
                            Text(note.titleName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = note.contextLabel(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp).rise(1),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TypeBadge(note.type.label())
                        note.positionLabel()?.let { PositionChip(it) }
                    }
                    Text(note.text, style = BigText, modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp).rise(2))
                    if (state.imageUrl != null) {
                        AsyncImage(
                            model = state.imageUrl,
                            contentDescription = stringResource(Res.string.note_detail_image),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp)
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(MaterialTheme.shapes.medium),
                        )
                    }
                    Text(
                        text = note.createdAt.formatDateTime(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 12.dp).rise(3),
                    )
                    SectionHeader(
                        title = stringResource(Res.string.note_detail_comments),
                        action = {
                            Text(
                                text = pluralStringResource(Res.plurals.comment_count, state.comments.size, state.comments.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                    if (state.comments.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.note_detail_no_comments),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = ScreenPadding),
                        )
                    }
                }
            }
            items(state.comments, key = { it.id }) { comment ->
                FilmoryCard(Modifier.fillMaxWidth().padding(start = ScreenPadding, end = ScreenPadding, bottom = 12.dp)) {
                    Row(Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f).padding(top = 8.dp)) {
                            Text(comment.text, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = comment.createdAt.formatDateTime(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            )
                        }
                        FilmoryIconButton(Res.drawable.ic_delete, stringResource(Res.string.note_detail_delete_comment), {
                            onAction(NoteDetailAction.OnDeleteCommentClick(comment.id))
                        })
                    }
                }
            }
        }
        Composer(state.draft, onAction)
    }
    if (state.isDeleteDialogOpen) {
        ConfirmDialog(
            title = stringResource(Res.string.note_detail_delete),
            message = stringResource(Res.string.note_detail_delete_message),
            confirmLabel = stringResource(Res.string.action_delete),
            onConfirm = { onAction(NoteDetailAction.OnDeleteConfirm) },
            onDismiss = { onAction(NoteDetailAction.OnDeleteDismiss) },
        )
    }
}

@Composable
private fun Composer(draft: String, onAction: (NoteDetailAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(ComposerShape)
            .background(MaterialTheme.colorScheme.surfaceContainer, ComposerShape)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { onAction(NoteDetailAction.OnDraftChange(it)) },
            placeholder = { Text(stringResource(Res.string.note_add_comment)) },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(
            onClick = { onAction(NoteDetailAction.OnSendClick) },
            enabled = draft.isNotBlank(),
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(painterResource(Res.drawable.ic_send), stringResource(Res.string.note_detail_send))
        }
    }
}

@Preview
@Composable
private fun NoteDetailScreenPreview() {
    FilmoryTheme { NoteDetailScreen(NoteDetailState(), onAction = {}) }
}
