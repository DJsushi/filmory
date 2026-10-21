package dev.gaens.filmory.core.presentation.richtext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownRevealTest {
    private val colors = MarkdownColors(Color.Gray, Color.Gray, Color.Blue)

    private fun shown(text: String, cursor: Int?) = MarkdownVisualTransformation(colors, cursor?.let { TextRange(it) })
        .filter(androidx.compose.ui.text.AnnotatedString(text)).text.text

    @Test
    fun `markers are hidden when the cursor is elsewhere`() {
        assertEquals("a loud b", shown("a **loud** b", cursor = 0))
        assertEquals("a loud b", shown("a **loud** b", cursor = null))
    }

    @Test
    fun `markers show when the cursor is in or next to the run`() {
        assertEquals("a **loud** b", shown("a **loud** b", cursor = 5))
        assertEquals("a **loud** b", shown("a **loud** b", cursor = 2))
        assertEquals("a **loud** b", shown("a **loud** b", cursor = 10))
    }

    @Test
    fun `only the run under the cursor shows its markers`() {
        assertEquals("**a** b", shown("**a** **b**", cursor = 1))
    }

    @Test
    fun `heading prefix hides off the line and shows on it`() {
        assertEquals("title\nbody", shown("# title\nbody", cursor = 10))
        assertEquals("# title\nbody", shown("# title\nbody", cursor = 3))
    }

    @Test
    fun `the cursor maps through hidden markers`() {
        val transformed = MarkdownVisualTransformation(colors, TextRange(0))
            .filter(androidx.compose.ui.text.AnnotatedString("x **b** y"))
        val mapping = transformed.offsetMapping

        assertEquals("x b y", transformed.text.text)
        assertEquals(2, mapping.originalToTransformed(4))
        assertEquals(4, mapping.transformedToOriginal(2))
        assertEquals(9, mapping.transformedToOriginal(5))
    }
}
