@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "ktlint:standard:max-line-length",
    "ktlint:standard:function-naming",
)

package com.himanshoe.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.diverging.DivergingBarChart
import com.himanshoe.charty.diverging.config.DivergingBarChartConfig
import com.himanshoe.charty.diverging.config.DivergingSideScaling
import com.himanshoe.charty.diverging.data.DivergingData
import com.himanshoe.charty.diverging.data.DivergingSelection
import kotlin.random.Random

private val AGE_BANDS = listOf("0–17", "18–24", "25–34", "35–44", "45–54", "55–64", "65+")

private fun demoPyramid(tick: Int): List<DivergingData> {
    val random = Random(seed = tick * 31 + 5)
    return AGE_BANDS.mapIndexed { index, band ->
        val shape = 1f - (index - 2).let { it * it } * 0.05f
        val base = 60f * shape.coerceAtLeast(0.2f)
        DivergingData(
            label = band,
            leftValue = base + random.nextFloat() * 18f,
            rightValue = base + random.nextFloat() * 18f,
        )
    }
}

/**
 * Interactive demo for [DivergingBarChart]: a population pyramid with a scaling chooser, a centre-gap
 * and bar-width slider, value labels, a rolling window, and a tap readout of the selected half.
 */
@Composable
internal fun DivergingPlayground() {
    var sharedScale by remember { mutableStateOf(true) }
    var centerGap by remember { mutableStateOf(6) }
    var barWidth by remember { mutableStateOf(70) }
    var showValueLabels by remember { mutableStateOf(false) }
    var windowed by remember { mutableStateOf(false) }
    var windowSize by remember { mutableStateOf(5) }
    var showTooltip by remember { mutableStateOf(true) }
    var tick by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf<DivergingSelection?>(null) }

    val rows = remember(tick) { demoPyramid(tick) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled
    val scaling = if (sharedScale) DivergingSideScaling.SHARED else DivergingSideScaling.INDEPENDENT
    val leftColor = ChartyColor.Solid(playgroundPalette[0])
    val rightColor = ChartyColor.Solid(playgroundPalette[2])

    val code =
        """
        // Population pyramid: two series back to back about a centre axis.
        DivergingBarChart(
            data = { rows },                    // List<DivergingData>(label, leftValue, rightValue)
            leftColor = ChartyColor.Solid(Color(${colorHex(playgroundPalette[0])})),
            rightColor = ChartyColor.Solid(Color(${colorHex(playgroundPalette[2])})),
            leftSeriesName = "Male",
            rightSeriesName = "Female",
            divergingConfig = DivergingBarChartConfig(
                sideScaling = DivergingSideScaling.${scaling.name},
                centerGapFraction = ${fc(centerGap / 100f)},
                barWidthFraction = ${fc(barWidth / 100f)},
                showValueLabels = $showValueLabels,${codeArg(
            include = windowed,
            text = "visibleWindow = $windowSize,",
            indent = 16,
        )}
            ),
            onBarClick = { selection -> /* ${clicked?.let { "${it.data.label} ${it.side}" } ?: "tap a bar"} */ },
            tooltip = ${if (showTooltip) "ChartTooltip.canvas()" else "ChartTooltip.none()"},
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            DivergingBarChart(
                data = { rows },
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                leftColor = leftColor,
                rightColor = rightColor,
                leftSeriesName = "Male",
                rightSeriesName = "Female",
                divergingConfig =
                    DivergingBarChartConfig(
                        sideScaling = scaling,
                        centerGapFraction = centerGap / 100f,
                        barWidthFraction = barWidth / 100f,
                        showValueLabels = showValueLabels,
                        animation = animation,
                        visibleWindow = windowOf(enabled = windowed, size = windowSize),
                    ),
                onBarClick = { clicked = it },
                tooltip = if (showTooltip) ChartTooltip.canvas() else ChartTooltip.none(),
            )
        },
        controls = {
            ControlSection(title = "Scaling")
            SwitchRow(
                label = "Shared scale across both halves",
                checked = sharedScale,
                onCheckedChange = { sharedScale = it },
            )
            Text(
                text =
                    if (sharedScale) {
                        "Both halves scale to the maximum across the two series, so lengths compare across the axis."
                    } else {
                        "Each half fills the plot on its own maximum. One linear axis can no longer describe both, so tick labels are hidden — turn on value labels."
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Layout")
            IntSliderRow(
                label = "Centre gap (%)",
                value = centerGap,
                valueRange = 0..30,
                onValueChange = { centerGap = it },
            )
            IntSliderRow(
                label = "Bar width (%)",
                value = barWidth,
                valueRange = 20..95,
                onValueChange = { barWidth = it },
            )
            SwitchRow(
                label = "Value labels",
                checked = showValueLabels,
                onCheckedChange = { showValueLabels = it },
            )
            VisibleWindowControls(
                enabled = windowed,
                onEnabledChange = { windowed = it },
                size = windowSize,
                onSizeChange = { windowSize = it },
                sizeRange = 2..7,
            )
            ControlSection(title = "Tooltip")
            SwitchRow(label = "Tap tooltip", checked = showTooltip, onCheckedChange = { showTooltip = it })
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(
                text =
                    clicked?.let { selection ->
                        "${selection.data.label} · ${selection.side} → ${selection.value.toInt()}"
                    } ?: "Tap either half of a row",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
