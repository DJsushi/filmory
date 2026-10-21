package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme
import dev.gaens.filmory.core.presentation.designsystem.ScreenPadding

/** A shape whose bottom edge is half an ellipse [curveDepth] deep, like Bookmory's header. */
class CurvedBottomShape(private val curveDepth: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val depth = with(density) { curveDepth.toPx() }.coerceAtMost(size.height)
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - depth)
            arcTo(
                rect = Rect(0f, size.height - 2 * depth, size.width, size.height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            close()
        }
        return Outline.Generic(path)
    }
}

private val HeroShape = CurvedBottomShape(44.dp)

/**
 * The solid blue header with a deep curve. Content inside is white. [containerColor] replaces the
 * blue, the caller keeps the text readable on it. With a [horizontalPadding] of zero, children
 * pad themselves and can reach the screen edge.
 */
@Composable
fun HeroHeader(
    modifier: Modifier = Modifier,
    containerColor: Color = FilmoryTheme.colors.hero,
    horizontalPadding: Dp = ScreenPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides FilmoryTheme.colors.onHero) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(containerColor, HeroShape)
                .statusBarsPadding()
                .padding(start = horizontalPadding, end = horizontalPadding, top = 8.dp, bottom = 40.dp),
            content = content,
        )
    }
}
