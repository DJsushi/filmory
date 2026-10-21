package dev.gaens.filmory.feature.collection.presentation

import androidx.compose.ui.graphics.Color
import dev.gaens.filmory.core.presentation.designsystem.contrastRatio
import dev.gaens.filmory.core.presentation.designsystem.readableContentOn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollectionColorsTest {
    @Test
    fun `a color goes through hue, saturation and brightness and back`() {
        (CollectionColorPresets + listOf(0xFF000000, 0xFFFFFFFF, 0xFF808080, 0xFFFF0000)).forEach { argb ->
            val back = Color(colorArgbOf(hsvOf(argb)).toInt())
            val original = Color(argb.toInt())

            assertEquals(original.red, back.red, 0.01f, "red of $argb")
            assertEquals(original.green, back.green, 0.01f, "green of $argb")
            assertEquals(original.blue, back.blue, 0.01f, "blue of $argb")
        }
    }

    @Test
    fun `the known hues come out right`() {
        assertEquals(0f, hsvOf(0xFFFF0000).hue, 0.5f)
        assertEquals(120f, hsvOf(0xFF00FF00).hue, 0.5f)
        assertEquals(240f, hsvOf(0xFF0000FF).hue, 0.5f)
    }

    @Test
    fun `text is readable on every preset`() {
        CollectionColorPresets.forEach { argb ->
            val background = Color(argb.toInt())

            assertTrue(contrastRatio(readableContentOn(background), background) >= 4.5f, "preset $argb")
        }
    }
}
