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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.interaction.ChartRangeSelector
import com.himanshoe.charty.common.interaction.RangeOption
import com.himanshoe.charty.common.interaction.RangeSelectorStyle
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

private const val TOTAL_POINTS = 120

private val rangeOptions =
    listOf(
        RangeOption.last(count = 7, label = "7D"),
        RangeOption.last(count = 30, label = "30D"),
        RangeOption.last(count = 90, label = "90D"),
        RangeOption.All,
    )

/**
 * Demo of [ChartRangeSelector] driving a real [LineChart]: the selected preset's `pointCount`
 * feeds `LineChartConfig.visibleWindow`, which already does the rolling-window clipping and the
 * slide animation — the selector needs no chart-specific glue.
 */
@Composable
internal fun RangeSelectorPlayground() {
    var selected by remember { mutableStateOf(rangeOptions.last()) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }
    var cornerRadius by remember { mutableStateOf(16) }
    var smooth by remember { mutableStateOf(true) }

    val values = remember { randomValues(count = TOTAL_POINTS, tick = 3) }
    val lineData =
        remember(values) {
            values.mapIndexed { index, value -> LineData(label = (index + 1).toString(), value = value) }
        }

    val code =
        """
        val options = listOf(
            RangeOption.last(count = 7, label = "7D"),
            RangeOption.last(count = 30, label = "30D"),
            RangeOption.last(count = 90, label = "90D"),
            RangeOption.All,
        )
        var selected by remember { mutableStateOf(RangeOption.All) }

        ChartRangeSelector(
            options = options,
            selected = selected,                    // now "${selected.label}"
            onSelect = { selected = it },
            style = RangeSelectorStyle(
                selectedColor = ChartyColor.Solid(Color(${colorHex(color)})),
                cornerRadius = $cornerRadius.dp,
            ),
        )
        LineChart(
            data = { points },                      // $TOTAL_POINTS points
            lineConfig = LineChartConfig(
                visibleWindow = selected.pointCount, // ${selected.pointCount ?: "null = show all"}
                smoothCurve = $smooth,
                animation = Animation.Fast,
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            Column(modifier = Modifier.fillMaxSize()) {
                ChartRangeSelector(
                    options = rangeOptions,
                    selected = selected,
                    onSelect = { selected = it },
                    modifier = Modifier.fillMaxWidth(),
                    style =
                        RangeSelectorStyle(
                            selectedColor = ChartyColor.Solid(color = color),
                            cornerRadius = cornerRadius.dp,
                        ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                LineChart(
                    data = { lineData },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = ChartyColor.Solid(color = color),
                    lineConfig =
                        LineChartConfig(
                            visibleWindow = selected.pointCount,
                            smoothCurve = smooth,
                            animation = Animation.Fast,
                        ),
                )
            }
        },
        controls = {
            ControlSection(title = "Range")
            ChoiceRow(
                label = "Preset",
                options = rangeOptions,
                selected = selected,
                labelOf = { it.label },
                onSelect = { selected = it },
            )
            ControlSection(title = "Pills")
            IntSliderRow(
                label = "Corner radius",
                value = cornerRadius,
                valueRange = 0..24,
                onValueChange = { cornerRadius = it },
            )
            ControlSection(title = "Line")
            SwitchRow(label = "Smooth curve", checked = smooth, onCheckedChange = { smooth = it })
            ControlSection(title = "Color")
            ColorRow(label = "Accent", selected = color, onSelect = { color = it })
        },
    )
}
