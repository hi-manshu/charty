package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.ui.graphics.Brush

/**
 * Parameters for drawing a single lollipop.
 *
 * @property centerX The X coordinate of the lollipop center
 * @property baselineY The Y coordinate of the baseline (bottom of the chart)
 * @property animatedTopY The animated Y coordinate of the lollipop top
 * @property stemBrush The brush for drawing the stem
 * @property circleColor The color for the circle
 * @property stemThickness The thickness of the stem line
 * @property circleRadius The radius of the circle
 * @property circleStrokeWidth The stroke width of the circle (0 for filled)
 */
internal data class LollipopDrawParams(
    val centerX: Float,
    val baselineY: Float,
    val animatedTopY: Float,
    val stemBrush: Brush,
    val circleColor: androidx.compose.ui.graphics.Color,
    val stemThickness: Float,
    val circleRadius: Float,
    val circleStrokeWidth: Float,
)

