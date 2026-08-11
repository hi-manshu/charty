package com.himanshoe.charty.common.draw

import kotlin.test.Test
import kotlin.test.assertEquals

class PersistentMarkerTest {
    @Test
    fun labelTopLeft_centersPillOnPoint() {
        val topLeft =
            resolveMarkerLabelTopLeft(
                centerX = 100f,
                anchorY = 50f,
                pillWidth = 40f,
                pillHeight = 20f,
                left = 0f,
                right = 200f,
                top = 0f,
                gap = 6f,
            )
        assertEquals(80f, topLeft.x)
        assertEquals(24f, topLeft.y)
    }

    @Test
    fun labelTopLeft_clampsToLeftEdge() {
        val topLeft =
            resolveMarkerLabelTopLeft(
                centerX = 5f,
                anchorY = 50f,
                pillWidth = 40f,
                pillHeight = 20f,
                left = 10f,
                right = 200f,
                top = 0f,
                gap = 6f,
            )
        assertEquals(10f, topLeft.x)
    }

    @Test
    fun labelTopLeft_clampsToRightEdge() {
        val topLeft =
            resolveMarkerLabelTopLeft(
                centerX = 195f,
                anchorY = 50f,
                pillWidth = 40f,
                pillHeight = 20f,
                left = 0f,
                right = 200f,
                top = 0f,
                gap = 6f,
            )
        assertEquals(160f, topLeft.x)
    }

    @Test
    fun labelTopLeft_clampsAboveTop() {
        val topLeft =
            resolveMarkerLabelTopLeft(
                centerX = 100f,
                anchorY = 10f,
                pillWidth = 40f,
                pillHeight = 20f,
                left = 0f,
                right = 200f,
                top = 5f,
                gap = 6f,
            )
        assertEquals(5f, topLeft.y)
    }

    @Test
    fun formatMarkerValue_dropsTrailingZeroOnWholeNumbers() {
        assertEquals("42", formatMarkerValue(42f))
        assertEquals("3.5", formatMarkerValue(3.5f))
        assertEquals("-7", formatMarkerValue(-7f))
    }
}
