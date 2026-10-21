package dev.gaens.filmory.core.presentation.richtext

/*
 * The small Markdown subset notes, comments and reviews are written in:
 *   **bold**  *italic*  ~~strikethrough~~
 *   "# " and "## " headings, "> " quotes, "- " bullets (one block per line).
 * Everything here is plain Kotlin so it can be tested without a screen.
 */

enum class InlineKind(val marker: String) {
    BOLD("**"),
    STRIKE("~~"),
    ITALIC("*"),
}

/** A styled run of one line. [start] to [end] is the whole run with its markers, [markerLength] long at each end. */
data class InlineSpan(val kind: InlineKind, val start: Int, val end: Int) {
    val markerLength: Int get() = kind.marker.length
    val contentStart: Int get() = start + markerLength
    val contentEnd: Int get() = end - markerLength
}

/** Finds the styled runs of one line. Unmatched markers, and markers next to spaces, stay plain text. */
fun parseInline(line: String): List<InlineSpan> {
    val spans = mutableListOf<InlineSpan>()
    val open = mutableListOf<Pair<InlineKind, Int>>()
    var i = 0
    while (i < line.length) {
        val kind = InlineKind.entries.firstOrNull { line.startsWith(it.marker, i) }
        if (kind == null) {
            i++
            continue
        }
        val length = kind.marker.length
        val canOpen = i + length < line.length && !line[i + length].isWhitespace()
        val canClose = i > 0 && !line[i - 1].isWhitespace()
        val openIndex = open.indexOfLast { it.first == kind }
        val hasContent = openIndex != -1 && i > open[openIndex].second + length
        when {
            canClose && hasContent -> {
                spans += InlineSpan(kind, open[openIndex].second, i + length)
                // Anything opened inside this run and never closed is plain text.
                while (open.size > openIndex) open.removeAt(open.lastIndex)
            }
            canOpen -> open += kind to i
        }
        i += length
    }
    return spans.sortedBy { it.start }
}

enum class BlockKind(val prefix: String) {
    HEADING("# "),
    SUBHEADING("## "),
    QUOTE("> "),
    BULLET("- "),
    PARAGRAPH(""),
}

/** One line of text: its [kind], and where the text after the prefix starts. */
data class Block(val kind: BlockKind, val lineStart: Int, val lineEnd: Int) {
    val textStart: Int get() = lineStart + kind.prefix.length
}

/** Splits [text] into one block per line. */
fun parseBlocks(text: String): List<Block> {
    val blocks = mutableListOf<Block>()
    var start = 0
    while (true) {
        val end = text.indexOf('\n', start).let { if (it == -1) text.length else it }
        val line = text.substring(start, end)
        // "## " has to be tried before "# ", which is its own start.
        val kind = listOf(BlockKind.SUBHEADING, BlockKind.HEADING, BlockKind.QUOTE, BlockKind.BULLET)
            .firstOrNull { line.startsWith(it.prefix) } ?: BlockKind.PARAGRAPH
        blocks += Block(kind, start, end)
        if (end >= text.length) return blocks
        start = end + 1
    }
}

/** [text] as plain words: no markers and no block prefixes, bullets as a dot. For searching and previews. */
fun toPlainText(text: String): String = parseBlocks(text).joinToString("\n") { block ->
    val body = text.substring(block.textStart, block.lineEnd)
    val markers = parseInline(body).flatMap { listOf(it.start until it.start + it.markerLength, it.contentEnd until it.end) }
    val stripped = body.filterIndexed { index, _ -> markers.none { index in it } }
    if (block.kind == BlockKind.BULLET) "• $stripped" else stripped
}

/** A text with the part the user has selected: [start] to [end], equal when it is just a cursor. */
data class TextEdit(val text: String, val start: Int, val end: Int)

/**
 * Wraps the selection in [marker], or takes the marker off when the selection is already wrapped.
 * With nothing selected the cursor lands between a new pair of markers.
 */
fun toggleWrap(edit: TextEdit, marker: String): TextEdit {
    val (text, start, end) = edit
    val length = marker.length
    val isInside = start >= length && text.startsWith(marker, start - length) && text.startsWith(marker, end)
    val isSelectedWhole = end - start >= 2 * length && text.startsWith(marker, start) && text.startsWith(marker, end - length)
    return when {
        isInside -> TextEdit(text.removeRange(end, end + length).removeRange(start - length, start), start - length, end - length)
        isSelectedWhole -> TextEdit(text.removeRange(end - length, end).removeRange(start, start + length), start, end - 2 * length)
        else -> TextEdit(text.substring(0, start) + marker + text.substring(start, end) + marker + text.substring(end), start + length, end + length)
    }
}

/**
 * Puts [kind]'s prefix on every line the selection touches, or takes it off when they all have it.
 * A line with another block prefix gets this one instead.
 */
fun toggleBlock(edit: TextEdit, kind: BlockKind): TextEdit {
    val (text, start, end) = edit
    val touched = parseBlocks(text).filter { it.lineStart <= end && it.lineEnd >= start }
    val allHaveIt = touched.all { it.kind == kind }
    var newStart = start
    var newEnd = end
    val result = StringBuilder(text)
    for (block in touched.asReversed()) {
        val oldPrefix = block.kind.prefix
        val newPrefix = if (allHaveIt) "" else kind.prefix
        result.setRange(block.lineStart, block.lineStart + oldPrefix.length, newPrefix)
        val change = newPrefix.length - oldPrefix.length
        if (block.lineStart < start || (block.lineStart == start && start != end)) newStart += change
        if (block.lineStart <= end) newEnd += change
    }
    return TextEdit(result.toString(), newStart.coerceAtLeast(0), newEnd.coerceAtLeast(0))
}
