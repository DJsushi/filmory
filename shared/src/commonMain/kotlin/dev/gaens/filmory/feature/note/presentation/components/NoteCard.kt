package dev.gaens.filmory.feature.note.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.richtext.MarkdownText
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryCard
import dev.gaens.filmory.core.presentation.designsystem.components.FilmoryIconButton
import dev.gaens.filmory.core.presentation.designsystem.components.PosterArt
import dev.gaens.filmory.core.presentation.designsystem.components.TypeBadge
import dev.gaens.filmory.core.presentation.ui.formatDate
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.note.presentation.NoteUi
import dev.gaens.filmory.feature.note.presentation.contextLabel
import dev.gaens.filmory.feature.note.presentation.positionLabel
import dev.gaens.filmory.feature.title.domain.TitleType
import dev.gaens.filmory.feature.note.domain.NoteType
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.comment_count
import dev.gaens.filmory.resources.detail_watching_number
import dev.gaens.filmory.resources.ic_clock
import dev.gaens.filmory.resources.ic_comment
import dev.gaens.filmory.resources.ic_copy
import dev.gaens.filmory.resources.ic_heart
import dev.gaens.filmory.resources.ic_heart_filled
import dev.gaens.filmory.resources.note_add_comment
import dev.gaens.filmory.resources.note_copy
import dev.gaens.filmory.resources.note_favorite
import dev.gaens.filmory.resources.note_unfavorite
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@Composable
fun NoteCard(
    note: NoteUi,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCopyClick: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
) {
    FilmoryCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp)) {
            if (onTitleClick != null) {
                NoteTitleRow(note, onTitleClick)
                Spacer(Modifier.height(10.dp))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp),
            ) {
                TypeBadge(note.type.label())
                note.positionLabel()?.let { PositionChip(it) }
                Spacer(Modifier.weight(1f))
                if (note.watchingNumber != null && onTitleClick == null) {
                    Text(
                        text = stringResource(Res.string.detail_watching_number, note.watchingNumber),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(top = 10.dp, end = 8.dp),
            ) {
                MarkdownText(note.text, style = MaterialTheme.typography.bodyLarge, maxLines = 8, overflow = TextOverflow.Ellipsis)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(painterResource(Res.drawable.ic_comment), null, Modifier.size(16.dp), MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (note.commentCount == 0) {
                            stringResource(Res.string.note_add_comment)
                        } else {
                            pluralStringResource(Res.plurals.comment_count, note.commentCount, note.commentCount)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilmoryIconButton(
                    icon = if (note.isFavorite) Res.drawable.ic_heart_filled else Res.drawable.ic_heart,
                    contentDescription = stringResource(if (note.isFavorite) Res.string.note_unfavorite else Res.string.note_favorite),
                    onClick = onFavoriteClick,
                )
                if (onCopyClick != null) {
                    FilmoryIconButton(Res.drawable.ic_copy, stringResource(Res.string.note_copy), onCopyClick)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = note.createdAt.formatDate(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun NoteTitleRow(note: NoteUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PosterArt(note.titleId, note.titleName, note.titlePosterUrl, Modifier.width(36.dp), showName = false)
        Column {
            Text(note.titleName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(note.contextLabel(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PositionChip(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(24.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(painterResource(Res.drawable.ic_clock), null, Modifier.size(14.dp), MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview
@Composable
private fun NoteCardPreview() {
    FilmoryTheme {
        NoteCard(
            note = NoteUi(
                id = "1", titleId = "t", titleName = "Arrival", titlePosterUrl = null, titleType = TitleType.MOVIE,
                type = NoteType.SCENE, text = "The first time the heptapod ink ring appears.", seasonNumber = null,
                episodeNumber = null, timestampMillis = 1_450_000, watchingNumber = 2, commentCount = 2,
                isFavorite = false, createdAt = Instant.fromEpochMilliseconds(0),
            ),
            onClick = {},
            onFavoriteClick = {},
            onCopyClick = {},
        )
    }
}
