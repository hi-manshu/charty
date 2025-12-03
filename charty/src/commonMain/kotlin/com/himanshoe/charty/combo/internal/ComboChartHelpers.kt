package com.himanshoe.charty.combo.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.ChartContext

/**
 * Calculate positions for line points
 */
internal fun ChartContext.calculateLinePointPositions(
    dataList: List<ComboChartData>,
): List<Offset> {
    return dataList.fastMapIndexed { index, comboData ->
        Offset(
            x = calculateCenteredXPosition(index, dataList.size),
            y = convertValueToYPosition(comboData.lineValue),
        )
    }
}

/**
 * Add hit areas for point interaction
 */
internal fun MutableList<Pair<Rect, ComboChartData>>.addPointHitAreas(
    pointPositions: List<Offset>,
    dataList: List<ComboChartData>,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1)
        if (pointProgress <= animationProgress) {
            val hitRadius = comboConfig.pointRadius * ComboChartConstants.TWO
            add(
                Rect(
                    left = position.x - hitRadius,
                    top = position.y - hitRadius,
                    right = position.x + hitRadius,
                    bottom = position.y + hitRadius,
                ) to dataList[index],
            )
        }
    }
}

/**
 * Calculate bar dimensions with animation
 */
internal data class BarDimensions(
    val top: Float,
    val height: Float,
)

internal fun calculateAnimatedBarDimensions(
    barValueY: Float,
    baselineY: Float,
    isNegative: Boolean,
    animationProgress: Float,
): BarDimensions {
    return if (isNegative) {
        val fullBarHeight = barValueY - baselineY
        BarDimensions(
            top = baselineY,
            height = fullBarHeight * animationProgress,
        )
    } else {
        val fullBarHeight = baselineY - barValueY
        val animatedBarHeight = fullBarHeight * animationProgress
        BarDimensions(
            top = baselineY - animatedBarHeight,
            height = animatedBarHeight,
        )
    }
}

