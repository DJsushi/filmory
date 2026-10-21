package dev.gaens.filmory.core.presentation.richtext

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Text written in Markdown, shown without its markers. [maxLines] works as for any text, so a
 * preview can cut it off.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val colors = rememberMarkdownColors()
    val styled = remember(text, colors) { markdownAnnotated(text, colors, showMarkers = false) }
    Text(
        text = styled,
        modifier = modifier,
        style = style,
        color = if (color == Color.Unspecified) LocalContentColor.current else color,
        maxLines = maxLines,
        overflow = overflow,
    )
}
