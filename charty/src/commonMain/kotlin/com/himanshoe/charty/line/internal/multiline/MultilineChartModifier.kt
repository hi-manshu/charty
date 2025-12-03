package com.himanshoe.charty.line.internal.multiline

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Add tap gesture detection for multiline chart points
 */
internal fun Modifier.multilineChartClickHandler(
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, MultilinePoint>>,
    onPointClick: (MultilinePoint) -> Unit,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return this.pointerInput(dataList, lineConfig, onPointClick) {
        detectTapGestures { offset ->
            val tapRadius = lineConfig.pointRadius * MultilineChartConstants.TAP_RADIUS_MULTIPLIER
            val clickedPoint = pointBounds.minByOrNull { (position, _) ->
                val dx = position.x - offset.x
                val dy = position.y - offset.y
                sqrt(dx.pow(2) + dy.pow(2))
            }

            clickedPoint?.let { (position, point) ->
                val dx = position.x - offset.x
                val dy = position.y - offset.y
                val distance = sqrt(dx.pow(2) + dy.pow(2))

                if (distance <= tapRadius) {
                    onPointClick.invoke(point)
                    onTooltipStateChange(
                        TooltipState(
                            content = point.lineGroup.label +
                                " Line ${point.seriesIndex +
                                    MultilineChartConstants.SERIES_INDEX_OFFSET}: ${point.value}",
                            x = position.x - lineConfig.pointRadius,
                            y = position.y,
                            barWidth = lineConfig.pointRadius * MultilineChartConstants.POINT_RADIUS_MULTIPLIER,
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

