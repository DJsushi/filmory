package dev.gaens.filmory.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.home_move_earlier
import dev.gaens.filmory.resources.home_move_later
import dev.gaens.filmory.resources.home_remove_from_watch_later
import dev.gaens.filmory.resources.home_watch_later_editor_title
import dev.gaens.filmory.resources.home_watch_later_empty
import dev.gaens.filmory.resources.ic_chevron_down
import dev.gaens.filmory.resources.ic_close
import org.jetbrains.compose.resources.stringResource

/** Bottom sheet to reorder or remove watch-later titles. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchLaterEditor(
    titles: List<TitleThumbUi>,
    onAction: (HomeAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(HomeAction.OnWatchLaterEditorDismiss) },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_watch_later_editor_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp),
            )
            if (titles.isEmpty()) {
                Text(
                    text = stringResource(Res.string.home_watch_later_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                )
            }
            titles.forEachIndexed { index, title ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = ScreenPadding, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PosterArt(title.id, title.name, title.posterUrl, Modifier.width(40.dp), showName = false)
                    Text(
                        text = title.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (index > 0) {
                        FilmoryIconButton(
                            icon = Res.drawable.ic_chevron_down,
                            contentDescription = stringResource(Res.string.home_move_earlier),
                            onClick = { onAction(HomeAction.OnMoveWatchLater(title.id, moveEarlier = true)) },
                            modifier = Modifier.rotate(180f),
                        )
                    }
                    if (index < titles.lastIndex) {
                        FilmoryIconButton(
                            icon = Res.drawable.ic_chevron_down,
                            contentDescription = stringResource(Res.string.home_move_later),
                            onClick = { onAction(HomeAction.OnMoveWatchLater(title.id, moveEarlier = false)) },
                        )
                    }
                    FilmoryIconButton(
                        icon = Res.drawable.ic_close,
                        contentDescription = stringResource(Res.string.home_remove_from_watch_later),
                        onClick = { onAction(HomeAction.OnRemoveWatchLater(title.id)) },
                    )
                }
            }
        }
    }
}
