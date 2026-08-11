package com.himanshoe.charty.common.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ChartyThemeTest {
    private val palette = listOf(Color.Red, Color.Green, Color.Blue)

    @Test
    fun colorForSeries_withinPalette_returnsMatchingColor() {
        val theme = ChartyTheme(palette = palette)
        assertEquals(Color.Red, theme.colorForSeries(0))
        assertEquals(Color.Green, theme.colorForSeries(1))
        assertEquals(Color.Blue, theme.colorForSeries(2))
    }

    @Test
    fun colorForSeries_beyondPalette_wrapsAround() {
        val theme = ChartyTheme(palette = palette)
        assertEquals(Color.Red, theme.colorForSeries(3))
        assertEquals(Color.Green, theme.colorForSeries(4))
    }

    @Test
    fun colorForSeries_negativeIndex_wrapsAround() {
        val theme = ChartyTheme(palette = palette)
        assertEquals(Color.Blue, theme.colorForSeries(-1))
    }

    @Test
    fun colorForSeries_emptyPalette_fallsBackToPrimary() {
        val theme = ChartyTheme(palette = emptyList(), primaryColor = Color.Magenta)
        assertEquals(Color.Magenta, theme.colorForSeries(0))
        assertEquals(Color.Magenta, theme.colorForSeries(5))
    }
}
