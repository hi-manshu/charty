package com.himanshoe.charty.common.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ChartyThemeTest {
    private val red = ChartyColor.Solid(Color.Red)
    private val green = ChartyColor.Solid(Color.Green)
    private val blue = ChartyColor.Solid(Color.Blue)
    private val palette = listOf(red, green, blue)

    @Test
    fun colorForSeries_withinPalette_returnsMatchingColor() {
        val theme = ChartyTheme(palette = palette)
        assertEquals(red, theme.colorForSeries(0))
        assertEquals(green, theme.colorForSeries(1))
        assertEquals(blue, theme.colorForSeries(2))
    }

    @Test
    fun colorForSeries_beyondPalette_wrapsAround() {
        val theme = ChartyTheme(palette = palette)
        assertEquals(red, theme.colorForSeries(3))
        assertEquals(green, theme.colorForSeries(4))
    }

    @Test
    fun colorForSeries_negativeIndex_wrapsAround() {
        val theme = ChartyTheme(palette = palette)
        assertEquals(blue, theme.colorForSeries(-1))
    }

    @Test
    fun colorForSeries_emptyPalette_fallsBackToPrimary() {
        val primary = ChartyColor.Solid(Color.Magenta)
        val theme = ChartyTheme(palette = emptyList(), primaryColor = primary)
        assertEquals(primary, theme.colorForSeries(0))
        assertEquals(primary, theme.colorForSeries(5))
    }

    @Test
    fun lightAndDark_haveDistinctAxisAndLabelColors() {
        val light = ChartyTheme.light()
        val dark = ChartyTheme.dark()
        assertNotEquals(light.axisColor, dark.axisColor)
        assertNotEquals(light.gridColor, dark.gridColor)
        assertNotEquals(light.labelTextStyle.color, dark.labelTextStyle.color)
    }

    @Test
    fun light_matchesDefaultConstructor() {
        assertEquals(ChartyTheme(), ChartyTheme.light())
    }

    @Test
    fun labelTextStyle_derivesFromTypographyAndComponentColors() {
        val theme =
            ChartyTheme(
                typography = ChartyTypography(axisLabel = TextStyle(fontSize = 20.sp)),
                componentColors = ChartyComponentColors(axisLabelText = ChartyColor.Solid(Color.Magenta)),
            )
        assertEquals(20.sp, theme.labelTextStyle.fontSize)
        assertEquals(Color.Magenta, theme.labelTextStyle.color)
    }

    @Test
    fun darkTheme_labelTextStyle_followsDarkComponentColors() {
        val dark = ChartyTheme.dark()
        assertEquals(ChartyColor.Solid(dark.labelTextStyle.color), dark.componentColors.axisLabelText)
    }

    @Test
    fun crosshairLabelBackground_fallsBackToPrimaryColor() {
        val theme = ChartyTheme(primaryColor = ChartyColor.Solid(Color.Cyan))
        assertNull(theme.componentColors.crosshairLabelBackground)
        assertEquals(ChartyColor.Solid(Color.Cyan), theme.crosshairLabelBackground)
    }

    @Test
    fun crosshairLabelBackground_prefersExplicitComponentColor() {
        val theme =
            ChartyTheme(
                primaryColor = ChartyColor.Solid(Color.Cyan),
                componentColors = ChartyComponentColors(crosshairLabelBackground = ChartyColor.Solid(Color.Yellow)),
            )
        assertEquals(ChartyColor.Solid(Color.Yellow), theme.crosshairLabelBackground)
    }

    @Test
    fun crosshairLabelStyle_fallsBackToAxisLabelColor() {
        val theme =
            ChartyTheme(componentColors = ChartyComponentColors(axisLabelText = ChartyColor.Solid(Color.Magenta)))
        assertEquals(Color.Magenta, theme.crosshairLabelStyle.color)
    }

    @Test
    fun copy_onNestedHolder_leavesOtherGroupsUntouched() {
        val theme = ChartyTheme.light()
        val restyled =
            theme.copy(
                componentColors = theme.componentColors.copy(tooltipBackground = ChartyColor.Solid(Color.Yellow)),
            )
        assertEquals(ChartyColor.Solid(Color.Yellow), restyled.componentColors.tooltipBackground)
        assertEquals(theme.componentColors.markerDot, restyled.componentColors.markerDot)
        assertEquals(theme.typography, restyled.typography)
        assertEquals(theme.shapes, restyled.shapes)
        assertEquals(theme.dimensions, restyled.dimensions)
    }

    @Test
    fun copy_onTypography_flowsIntoDerivedLabelStyle() {
        val base = ChartyTheme.light()
        val restyled = ChartyTheme(typography = base.typography.copy(axisLabel = TextStyle(fontSize = 30.sp)))
        assertEquals(30.sp, restyled.labelTextStyle.fontSize)
        assertEquals(base.labelTextStyle.color, restyled.labelTextStyle.color)
    }
}
