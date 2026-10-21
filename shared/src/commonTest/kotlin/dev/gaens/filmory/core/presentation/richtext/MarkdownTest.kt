package dev.gaens.filmory.core.presentation.richtext

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTest {
    private fun kinds(line: String) = parseInline(line).map { it.kind to line.substring(it.contentStart, it.contentEnd) }

    @Test
    fun `bold, italic and strikethrough are found`() {
        assertEquals(listOf(InlineKind.BOLD to "loud"), kinds("a **loud** b"))
        assertEquals(listOf(InlineKind.ITALIC to "soft"), kinds("a *soft* b"))
        assertEquals(listOf(InlineKind.STRIKE to "wrong"), kinds("a ~~wrong~~ b"))
    }

    @Test
    fun `runs can sit inside each other`() {
        assertEquals(
            listOf(InlineKind.BOLD to "very *nice* it", InlineKind.ITALIC to "nice"),
            kinds("**very *nice* it**"),
        )
    }

    @Test
    fun `markers next to spaces or without a partner stay plain`() {
        assertEquals(emptyList(), kinds("2 * 3 * 4"))
        assertEquals(emptyList(), kinds("a **never closed"))
        assertEquals(emptyList(), kinds("****"))
        // The bold that was opened inside the italic never closes, so only the italic counts.
        assertEquals(listOf(InlineKind.ITALIC to "a **b"), kinds("*a **b* c"))
    }

    @Test
    fun `each line is one block with its kind`() {
        val text = "# Title\n## Sub\n> said\n- item\nplain"

        assertEquals(
            listOf(BlockKind.HEADING, BlockKind.SUBHEADING, BlockKind.QUOTE, BlockKind.BULLET, BlockKind.PARAGRAPH),
            parseBlocks(text).map { it.kind },
        )
        assertEquals("Title", parseBlocks(text).first().let { text.substring(it.textStart, it.lineEnd) })
    }

    @Test
    fun `empty text is one empty paragraph`() {
        assertEquals(listOf(Block(BlockKind.PARAGRAPH, 0, 0)), parseBlocks(""))
    }

    @Test
    fun `plain text drops markers and prefixes`() {
        assertEquals("Big\nsaid loudly\n• item one", toPlainText("# Big\n> said *loudly*\n- item **one**"))
        assertEquals("hello old world", toPlainText("**hello** ~~old~~ *world*"))
        assertEquals("a * b", toPlainText("a * b"))
    }

    @Test
    fun `wrapping a selection adds markers and wrapping again removes them`() {
        val wrapped = toggleWrap(TextEdit("say hi now", 4, 6), "**")
        assertEquals(TextEdit("say **hi** now", 6, 8), wrapped)

        assertEquals(TextEdit("say hi now", 4, 6), toggleWrap(wrapped, "**"))
    }

    @Test
    fun `a selection that includes its markers is unwrapped`() {
        assertEquals(TextEdit("say hi now", 4, 6), toggleWrap(TextEdit("say **hi** now", 4, 10), "**"))
    }

    @Test
    fun `wrapping nothing puts the cursor between the markers`() {
        assertEquals(TextEdit("ab****", 4, 4), toggleWrap(TextEdit("ab", 2, 2), "**"))
        assertEquals(TextEdit("a****b", 3, 3), toggleWrap(TextEdit("ab", 1, 1), "**"))
    }

    @Test
    fun `a block prefix goes on every touched line and comes off again`() {
        val quoted = toggleBlock(TextEdit("one\ntwo\nthree", 1, 5), BlockKind.QUOTE)
        assertEquals("> one\n> two\nthree", quoted.text)

        assertEquals("one\ntwo\nthree", toggleBlock(quoted, BlockKind.QUOTE).text)
    }

    @Test
    fun `a different prefix replaces the old one`() {
        assertEquals("- one", toggleBlock(TextEdit("> one", 2, 2), BlockKind.BULLET).text)
        assertEquals("## one", toggleBlock(TextEdit("# one", 0, 0), BlockKind.SUBHEADING).text)
    }

    @Test
    fun `the cursor stays on the same letter when a prefix is added`() {
        val edit = toggleBlock(TextEdit("one", 2, 2), BlockKind.BULLET)

        assertEquals("- one", edit.text)
        assertEquals(4, edit.start)
        assertEquals(4, edit.end)
    }
}
