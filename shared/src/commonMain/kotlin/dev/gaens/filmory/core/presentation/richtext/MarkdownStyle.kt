package dev.gaens.filmory.core.presentation.richtext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em

/** The few colors Markdown styling needs, so styling itself stays free of the theme. */
data class MarkdownColors(val marker: Color, val quote: Color, val accent: Color)

@Composable
fun rememberMarkdownColors(): MarkdownColors {
    val marker = MaterialTheme.colorScheme.outline
    val quote = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = MaterialTheme.colorScheme.primary
    return remember(marker, quote, accent) { MarkdownColors(marker, quote, accent) }
}

/**
 * [text] styled as Markdown. With [showMarkers] the text is unchanged, same length, with the
 * markers dimmed and the text between them styled: this is what the editor shows. Without it the
 * markers are gone, bullets are dots and quotes have a bar: this is for reading.
 */
fun markdownAnnotated(text: String, colors: MarkdownColors, showMarkers: Boolean): AnnotatedString =
    buildAnnotatedString {
        parseBlocks(text).forEachIndexed { index, block ->
            if (index > 0) append('\n')
            appendBlock(text, block, colors, showMarkers)
        }
    }

/** Shows what is typed styled as Markdown. The length never changes, so the cursor needs no mapping. */
class MarkdownVisualTransformation(private val colors: MarkdownColors) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(markdownAnnotated(text.text, colors, showMarkers = true), OffsetMapping.Identity)
}

private fun InlineKind.style() = when (this) {
    InlineKind.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
    InlineKind.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
    InlineKind.STRIKE -> SpanStyle(textDecoration = TextDecoration.LineThrough)
}

private fun BlockKind.style(colors: MarkdownColors): SpanStyle? = when (this) {
    BlockKind.HEADING -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.35.em)
    BlockKind.SUBHEADING -> SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.18.em)
    BlockKind.QUOTE -> SpanStyle(fontStyle = FontStyle.Italic, color = colors.quote)
    BlockKind.BULLET, BlockKind.PARAGRAPH -> null
}

private fun AnnotatedString.Builder.appendBlock(text: String, block: Block, colors: MarkdownColors, showMarkers: Boolean) {
    val body = text.substring(block.textStart, block.lineEnd)
    val spans = parseInline(body)
    val blockStart = length

    val prefix = when {
        showMarkers -> block.kind.prefix
        block.kind == BlockKind.BULLET -> "• "
        block.kind == BlockKind.QUOTE -> "▎ "
        else -> ""
    }
    append(prefix)
    val bodyStart = length

    // Where each character of the body ends up, with the markers left out when they are hidden.
    val offsets = IntArray(body.length + 1)
    var written = 0
    body.forEachIndexed { i, char ->
        offsets[i] = written
        val isMarker = !showMarkers && spans.any { i in it.start until it.contentStart || i in it.contentEnd until it.end }
        if (!isMarker) {
            append(char)
            written++
        }
    }
    offsets[body.length] = written

    // Later styles win over earlier ones, so the block comes first, then prefixes, then inline runs.
    block.kind.style(colors)?.let { addStyle(it, blockStart, length) }
    if (prefix.isNotEmpty()) {
        val color = if (showMarkers) colors.marker else colors.accent
        addStyle(SpanStyle(color = color, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal), blockStart, bodyStart)
    }
    spans.forEach { span ->
        if (showMarkers) {
            val markerStyle = SpanStyle(color = colors.marker, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal)
            addStyle(markerStyle, bodyStart + span.start, bodyStart + span.contentStart)
            addStyle(markerStyle, bodyStart + span.contentEnd, bodyStart + span.end)
        }
        addStyle(span.kind.style(), bodyStart + offsets[span.contentStart], bodyStart + offsets[span.contentEnd])
    }
}
