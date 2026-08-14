package com.himanshoe.charty.combo.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.baselineY
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.drawPointCrosshair
import com.himanshoe.charty.common.tooltip.drawTooltip

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
        val barX =
            chartContext.calculateBarLeftPosition(
                index,
                dataList.size,
                comboConfig.barWidthFraction,
            )
        val barWidth =
            chartContext.calculateBarWidth(
                dataList.size,
                comboConfig.barWidthFraction,
            )
        val barValueY = chartContext.convertValueToYPosition(comboData.barValue)
        val isNegative = comboData.barValue < 0f

        val barDimensions =
            calculateAnimatedBarDimensions(
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

/**
 * Draws one full combo pass: the optional reference band, the bars, the line and its markers, the
 * optional reference line, the canvas tooltip when no crosshair is active, the interaction
 * overlays, and finally the crosshair itself.
 *
 * @param p The data, geometry, styling, and interaction state for this pass.
 */
internal fun DrawScope.drawComboContent(p: ComboDrawParams) {
    val baselineY = p.chartContext.baselineY(minValue = p.minValue, drawNegativesInPlace = p.isBelowAxisMode)
    drawReferenceBandIfNeeded(
        referenceBandConfig = p.comboConfig.referenceBand,
        chartContext = p.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = p.textMeasurer,
    )
    drawComboBars(
        dataList = p.dataList,
        chartContext = p.chartContext,
        comboConfig = p.comboConfig,
        barColor = p.barColor,
        baselineY = baselineY,
        animationProgress = p.animationProgress,
        isBelowAxisMode = p.isBelowAxisMode,
        dataBounds =
            p.dataBounds.takeIf { p.recordDataBounds },
    )
    val pointPositions = p.chartContext.calculateLinePointPositions(dataList = p.dataList, lineRange = p.lineRange)
    p.crosshairBounds?.let { bounds ->
        bounds.clear()
        pointPositions.fastForEachIndexed { index, pos ->
            p.dataList.getOrNull(index)?.let { bounds.add(pos to it) }
        }
    }
    drawComboLine(
        pointPositions = pointPositions,
        lineColor = p.lineColor,
        comboConfig = p.comboConfig,
        animationProgress = p.animationProgress,
        dataList = p.dataList,
        dataBounds =
            p.dataBounds.takeIf { p.recordDataBounds },
    )
    drawPersistentMarkers(
        chartContext = p.chartContext,
        markers = p.comboConfig.markers,
        pointPositions = pointPositions,
        valueLabelFor = { index -> formatMarkerValue(p.dataList[index].lineValue) },
        textMeasurer = p.textMeasurer,
    )
    p.comboConfig.referenceLine?.let { referenceLineConfig ->
        drawReferenceLine(
            chartContext = p.chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = referenceLineConfig,
            textMeasurer = p.textMeasurer,
        )
    }
    p.tooltipState?.takeIf { p.drawTooltipBubble }?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = p.tooltipConfig,
            textMeasurer = p.textMeasurer,
            chartWidth = p.chartContext.right,
            chartTop = p.chartContext.top,
            chartBottom = p.chartContext.bottom,
        )
    }
    drawInteractionOverlays(
        interactionConfig = p.interactionConfig,
        chartContext = p.chartContext,
        totalItems = p.dataList.size,
        textMeasurer = p.textMeasurer,
    )
    p.crosshairState?.let { state ->
        p.comboConfig.crosshairConfig?.let { config ->
            drawPointCrosshair(
                state = state,
                config = config,
                chartContext = p.chartContext,
                textMeasurer = p.textMeasurer,
                chartColor = p.lineColor,
            )
        }
    }
}
