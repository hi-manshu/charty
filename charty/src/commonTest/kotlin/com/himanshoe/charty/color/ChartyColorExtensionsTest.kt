package com.himanshoe.charty.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChartyColorExtensionsTest {
    @Test
    fun toBrush_solid_isSolidColorOfTheSameColor() {
        val brush = ChartyColor.Solid(Color.Red).toBrush()
        assertTrue(brush is SolidColor)
        assertEquals(Color.Red, brush.value)
    }

    @Test
    fun toBrush_solid_isReusedAcrossCalls() {
        val color = ChartyColor.Solid(Color.Red)
        assertTrue(color.toBrush() === color.toBrush())
    }

    @Test
    fun toHorizontalBrush_solid_isSolidColorOfTheSameColor() {
        val brush = ChartyColor.Solid(Color.Blue).toHorizontalBrush()
        assertTrue(brush is SolidColor)
        assertEquals(Color.Blue, brush.value)
    }

    @Test
    fun toBrush_gradient_isNotSolid() {
        val brush = ChartyColor.Gradient(listOf(Color.Red, Color.Blue)).toBrush()
        assertTrue(brush !is SolidColor)
    }

    @Test
    fun withChartyColor_solid_setsTextColor() {
        val style = TextStyle(fontSize = 12.sp).withChartyColor(ChartyColor.Solid(Color.Green))
        assertEquals(Color.Green, style.color)
        assertEquals(12.sp, style.fontSize)
    }

    @Test
    fun withChartyColor_gradient_setsBrushInsteadOfColor() {
        val style = TextStyle(fontSize = 12.sp).withChartyColor(ChartyColor.Gradient(listOf(Color.Red, Color.Blue)))
        assertNotNull(style.brush)
        assertEquals(12.sp, style.fontSize)
    }

    @Test
    fun withChartyColor_null_keepsStyleUnchanged() {
        val style = TextStyle(color = Color.Yellow, fontSize = 12.sp)
        assertEquals(style, style.withChartyColor(null))
    }

    @Test
    fun withChartyColor_null_onUncoloredStyle_leavesBrushUnset() {
        assertNull(TextStyle(fontSize = 12.sp).withChartyColor(null).brush)
    }
}
