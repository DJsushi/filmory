package dev.gaens.filmory.feature.home.presentation

import androidx.compose.ui.graphics.Color
import dev.gaens.filmory.core.presentation.designsystem.contrastRatio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HeaderTintTest {
    private val base = Color(0xFF346A97)
    private val white = Color.White

    @Test
    fun `average skips transparent pixels`() {
        val red = 0xFFFF0000.toInt()
        val blue = 0xFF0000FF.toInt()
        val clear = 0x00FFFFFF

        assertEquals(Color(0.5f, 0f, 0.5f), averageColor(intArrayOf(red, blue, clear)))
    }

    @Test
    fun `average of an empty or see-through image is null`() {
        assertNull(averageColor(intArrayOf()))
        assertNull(averageColor(intArrayOf(0x00000000)))
    }

    @Test
    fun `any poster color gives text that is readable on the header`() {
        val posters = listOf(Color.White, Color.Black, Color.Yellow, Color(0xFFFFEEAA), Color(0xFF123456), Color.Red)

        posters.forEach { poster ->
            assertTrue(contrastRatio(white, headerTint(poster, base, white)) >= 4.5f, "poster $poster")
        }
    }

    @Test
    fun `a poster as dark as the base only leans toward the poster`() {
        val poster = Color(0xFF223322)

        assertEquals(androidx.compose.ui.graphics.lerp(base, poster, 0.6f), headerTint(poster, base, white))
    }

    @Test
    fun `dark text is pushed toward white on a dark base`() {
        val tint = headerTint(Color.Black, Color(0xFF202020), onBase = Color.Black)

        assertTrue(contrastRatio(Color.Black, tint) >= 4.5f)
    }
}
