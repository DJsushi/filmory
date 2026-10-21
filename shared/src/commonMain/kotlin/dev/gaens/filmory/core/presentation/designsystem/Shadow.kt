package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/** The soft warm shadow under cards and posters. */
@Composable
fun Modifier.softShadow(shape: Shape): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = 14.dp,
        color = FilmoryTheme.colors.cardShadow.copy(alpha = 0.12f),
        offset = DpOffset(0.dp, 4.dp),
    ),
)
