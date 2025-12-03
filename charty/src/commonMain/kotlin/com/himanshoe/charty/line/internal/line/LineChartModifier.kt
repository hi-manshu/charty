package com.himanshoe.charty.line.internal.line

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Add tap gesture detection for line chart points
 */
internal fun Modifier.lineChartClickHandler(
    dataList: List<LineData>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    onPointClick: (LineData) -> Unit,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return this.pointerInput(dataList, lineConfig, onPointClick) {
        detectTapGestures { offset ->
            val tapRadius = lineConfig.pointRadius * LineChartConstants.TAP_RADIUS_MULTIPLIER
            val clickedPoint = pointBounds.minByOrNull { (position, _) ->
                val dx = position.x - offset.x
                val dy = position.y - offset.y
                sqrt(dx.pow(2) + dy.pow(2))
            }

            clickedPoint?.let { (position, lineData) ->
                val dx = position.x - offset.x
                val dy = position.y - offset.y
                val distance = sqrt(dx.pow(2) + dy.pow(2))

                if (distance <= tapRadius) {
                    onPointClick.invoke(lineData)
                    onTooltipStateChange(
                        TooltipState(
                            content = lineConfig.tooltipFormatter(lineData),
                            x = position.x - lineConfig.pointRadius,
                            y = position.y,
                            barWidth = lineConfig.pointRadius * LineChartConstants.POINT_RADIUS_MULTIPLIER,
                            position = lineConfig.tooltipPosition,
                        ),
                    )
                } else {
                    onTooltipStateChange(null)
                }
            } ?: run {
                onTooltipStateChange(null)
            }
        }
    }
}

