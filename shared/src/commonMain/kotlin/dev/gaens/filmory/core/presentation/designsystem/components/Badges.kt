package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.icon
import dev.gaens.filmory.core.presentation.ui.label
import dev.gaens.filmory.feature.title.domain.TitleStatus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

data class BadgeColors(val container: Color, val content: Color)

@Composable
fun statusColors(status: TitleStatus): BadgeColors {
    val scheme = MaterialTheme.colorScheme
    val colors = FilmoryTheme.colors
    return when (status) {
        TitleStatus.NOT_WATCHED -> BadgeColors(colors.wishlistContainer, colors.onWishlistContainer)
        TitleStatus.WATCHING -> BadgeColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        TitleStatus.WATCHED -> BadgeColors(colors.watchedContainer, colors.onWatchedContainer)
        TitleStatus.ON_HOLD -> BadgeColors(scheme.tertiaryContainer, scheme.onTertiaryContainer)
        TitleStatus.DROPPED -> BadgeColors(scheme.errorContainer, scheme.onErrorContainer)
    }
}

/** A small pill with an optional icon. */
@Composable
fun FilmoryBadge(
    text: String,
    colors: BadgeColors,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
) {
    Row(
        modifier = modifier
            .height(24.dp)
            .background(colors.container, CircleShape)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(painterResource(icon), null, tint = colors.content, modifier = Modifier.size(14.dp))
        }
        Text(text = text, color = colors.content, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun StatusBadge(status: TitleStatus, modifier: Modifier = Modifier) {
    FilmoryBadge(text = status.label(), colors = statusColors(status), icon = status.icon, modifier = modifier)
}

@Composable
fun TypeBadge(text: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    FilmoryBadge(text = text, colors = BadgeColors(scheme.secondaryContainer, scheme.onSecondaryContainer), modifier = modifier)
}

/** The round status mark in the corner of a poster. Not watched has none. */
@Composable
fun StatusMark(status: TitleStatus, modifier: Modifier = Modifier) {
    if (status == TitleStatus.NOT_WATCHED) return
    val colors = statusColors(status)
    Box(
        modifier = modifier.size(26.dp).background(colors.container, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(status.icon), status.label(), tint = colors.content, modifier = Modifier.size(14.dp))
    }
}
