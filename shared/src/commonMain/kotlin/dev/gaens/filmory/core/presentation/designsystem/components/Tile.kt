package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** A home tile: title and count on the left or top, a muted icon in the corner. */
@Composable
fun FilmoryTile(
    title: String,
    subtitle: String,
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isWide: Boolean = false,
) {
    FilmoryCard(modifier = modifier, onClick = onClick) {
        val iconTint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 92.dp).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TileText(title, subtitle, Modifier.weight(1f))
                Icon(painterResource(icon), null, tint = iconTint, modifier = Modifier.size(30.dp))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 112.dp).padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                TileText(title, subtitle)
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp).align(Alignment.End),
                )
            }
        }
    }
}

@Composable
private fun TileText(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
