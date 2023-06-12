package com.himanshoe.charty.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.common.config.AxisConfig
import com.himanshoe.charty.common.config.ChartDefaults
import com.himanshoe.charty.common.ui.drawXAxisLabels
import com.himanshoe.charty.common.ui.drawYAxisLabels

@Composable
fun ChartSurface(
    padding: Dp,
    chartData: ChartDataCollection,
    modifier: Modifier = Modifier,
    axisConfig: AxisConfig = ChartDefaults.axisConfigDefaults(),
    content: @Composable () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .padding(
                start = padding.times(2),
                bottom = padding.times(2),
                top = padding.times(2),
                end = padding
            )
            .drawBehind {
                if (chartData.data.count() >= 14 && axisConfig.showGridLabel) {
                    drawXAxisLabels(
                        data = chartData.data.fastMap { it.xValue },
                        count = chartData.data.count(),
                        padding = padding.toPx(),
                        minLabelCount = axisConfig.minLabelCount
                    )
                }
                if (axisConfig.showGridLabel) {
                    drawYAxisLabels(
                        chartData.data.fastMap { it.yValue },
                        spacing = padding.toPx(),
                    )
                }
            }
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
