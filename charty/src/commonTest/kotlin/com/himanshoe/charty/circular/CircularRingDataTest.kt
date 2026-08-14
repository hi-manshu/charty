package com.himanshoe.charty.circular

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.color.ChartyColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private fun ring(
    progress: Float,
    maxValue: Float,
) = CircularRingData(
    label = "Move",
    progress = progress,
    maxValue = maxValue,
    color = ChartyColor.Solid(Color.Red),
)

class CircularRingDataTest {
    @Test
    fun `percentage is the progress against the maximum`() {
        assertEquals(expected = 75f, actual = ring(progress = 450f, maxValue = 600f).calculatePercentage())
    }

    @Test
    fun `percentage never exceeds a full ring`() {
        assertEquals(expected = 100f, actual = ring(progress = 900f, maxValue = 600f).calculatePercentage())
    }

    @Test
    fun `a ring cannot be built without a maximum to measure against`() {
        // This is what keeps calculatePercentage and calculateSweepAngle from ever dividing by zero,
        // so it is worth pinning: the guard lives at construction, not at each division.
        assertFailsWith<IllegalArgumentException> { ring(progress = 0f, maxValue = 0f) }
    }
}
