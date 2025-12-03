package com.himanshoe.charty.combo.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.ChartContext

/**
 * Draw all bars in the combo chart
 */
internal fun DrawScope.drawComboBars(
    dataList: List<ComboChartData>,
    chartContext: ChartContext,
    comboConfig: ComboChartConfig,
    barColor: ChartyColor,
    baselineY: Float,
    animationProgress: Float,
    isBelowAxisMode: Boolean,
    dataBounds: MutableList<Pair<Rect, ComboChartData>>?,
) {
    dataList.fastForEachIndexed { index, comboData ->
        val barX = chartContext.calculateBarLeftPosition(
            index,
            dataList.size,
            comboConfig.barWidthFraction,
        )
        val barWidth = chartContext.calculateBarWidth(
            dataList.size,
            comboConfig.barWidthFraction,
        )
        val barValueY = chartContext.convertValueToYPosition(comboData.barValue)
        val isNegative = comboData.barValue < 0f

        val barDimensions = calculateAnimatedBarDimensions(
            barValueY = barValueY,
            baselineY = baselineY,
            isNegative = isNegative,
            animationProgress = animationProgress,
        )

        if (dataBounds != null && barDimensions.height > 0) {
            dataBounds.add(
                Rect(
                    left = barX,
                    top = barDimensions.top,
                    right = barX + barWidth,
                    bottom = barDimensions.top + barDimensions.height,
                ) to comboData,
            )
        }

        val brush = with(chartContext) { barColor.toVerticalGradientBrush() }
        drawRoundedBar(
            brush = brush,
            x = barX,
            y = barDimensions.top,
            width = barWidth,
            height = barDimensions.height,
            isNegative = isNegative,
            isBelowAxisMode = isBelowAxisMode,
            cornerRadius = comboConfig.barCornerRadius.value,
        )
    }
}

/**
 * Draw the line portion of the combo chart
 */
internal fun DrawScope.drawComboLine(
    pointPositions: List<Offset>,
    lineColor: ChartyColor,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
    dataList: List<ComboChartData>,
    dataBounds: MutableList<Pair<Rect, ComboChartData>>?,
) {
    if (comboConfig.smoothCurve) {
        drawSmoothCurveLine(
            pointPositions = pointPositions,
            lineColor = lineColor,
            comboConfig = comboConfig,
            animationProgress = animationProgress,
        )
    } else {
        drawStraightLine(
            pointPositions = pointPositions,
            lineColor = lineColor,
            comboConfig = comboConfig,
            animationProgress = animationProgress,
        )
    }

    if (comboConfig.showPoints) {
        dataBounds?.addPointHitAreas(
            pointPositions = pointPositions,
            dataList = dataList,
            comboConfig = comboConfig,
            animationProgress = animationProgress,
        )

        drawLinePoints(
            pointPositions = pointPositions,
            lineColor = lineColor,
            comboConfig = comboConfig,
            animationProgress = animationProgress,
        )
    }
}

