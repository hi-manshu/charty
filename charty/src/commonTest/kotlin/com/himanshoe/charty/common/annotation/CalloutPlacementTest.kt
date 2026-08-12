package com.himanshoe.charty.common.annotation

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val TOL = 0.001f

class CalloutPlacementTest {
    private fun side(
        preferredSide: CalloutSide,
        anchorY: Float,
        bubbleHeight: Float = 20f,
        pointerLength: Float = 10f,
        plotTop: Float = 0f,
    ) = resolveCalloutSide(
        preferredSide = preferredSide,
        anchorY = anchorY,
        bubbleHeight = bubbleHeight,
        pointerLength = pointerLength,
        plotTop = plotTop,
    )

    @Test
    fun explicitAbove_isHonouredEvenNearTheTopEdge() {
        assertEquals(CalloutSide.ABOVE, side(preferredSide = CalloutSide.ABOVE, anchorY = 2f))
    }

    @Test
    fun explicitBelow_isHonouredEvenWithRoomAbove() {
        assertEquals(CalloutSide.BELOW, side(preferredSide = CalloutSide.BELOW, anchorY = 90f))
    }

    @Test
    fun auto_picksAbove_whenTheBubbleFits() {
        assertEquals(CalloutSide.ABOVE, side(preferredSide = CalloutSide.AUTO, anchorY = 60f))
    }

    @Test
    fun auto_picksBelow_nearTheTopEdge() {
        assertEquals(CalloutSide.BELOW, side(preferredSide = CalloutSide.AUTO, anchorY = 12f))
    }

    @Test
    fun auto_picksAbove_atTheExactFitBoundary() {
        assertEquals(CalloutSide.ABOVE, side(preferredSide = CalloutSide.AUTO, anchorY = 30f))
    }

    @Test
    fun bubbleLeft_centresOnTheAnchor() {
        val left = calloutBubbleLeft(anchorX = 50f, bubbleWidth = 20f, plotLeft = 0f, plotRight = 100f)
        assertEquals(40f, left, TOL)
    }

    @Test
    fun bubbleLeft_clampsAtBothPlotEdges() {
        assertEquals(0f, calloutBubbleLeft(anchorX = 2f, bubbleWidth = 20f, plotLeft = 0f, plotRight = 100f), TOL)
        assertEquals(80f, calloutBubbleLeft(anchorX = 99f, bubbleWidth = 20f, plotLeft = 0f, plotRight = 100f), TOL)
    }

    @Test
    fun bubbleLeft_pinsToLeft_whenWiderThanThePlot() {
        assertEquals(0f, calloutBubbleLeft(anchorX = 50f, bubbleWidth = 400f, plotLeft = 0f, plotRight = 100f), TOL)
    }

    @Test
    fun bubbleTop_sitsAboveOrBelowTheAnchor() {
        val above =
            calloutBubbleTop(
                anchorY = 60f,
                bubbleHeight = 20f,
                side = CalloutSide.ABOVE,
                pointerLength = 10f,
                plotTop = 0f,
                plotBottom = 100f,
            )
        assertEquals(30f, above, TOL)
        val below =
            calloutBubbleTop(
                anchorY = 60f,
                bubbleHeight = 20f,
                side = CalloutSide.BELOW,
                pointerLength = 10f,
                plotTop = 0f,
                plotBottom = 100f,
            )
        assertEquals(70f, below, TOL)
    }

    @Test
    fun bubbleTop_clampsInsideThePlot() {
        val clampedTop =
            calloutBubbleTop(
                anchorY = 5f,
                bubbleHeight = 20f,
                side = CalloutSide.ABOVE,
                pointerLength = 10f,
                plotTop = 0f,
                plotBottom = 100f,
            )
        assertEquals(0f, clampedTop, TOL)
        val clampedBottom =
            calloutBubbleTop(
                anchorY = 98f,
                bubbleHeight = 20f,
                side = CalloutSide.BELOW,
                pointerLength = 10f,
                plotTop = 0f,
                plotBottom = 100f,
            )
        assertEquals(80f, clampedBottom, TOL)
    }

    @Test
    fun calloutStyle_rejectsInvalidNumbers() {
        assertFailsWith<IllegalArgumentException> { CalloutStyle(cornerRadius = -1f) }
        assertFailsWith<IllegalArgumentException> { CalloutStyle(connectorWidth = 0f) }
        assertFailsWith<IllegalArgumentException> { CalloutStyle(paddingHorizontal = -1f) }
        assertFailsWith<IllegalArgumentException> { CalloutStyle(paddingVertical = -1f) }
        assertFailsWith<IllegalArgumentException> { CalloutStyle(pointerLengthPx = -1f) }
    }

    @Test
    fun shapeAnnotation_rejectsNonPositiveStroke() {
        assertFailsWith<IllegalArgumentException> {
            ChartAnnotation.Shape(
                from = AnnotationAnchor.DataPoint(index = 0),
                to = AnnotationAnchor.DataPoint(index = 1),
                kind = AnnotationShapeKind.RECT,
                color = ChartyColor.Solid(Color.Red),
                strokeWidth = 0f,
            )
        }
    }

    @Test
    fun markerFactory_keepsTheLegacyConstructorShape() {
        val annotation = ChartAnnotation(xIndex = 3, label = "Peak")
        assertEquals(ChartAnnotation.Marker(xIndex = 3, label = "Peak"), annotation)
    }
}
