package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush

/**
 * Data class to hold parameters for drawing a waterfall bar
 */
internal data class WaterfallBarDrawParams(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val brush: Brush,
    val bounds: Rect,
)

