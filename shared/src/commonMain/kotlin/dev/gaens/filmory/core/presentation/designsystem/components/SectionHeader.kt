package dev.gaens.filmory.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import dev.gaens.filmory.core.presentation.designsystem.FilmoryTheme

/** A bold section title with a highlighter stroke behind its lower half, plus an optional action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 20.dp, top = 28.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val highlighter = FilmoryTheme.colors.highlighter
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            modifier = Modifier
                .drawBehind {
                    val height = 11.dp.toPx()
                    drawRoundRect(
                        color = highlighter,
                        topLeft = Offset(-4.dp.toPx(), size.height - height - 1.dp.toPx()),
                        size = Size(size.width + 14.dp.toPx(), height),
                        cornerRadius = CornerRadius(2.dp.toPx()),
                    )
                },
        )
        Spacer(Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun SectionAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
