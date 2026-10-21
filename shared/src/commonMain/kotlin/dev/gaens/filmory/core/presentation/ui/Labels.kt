package dev.gaens.filmory.core.presentation.ui

import androidx.compose.runtime.Composable
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.feature.title.domain.TitleStatus
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.note_type_question
import dev.gaens.filmory.resources.note_type_scene
import dev.gaens.filmory.resources.note_type_summary
import dev.gaens.filmory.resources.note_type_thought
import dev.gaens.filmory.resources.status_dropped
import dev.gaens.filmory.resources.status_not_watched
import dev.gaens.filmory.resources.status_on_hold
import dev.gaens.filmory.resources.status_watched
import dev.gaens.filmory.resources.status_watching
import dev.gaens.filmory.resources.type_movie
import dev.gaens.filmory.resources.type_series
import dev.gaens.filmory.resources.watching_state_dropped
import dev.gaens.filmory.resources.watching_state_finished
import dev.gaens.filmory.resources.watching_state_in_progress
import dev.gaens.filmory.resources.watching_state_on_hold
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val TitleStatus.labelResource: StringResource
    get() = when (this) {
        TitleStatus.NOT_WATCHED -> Res.string.status_not_watched
        TitleStatus.WATCHING -> Res.string.status_watching
        TitleStatus.WATCHED -> Res.string.status_watched
        TitleStatus.ON_HOLD -> Res.string.status_on_hold
        TitleStatus.DROPPED -> Res.string.status_dropped
    }

@Composable
fun TitleStatus.label(): String = stringResource(labelResource)

@Composable
fun TitleType.label(): String = stringResource(
    when (this) {
        TitleType.MOVIE -> Res.string.type_movie
        TitleType.SERIES -> Res.string.type_series
    },
)

@Composable
fun WatchingState.label(): String = stringResource(
    when (this) {
        WatchingState.IN_PROGRESS -> Res.string.watching_state_in_progress
        WatchingState.ON_HOLD -> Res.string.watching_state_on_hold
        WatchingState.DROPPED -> Res.string.watching_state_dropped
        WatchingState.FINISHED -> Res.string.watching_state_finished
    },
)

@Composable
fun NoteType.label(): String = stringResource(
    when (this) {
        NoteType.SCENE -> Res.string.note_type_scene
        NoteType.SUMMARY -> Res.string.note_type_summary
        NoteType.THOUGHT -> Res.string.note_type_thought
        NoteType.QUESTION -> Res.string.note_type_question
    },
)
