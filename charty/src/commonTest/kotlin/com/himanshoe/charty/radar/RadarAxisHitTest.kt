package com.himanshoe.charty.radar

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals

class RadarAxisHitTest {
    // A 4-axis radar with startAngle -90 puts axis 0 at top, 1 right, 2 bottom, 3 left.
    private val width = 200f
    private val height = 200f

    private fun index(
        x: Float,
        y: Float,
    ) = nearestRadarAxisIndex(
        offset = Offset(x = x, y = y),
        width = width,
        height = height,
        startAngleDegrees = -90f,
        numberOfAxes = 4,
    )

    @Test
    fun tapAboveCentre_selectsTopAxis() {
        assertEquals(0, index(100f, 20f))
    }

    @Test
    fun tapRightOfCentre_selectsRightAxis() {
        assertEquals(1, index(180f, 100f))
    }

    @Test
    fun tapBelowCentre_selectsBottomAxis() {
        assertEquals(2, index(100f, 180f))
    }

    @Test
    fun tapLeftOfCentre_selectsLeftAxis() {
        assertEquals(3, index(20f, 100f))
    }

    @Test
    fun tapNearAnAxisButOffCentre_stillSelectsThatAxis() {
        // Up and slightly right is still closest to the top axis.
        assertEquals(0, index(120f, 30f))
    }
}
