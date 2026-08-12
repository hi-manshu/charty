package com.himanshoe.charty.common.interaction

import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RangeSelectorStyleTest {
    @Test
    fun defaults_areValid() {
        val style = RangeSelectorStyle()
        assertEquals(16.dp, style.cornerRadius)
        assertEquals(12.dp, style.horizontalPadding)
        assertEquals(6.dp, style.verticalPadding)
        assertEquals(8.dp, style.itemSpacing)
        assertTrue(style.selectedColor is ChartyColor.Solid)
        assertTrue(style.unselectedColor is ChartyColor.Solid)
    }

    @Test
    fun negativeCornerRadius_throws() {
        assertFailsWith<IllegalArgumentException> { RangeSelectorStyle(cornerRadius = (-1).dp) }
    }

    @Test
    fun negativeHorizontalPadding_throws() {
        assertFailsWith<IllegalArgumentException> { RangeSelectorStyle(horizontalPadding = (-1).dp) }
    }

    @Test
    fun negativeVerticalPadding_throws() {
        assertFailsWith<IllegalArgumentException> { RangeSelectorStyle(verticalPadding = (-1).dp) }
    }

    @Test
    fun negativeItemSpacing_throws() {
        assertFailsWith<IllegalArgumentException> { RangeSelectorStyle(itemSpacing = (-1).dp) }
    }

    @Test
    fun zeroValues_construct() {
        val style =
            RangeSelectorStyle(
                cornerRadius = 0.dp,
                horizontalPadding = 0.dp,
                verticalPadding = 0.dp,
                itemSpacing = 0.dp,
            )
        assertEquals(0.dp, style.cornerRadius)
    }
}
