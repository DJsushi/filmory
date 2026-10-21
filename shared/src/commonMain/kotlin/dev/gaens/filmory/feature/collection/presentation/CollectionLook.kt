package dev.gaens.filmory.feature.collection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.gaens.filmory.core.presentation.designsystem.components.posterGradient
import dev.gaens.filmory.core.presentation.designsystem.readableContentOn
import dev.gaens.filmory.core.presentation.designsystem.softShadow
import dev.gaens.filmory.feature.collection.domain.CollectionIcon
import dev.gaens.filmory.feature.collection.domain.TitleCollection
import dev.gaens.filmory.feature.collection.domain.collectionIconOf
import dev.gaens.filmory.resources.Res
import dev.gaens.filmory.resources.ic_bolt
import dev.gaens.filmory.resources.ic_bookmark
import dev.gaens.filmory.resources.ic_calendar
import dev.gaens.filmory.resources.ic_clock
import dev.gaens.filmory.resources.ic_cloud
import dev.gaens.filmory.resources.ic_drop
import dev.gaens.filmory.resources.ic_flag
import dev.gaens.filmory.resources.ic_flame
import dev.gaens.filmory.resources.ic_folder
import dev.gaens.filmory.resources.ic_ghost
import dev.gaens.filmory.resources.ic_gift
import dev.gaens.filmory.resources.ic_globe
import dev.gaens.filmory.resources.ic_heart
import dev.gaens.filmory.resources.ic_home
import dev.gaens.filmory.resources.ic_library
import dev.gaens.filmory.resources.ic_moon
import dev.gaens.filmory.resources.ic_music
import dev.gaens.filmory.resources.ic_notes
import dev.gaens.filmory.resources.ic_play
import dev.gaens.filmory.resources.ic_shuffle
import dev.gaens.filmory.resources.ic_smile
import dev.gaens.filmory.resources.ic_star
import dev.gaens.filmory.resources.ic_sun
import dev.gaens.filmory.resources.ic_tv
import dev.gaens.filmory.resources.ic_watched
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * How a collection looks on a card: its icon (a photo already turned into a file path) and its
 * color. A null color keeps the gradient picked from the collection's id.
 */
data class CollectionLook(
    val iconKey: String? = null,
    val photoPath: String? = null,
    val colorArgb: Long? = null,
)

fun TitleCollection.toLook(resolvePhoto: (relativePath: String) -> String) = CollectionLook(
    iconKey = iconKey,
    photoPath = (collectionIconOf(iconKey) as? CollectionIcon.Photo)?.let { resolvePhoto(it.path) },
    colorArgb = colorArgb,
)

/** The drawn symbols a collection can use, by the name saved in its icon key. The names never change. */
val CollectionSymbols: List<Pair<String, DrawableResource>> = listOf(
    "film" to Res.drawable.ic_library,
    "tv" to Res.drawable.ic_tv,
    "play" to Res.drawable.ic_play,
    "heart" to Res.drawable.ic_heart,
    "star" to Res.drawable.ic_star,
    "bookmark" to Res.drawable.ic_bookmark,
    "flag" to Res.drawable.ic_flag,
    "folder" to Res.drawable.ic_folder,
    "notes" to Res.drawable.ic_notes,
    "calendar" to Res.drawable.ic_calendar,
    "clock" to Res.drawable.ic_clock,
    "home" to Res.drawable.ic_home,
    "watched" to Res.drawable.ic_watched,
    "shuffle" to Res.drawable.ic_shuffle,
    "moon" to Res.drawable.ic_moon,
    "sun" to Res.drawable.ic_sun,
    "cloud" to Res.drawable.ic_cloud,
    "drop" to Res.drawable.ic_drop,
    "bolt" to Res.drawable.ic_bolt,
    "flame" to Res.drawable.ic_flame,
    "smile" to Res.drawable.ic_smile,
    "ghost" to Res.drawable.ic_ghost,
    "music" to Res.drawable.ic_music,
    "gift" to Res.drawable.ic_gift,
    "globe" to Res.drawable.ic_globe,
)

/** The icon of a collection at [size], tinted with [tint]. Draws nothing when the collection has none. */
@Composable
fun CollectionIconView(look: CollectionLook, tint: Color, size: Dp, modifier: Modifier = Modifier) {
    when (val icon = collectionIconOf(look.iconKey)) {
        is CollectionIcon.Symbol -> CollectionSymbols.firstOrNull { it.first == icon.name }?.let { (_, drawable) ->
            Icon(painterResource(drawable), null, modifier.size(size), tint)
        }
        is CollectionIcon.Emoji -> Text(icon.text, modifier, fontSize = (size.value * 0.85f).sp, maxLines = 1)
        is CollectionIcon.Photo -> look.photoPath?.let {
            AsyncImage(it, null, modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop)
        }
        null -> Unit
    }
}

/** The card background of a collection: its color, or the gradient picked from [seed]. */
fun collectionBackground(look: CollectionLook, seed: String): Brush =
    look.colorArgb?.let { SolidColor(Color(it.toInt())) } ?: posterGradient(seed)

/** The color of text and icons on a collection card. */
fun collectionContentColor(look: CollectionLook): Color =
    look.colorArgb?.let { readableContentOn(Color(it.toInt())) } ?: Color.White

/**
 * A collection card: its color and icon, with [content] laid out at the bottom in a color that is
 * readable on it.
 */
@Composable
fun CollectionCardSurface(
    look: CollectionLook,
    seed: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable ColumnScope.(contentColor: Color) -> Unit,
) {
    val contentColor = collectionContentColor(look)
    Box(
        modifier = modifier
            .softShadow(shape)
            .clip(shape)
            .background(collectionBackground(look, seed))
            .clickable(enabled = isEnabled, onClick = onClick),
    ) {
        CollectionIconView(look, contentColor, 28.dp, Modifier.align(Alignment.TopEnd).padding(12.dp))
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) { content(contentColor) }
    }
}
