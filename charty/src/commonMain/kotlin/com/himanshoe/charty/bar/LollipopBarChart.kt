@file:Suppress("LongMethod", "LongParameterList", "FunctionNaming", "CyclomaticComplexMethod", "WildcardImport", "MagicNumber", "MaxLineLength", "ReturnCount", "UnusedImports")

package com.himanshoe.charty.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.ext.getValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.common.config.Animation

/**
 * Lollipop Bar Chart - vertical line with a circular head for each value.
 *
 * This chart is similar to a traditional bar chart but uses a thin "stem" and a
 * configurable circle at the value position, making it visually lighter for
 * category comparisons.
 *
 * Configuration options allow customizing stem thickness, circle radius and circle
 * color, along with standard bar width fraction and animation.
 */
@Composable
fun LollipopBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColor.Solid(Color(0xFFE91E63)),
    config: LollipopBarChartConfig = LollipopBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Lollipop bar chart data cannot be empty" }

    val (minValue, maxValue) = remember(dataList) {
        val values = dataList.getValues()
        0f to calculateMaxValue(values)
    }

    val animationProgress = remember {
        Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
    }

    LaunchedEffect(config.animation) {
        if (config.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = config.animation.duration)
            )
        }
    }

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = true
        ),
        config = scaffoldConfig
    ) { chartContext ->
        val baselineY = chartContext.bottom

        dataList.fastForEachIndexed { index, bar ->
            val barLeft = chartContext.calculateBarLeftPosition(index, dataList.size, config.barWidthFraction)
            val barWidth = chartContext.calculateBarWidth(dataList.size, config.barWidthFraction)
            val centerX = barLeft + barWidth / 2f

            val barValueY = chartContext.convertValueToYPosition(bar.value)
            val animatedTopY = baselineY - (baselineY - barValueY) * animationProgress.value

            val chartyColor = bar.color ?: colors
            val circleChartyColor = config.circleColor ?: chartyColor

            val stemBrush = when (chartyColor) {
                is ChartyColor.Solid -> Brush.verticalGradient(
                    colors = listOf(chartyColor.color, chartyColor.color),
                    startY = baselineY,
                    endY = barValueY
                )
                is ChartyColor.Gradient -> Brush.verticalGradient(
                    colors = chartyColor.colors,
                    startY = baselineY,
                    endY = barValueY
                )
            }

            val circleColor = when (circleChartyColor) {
                is ChartyColor.Solid -> circleChartyColor.color
                is ChartyColor.Gradient -> circleChartyColor.colors[index % circleChartyColor.colors.size]
            }

            drawLine(
                brush = stemBrush,
                start = Offset(centerX, baselineY),
                end = Offset(centerX, animatedTopY),
                strokeWidth = config.stemThickness
            )

            if (config.circleStrokeWidth > 0f) {
                drawCircle(
                    color = circleColor,
                    radius = config.circleRadius,
                    center = Offset(centerX, animatedTopY),
                    style = Stroke(width = config.circleStrokeWidth)
                )
            } else {
                drawCircle(
                    color = circleColor,
                    radius = config.circleRadius,
                    center = Offset(centerX, animatedTopY)
                )
            }
        }
    }
}
