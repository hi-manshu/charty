package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceLineIfNeeded
import com.himanshoe.charty.common.draw.drawTooltipIfNeeded
import com.himanshoe.charty.common.tooltip.TooltipState

private const val DATA_LABEL_PADDING = 4f

/**
 * Bundles the parameters required to render a bar chart's bars, keeping [drawBars] within a
 * manageable parameter count.
 *
 * @property dataList The bars to draw.
 * @property chartContext The chart's coordinate context.
 * @property barConfig Appearance and behaviour configuration for the bars.
 * @property baselineY The pixel y-coordinate of the value baseline.
 * @property animationProgress Growth progress in `0f..1f`.
 * @property color The default bar colour, used when a [BarData] has no per-bar colour.
 * @property barBounds Sink for hit-test rectangles, populated when [recordBounds] is `true`.
 * @property textMeasurer Required when [BarChartConfig.showDataLabels] is enabled.
 * @property recordBounds When `true`, each bar's [Rect] is recorded into [barBounds].
 */
internal data class BarDrawParams(
    val dataList: List<BarData>,
    val chartContext: ChartContext,
    val barConfig: BarChartConfig,
    val baselineY: Float,
    val animationProgress: Float,
    val color: ChartyColor,
    val barBounds: MutableList<Pair<Rect, BarData>>,
    val textMeasurer: TextMeasurer? = null,
    val recordBounds: Boolean = false,
)

/**
 * Draw the bars on the chart
 */
internal fun DrawScope.drawBars(params: BarDrawParams) {
    val dataList = params.dataList
    val chartContext = params.chartContext
    val barConfig = params.barConfig
    val baselineY = params.baselineY
    val animationProgress = params.animationProgress
    val color = params.color
    val barBounds = params.barBounds
    val textMeasurer = params.textMeasurer
    val recordBounds = params.recordBounds
    dataList.fastForEachIndexed { index, bar ->
        val barX = chartContext.calculateBarLeftPosition(index, dataList.size, barConfig.barWidthFraction)
        val barWidth = chartContext.calculateBarWidth(dataList.size, barConfig.barWidthFraction)
        val barValueY = chartContext.convertValueToYPosition(bar.value)
        val isNegative = bar.value < 0f
        val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

        val (barTop, barHeight) =
            calculateVerticalBarDimensions(
                isNegative = isNegative,
                isBelowAxisMode = isBelowAxisMode,
                baselineY = baselineY,
                barValueY = barValueY,
                animationProgress = animationProgress,
            )

        if (recordBounds) {
            barBounds.add(
                Rect(
                    left = barX,
                    top = barTop,
                    right = barX + barWidth,
                    bottom = barTop + barHeight,
                ) to bar,
            )
        }

        val barColor = bar.color ?: color
        val brush = with(chartContext) { barColor.toVerticalGradientBrush() }

        drawRoundedBar(
            brush = brush,
            x = barX,
            y = barTop,
            width = barWidth,
            height = barHeight,
            isNegative = isNegative,
            isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS,
            cornerRadius = barConfig.cornerRadius.value,
        )

        if (barConfig.showDataLabels && textMeasurer != null && animationProgress >= 1f) {
            val labelText = barConfig.dataLabelFormatter(bar)
            val textLayout = textMeasurer.measure(labelText, barConfig.dataLabelStyle)
            val labelX = barX + (barWidth - textLayout.size.width) / 2f
            val labelY =
                if (isNegative) {
                    barTop + barHeight + DATA_LABEL_PADDING
                } else {
                    barTop - textLayout.size.height - DATA_LABEL_PADDING
                }
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(labelX, labelY.coerceAtLeast(chartContext.top)),
            )
        }
    }
}

/**
 * Draw the reference line if configured
 */
internal fun DrawScope.drawBarReferenceLineIfNeeded(
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    drawReferenceLineIfNeeded(
        referenceLineConfig = barConfig.referenceLine,
        chartContext = chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = textMeasurer,
    )
}

/**
 * Draw the tooltip if the state is not null
 */
internal fun DrawScope.drawBarTooltipIfNeeded(
    tooltipState: TooltipState?,
    barConfig: BarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    drawTooltipIfNeeded(
        tooltipState = tooltipState,
        tooltipConfig = barConfig.tooltipConfig,
        textMeasurer = textMeasurer,
        chartContext = chartContext,
    )
}

/**
 * Helper function to draw a bar with rounded corners based on bar position
 */
private fun DrawScope.drawRoundedBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float,
) {
    val path =
        Path().apply {
            if (isNegative && isBelowAxisMode) {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
            } else {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            }
        }
    drawPath(path, brush)
}
