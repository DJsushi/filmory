package dev.gaens.filmory.feature.title.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.ConfirmDialog
import dev.gaens.filmory.core.presentation.designsystem.components.FilmorySegmentedRow
import dev.gaens.filmory.core.presentation.designsystem.components.RatingInputRow
import dev.gaens.filmory.core.presentation.ui.UiText
import dev.gaens.filmory.core.presentation.ui.formatDayMonthYear
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.core.presentation.ui.toLocalDate
import dev.gaens.filmory.feature.watching.domain.Watching
import dev.gaens.filmory.feature.watching.domain.WatchingState
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.action_cancel
import dev.gaens.filmory.resources.action_delete
import dev.gaens.filmory.resources.action_done
import dev.gaens.filmory.resources.action_save
import dev.gaens.filmory.resources.watching_clear_date
import dev.gaens.filmory.resources.watching_clear_rating
import dev.gaens.filmory.resources.watching_delete
import dev.gaens.filmory.resources.watching_delete_message
import dev.gaens.filmory.resources.watching_editor_title
import dev.gaens.filmory.resources.watching_finished
import dev.gaens.filmory.resources.watching_no_date
import dev.gaens.filmory.resources.watching_rating
import dev.gaens.filmory.resources.watching_rating_description
import dev.gaens.filmory.resources.watching_review
import dev.gaens.filmory.resources.watching_started
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchingEditorSheet(draft: Watching, error: UiText?, onAction: (TitleDetailAction) -> Unit) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    ModalBottomSheet(
        onDismissRequest = { onAction(TitleDetailAction.OnSheetDismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = ScreenPadding, end = ScreenPadding, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.watching_editor_title), style = MaterialTheme.typography.headlineSmall)
            val states = WatchingState.entries
            FilmorySegmentedRow(
                options = states.map { it.label() },
                selectedIndex = states.indexOf(draft.state),
                onSelect = { onAction(TitleDetailAction.OnDraftStateChange(states[it])) },
            )
            val startedOn = draft.startedAt?.toLocalDate()
            val finishedOn = draft.finishedAt?.toLocalDate()
            DateField(stringResource(Res.string.watching_started), draft.startedAt, startDateLimits(finishedOn, today)) {
                onAction(TitleDetailAction.OnDraftStartDateChange(it))
            }
            DateField(stringResource(Res.string.watching_finished), draft.finishedAt, finishDateLimits(startedOn, today)) {
                onAction(TitleDetailAction.OnDraftFinishDateChange(it))
            }
            Text(stringResource(Res.string.watching_rating), style = MaterialTheme.typography.labelLarge)
            val halfStars = draft.rating?.halfStars ?: 0
            RatingInputRow(
                halfStars = halfStars,
                onChange = { onAction(TitleDetailAction.OnDraftRatingChange(it)) },
                onClear = { onAction(TitleDetailAction.OnDraftRatingChange(null)) },
                contentDescription = stringResource(Res.string.watching_rating_description, (halfStars / 2f).toString()),
                clearLabel = stringResource(Res.string.watching_clear_rating),
            )
            OutlinedTextField(
                value = draft.review.orEmpty(),
                onValueChange = { onAction(TitleDetailAction.OnDraftReviewChange(it)) },
                label = { Text(stringResource(Res.string.watching_review)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            if (error != null) {
                Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onAction(TitleDetailAction.OnDraftDeleteClick) }) {
                    Text(stringResource(Res.string.watching_delete), color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onAction(TitleDetailAction.OnDraftSave) }) { Text(stringResource(Res.string.action_save)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, value: Instant?, limits: DateLimits, onChange: (LocalDate?) -> Unit) {
    var isPickerOpen by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        FilledTonalButton(onClick = { isPickerOpen = true }) {
            Text(value?.toLocalDate()?.formatDayMonthYear() ?: stringResource(Res.string.watching_no_date))
        }
        if (value != null) {
            TextButton(onClick = { onChange(null) }) { Text(stringResource(Res.string.watching_clear_date)) }
        }
    }
    if (isPickerOpen) {
        // The date picker works in UTC midnights; convert through LocalDate so the day never shifts.
        val initialMillis = value?.toLocalDate()?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    limits.allows(Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date)

                override fun isSelectableYear(year: Int): Boolean = limits.allowsYear(year)
            },
        )
        DatePickerDialog(
            onDismissRequest = { isPickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        isPickerOpen = false
                        pickerState.selectedDateMillis?.let { millis ->
                            onChange(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
                        }
                    },
                ) { Text(stringResource(Res.string.action_done)) }
            },
            dismissButton = {
                TextButton(onClick = { isPickerOpen = false }) { Text(stringResource(Res.string.action_cancel)) }
            },
        ) {
            DatePicker(pickerState)
        }
    }
}

@Composable
fun DeleteWatchingDialog(onAction: (TitleDetailAction) -> Unit) {
    ConfirmDialog(
        title = stringResource(Res.string.watching_delete),
        message = stringResource(Res.string.watching_delete_message),
        confirmLabel = stringResource(Res.string.action_delete),
        onConfirm = { onAction(TitleDetailAction.OnDeleteWatchingConfirm) },
        onDismiss = { onAction(TitleDetailAction.OnSheetDismiss) },
    )
}
