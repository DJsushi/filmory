package dev.gaens.filmory.core.presentation.designsystem

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContrastTest {
    @Test
    fun `black on white is the strongest contrast and the same color is the weakest`() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contrastRatio(Color.Red, Color.Red), 0.001f)
    }

    @Test
    fun `dark backgrounds get white text and light ones get dark text`() {
        assertEquals(Color.White, readableContentOn(Color(0xFF14213D)))
        assertEquals(Color.White, readableContentOn(Color.Black))
        assertTrue(readableContentOn(Color(0xFFFFE08C)) != Color.White)
        assertTrue(readableContentOn(Color.White) != Color.White)
    }

    @Test
    fun `any color gets text with a readable contrast`() {
        val steps = (0..255 step 51).map { it / 255f }
        for (red in steps) for (green in steps) for (blue in steps) {
            val background = Color(red, green, blue)

            assertTrue(contrastRatio(readableContentOn(background), background) >= 4.5f, "background $background")
        }
    }
}
