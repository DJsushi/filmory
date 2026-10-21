package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.feature.title.domain.TitleStatus

/** A poster with its name and optional stars and status mark, for grids. */
@Composable
fun TitleGridCell(
    id: String,
    name: String,
    posterUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    status: TitleStatus? = null,
    stars: Float? = null,
) {
    Column(
        modifier = modifier.clickable(onClickLabel = name, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PosterArt(
            seed = id,
            name = name,
            posterUrl = posterUrl,
            modifier = Modifier.fillMaxWidth().softShadow(MaterialTheme.shapes.small),
            overlay = { if (status != null) StatusMark(status, Modifier.padding(6.dp)) },
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (stars != null) StarRating(stars, starSize = 10.dp)
    }
}

/** A row with a small poster, name, details line, stars and status badge, for list layouts. */
@Composable
fun TitleListRow(
    id: String,
    name: String,
    details: String,
    posterUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    status: TitleStatus? = null,
    stars: Float? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ScreenPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PosterArt(id, name, posterUrl, Modifier.width(56.dp).softShadow(MaterialTheme.shapes.small), showName = false)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (stars != null) StarRating(stars, starSize = 12.dp)
        }
        if (status != null) StatusBadge(status)
    }
}
