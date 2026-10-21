package dev.gaens.filmory.core.presentation.richtext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
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

/** [text] rendered, and how offsets in the original text map to offsets in the rendered one and back. */
private class Rendered(val text: AnnotatedString, val toRendered: IntArray, val toOriginal: IntArray)

/**
 * [text] styled as Markdown, with the markers gone, bullets as dots and quotes with a bar: this is
 * for reading. With [reveal], the editor's way: the markers of every run and the prefix of every
 * line that the [reveal] range touches are shown dimmed, the rest of them stay hidden.
 */
fun markdownAnnotated(text: String, colors: MarkdownColors, reveal: TextRange? = null): AnnotatedString =
    render(text, colors, reveal).text

/** Shows what is typed styled as Markdown, hiding the markers except around the cursor or selection. */
class MarkdownVisualTransformation(
    private val colors: MarkdownColors,
    private val reveal: TextRange?,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rendered = render(text.text, colors, reveal)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = rendered.toRendered[offset]
            override fun transformedToOriginal(offset: Int) = rendered.toOriginal[offset]
        }
        return TransformedText(rendered.text, mapping)
    }
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

/** What a line's prefix looks like when it is not being edited. Same length, so no characters vanish; null hides it. */
private fun BlockKind.restingPrefix(): String? = when (this) {
    BlockKind.BULLET -> "• "
    BlockKind.QUOTE -> "▎ "
    BlockKind.HEADING, BlockKind.SUBHEADING -> null
    BlockKind.PARAGRAPH -> ""
}

private fun TextRange?.touches(start: Int, end: Int) = this != null && min <= end && max >= start

private fun render(text: String, colors: MarkdownColors, reveal: TextRange?): Rendered {
    val toRendered = IntArray(text.length + 1)
    val shown = mutableListOf<Int>()
    val annotated = buildAnnotatedString {
        parseBlocks(text).forEachIndexed { index, block ->
            if (index > 0) {
                toRendered[block.lineStart - 1] = length
                shown += block.lineStart - 1
                append('\n')
            }
            appendBlock(text, block, colors, reveal, toRendered, shown)
        }
        toRendered[text.length] = length
    }
    shown += text.length
    return Rendered(annotated, toRendered, shown.toIntArray())
}

private fun AnnotatedString.Builder.appendBlock(
    text: String,
    block: Block,
    colors: MarkdownColors,
    reveal: TextRange?,
    toRendered: IntArray,
    shown: MutableList<Int>,
) {
    val spans = parseInline(text.substring(block.textStart, block.lineEnd)).map {
        // Spans are relative to the line's text, the rest here is relative to the whole text.
        InlineSpan(it.kind, block.textStart + it.start, block.textStart + it.end)
    }
    val revealedSpans = spans.filter { reveal.touches(it.start, it.end) }
    val isLineRevealed = reveal.touches(block.lineStart, block.lineEnd)
    val blockStart = length

    fun appendChar(original: Int, char: Char) {
        toRendered[original] = length
        shown += original
        append(char)
    }

    val resting = block.kind.restingPrefix()
    for (i in block.lineStart until block.textStart) {
        when {
            isLineRevealed -> appendChar(i, text[i])
            resting == null -> toRendered[i] = length
            else -> appendChar(i, resting[i - block.lineStart])
        }
    }
    val bodyStart = length

    for (i in block.textStart until block.lineEnd) {
        val isMarker = spans.any { i in it.start until it.contentStart || i in it.contentEnd until it.end }
        if (isMarker && revealedSpans.none { i in it.start until it.end }) {
            toRendered[i] = length
        } else {
            appendChar(i, text[i])
        }
    }

    // Later styles win over earlier ones, so the block comes first, then prefixes, then inline runs.
    block.kind.style(colors)?.let { addStyle(it, blockStart, length) }
    if (bodyStart > blockStart) {
        val color = if (isLineRevealed) colors.marker else colors.accent
        addStyle(SpanStyle(color = color, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal), blockStart, bodyStart)
    }
    spans.forEach { span ->
        if (span in revealedSpans) {
            val markerStyle = SpanStyle(color = colors.marker, fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal)
            addStyle(markerStyle, toRendered[span.start], toRendered[span.contentStart])
            addStyle(markerStyle, toRendered[span.contentEnd], toRendered[span.end - 1] + 1)
        }
        addStyle(span.kind.style(), toRendered[span.contentStart], toRendered[span.contentEnd])
    }
}
