package com.himanshoe.charty.point

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val TOLERANCE = 0.001f

class BubbleTooltipTest {
    private val small = BubbleData(label = "Small", yValue = 10f, size = 5f)
    private val large = BubbleData(label = "Large", yValue = 50f, size = 200f)

    private val bounds =
        listOf(
            BubbleBounds(center = Offset(x = 20f, y = 20f), radius = 4f) to small,
            BubbleBounds(center = Offset(x = 60f, y = 60f), radius = 20f) to large,
        )

    @Test
    fun aTapInsideABubbleResolvesToIt() {
        assertEquals(
            small,
            resolveBubbleAt(bubbleBounds = bounds, position = Offset(x = 22f, y = 21f))?.second,
        )
        assertEquals(
            large,
            resolveBubbleAt(bubbleBounds = bounds, position = Offset(x = 75f, y = 60f))?.second,
        )
    }

    @Test
    fun aTapJustOutsideASmallBubbleHitsNothingEvenThoughItIsNearest() {
        assertNull(resolveBubbleAt(bubbleBounds = bounds, position = Offset(x = 26f, y = 20f)))
    }

    @Test
    fun aTapBetweenBubblesHitsNothing() {
        assertNull(resolveBubbleAt(bubbleBounds = bounds, position = Offset(x = 40f, y = 5f)))
        assertNull(resolveBubbleAt(bubbleBounds = emptyList(), position = Offset(x = 20f, y = 20f)))
    }

    @Test
    fun theHitAreaFollowsTheRadiusRatherThanAUniformColumn() {
        val offset = Offset(x = 60f, y = 78f)
        assertEquals(large, resolveBubbleAt(bubbleBounds = bounds, position = offset)?.second)
        val shrunk = listOf(BubbleBounds(center = Offset(x = 60f, y = 60f), radius = 4f) to large)
        assertNull(resolveBubbleAt(bubbleBounds = shrunk, position = offset))
    }

    @Test
    fun theAnchorSitsOnTopOfTheCircleAndResolvesToItsCentre() {
        val state =
            bubbleTooltipState(
                bounds = BubbleBounds(center = Offset(x = 60f, y = 60f), radius = 20f),
                content = "Large (size 200): 50",
            )
        assertEquals("Large (size 200): 50", state.content)
        assertEquals(40f, state.x, TOLERANCE)
        assertEquals(40f, state.y, TOLERANCE)
        assertEquals(40f, state.barWidth, TOLERANCE)
        assertEquals(60f, state.x + state.barWidth / 2f, TOLERANCE)
    }

    @Test
    fun theProjectionFoldsTheEncodedSizeIntoTheLabel() {
        val projected = large.toTooltipPointData()
        assertEquals("Large (size 200)", projected.label)
        assertEquals(50f, projected.value)
    }

    @Test
    fun defaultFormatterRendersTheProjectedBubble() {
        assertEquals("Large (size 200): 50", PointChartConfig().tooltipFormatter(large.toTooltipPointData()))
    }

    @Test
    fun customFormatterReceivesTheProjectedBubble() {
        val config = PointChartConfig(tooltipFormatter = { point -> "${point.label}=${point.value.toInt()}" })
        assertEquals("Large (size 200)=50", config.tooltipFormatter(large.toTooltipPointData()))
    }
}
