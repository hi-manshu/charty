package com.himanshoe.charty.candlestick.internal

import androidx.compose.ui.graphics.Brush

/**
 * Parameters for drawing a single candlestick
 */
internal data class CandlestickDrawParams(
    val brush: Brush,
    val centerX: Float,
    val bodyTop: Float,
    val bodyHeight: Float,
    val bodyWidth: Float,
    val highY: Float,
    val lowY: Float,
    val wickWidth: Float,
    val showWicks: Boolean,
    val cornerRadius: Float,
)
