@file:Suppress("LongMethod", "LongParameterList", "FunctionNaming", "CyclomaticComplexMethod", "WildcardImport", "MagicNumber", "MaxLineLength", "ReturnCount", "UnusedImports")

package com.himanshoe.charty.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.AxisConfig
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.ChartScaffoldConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.draw.drawReferenceLine

/**
 * Waterfall Chart - visualizes cumulative effect of sequential gains/losses.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun WaterfallChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    config: WaterfallChartConfig = WaterfallChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig()
) {
    val items = remember(data) { data() }
    require(items.isNotEmpty()) { "Waterfall chart data cannot be empty" }

    val cumulativeValues = remember(items) {
        val result = mutableListOf<Float>()
        var running = 0f
        items.forEach { bar ->
            running += bar.value
            result += running
        }
        result
    }

    val minValue = (cumulativeValues.minOrNull() ?: 0f).coerceAtMost(0f)
    val maxValue = (cumulativeValues.maxOrNull() ?: 0f).coerceAtLeast(0f)

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

    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier,
        xLabels = items.map { it.label },
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = true
        ),
        config = scaffoldConfig
    ) { chartContext ->
        items.fastForEachIndexed { index, bar ->
            val barX = chartContext.calculateBarLeftPosition(index, items.size, config.barWidthFraction)
            val barWidth = chartContext.calculateBarWidth(items.size, config.barWidthFraction)

            val prevTotal = if (index == 0) 0f else cumulativeValues[index - 1]
            val currTotal = cumulativeValues[index]

            val startY = chartContext.convertValueToYPosition(prevTotal)
            val endY = chartContext.convertValueToYPosition(currTotal)

            val isIncrease = bar.value >= 0f
            val fullHeight = kotlin.math.abs(endY - startY)
            val animatedHeight = fullHeight * animationProgress.value
            val animatedTop = if (isIncrease) startY - animatedHeight else startY

            val baseColor = if (isIncrease) config.positiveColor else config.negativeColor
            val chartyColor = bar.color ?: baseColor
            val color = when (chartyColor) {
                is ChartyColor.Solid -> chartyColor.color
                is ChartyColor.Gradient -> chartyColor.colors.first()
            }

            drawWaterfallBar(
                color = color,
                x = barX,
                y = animatedTop,
                width = barWidth,
                height = animatedHeight,
                cornerRadius = config.cornerRadius.value
            )
        }
    }
}

private fun DrawScope.drawWaterfallBar(
    color: Color,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float
) {
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = x,
                top = y,
                right = x + width,
                bottom = y + height,
                topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomLeftCornerRadius = CornerRadius.Zero,
                bottomRightCornerRadius = CornerRadius.Zero
            )
        )
    }
    drawPath(path, color)
}
