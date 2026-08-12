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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.gauge.AngularGaugeChart
import com.himanshoe.charty.gauge.config.AngularGaugeConfig
import com.himanshoe.charty.gauge.data.GaugeBand

private val bandSafeColor = Color(0xFF43A047)
private val bandWarnColor = Color(0xFFF9A825)
private val bandDangerColor = Color(0xFFE53935)

/**
 * Interactive playground for [AngularGaugeChart]: drag the value and watch the needle sweep, adjust
 * the range, toggle qualitative plot bands, and recolor the progress arc and needle.
 */
@Composable
internal fun GaugePlayground() {
    var value by remember { mutableStateOf(62) }
    var minValue by remember { mutableStateOf(0) }
    var maxValue by remember { mutableStateOf(100) }
    var tickCount by remember { mutableStateOf(5) }
    var startAngle by remember { mutableStateOf(135f) }
    var sweepAngle by remember { mutableStateOf(270f) }
    var trackWidthFraction by remember { mutableStateOf(0.12f) }
    var showBands by remember { mutableStateOf(true) }
    var showValueLabel by remember { mutableStateOf(true) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }

    val range = (maxValue - minValue).toFloat()
    val bands =
        if (showBands) {
            listOf(
                GaugeBand(
                    fromValue = minValue.toFloat(),
                    toValue = minValue + range * 0.6f,
                    color = ChartyColor.Solid(bandSafeColor),
                ),
                GaugeBand(
                    fromValue = minValue + range * 0.6f,
                    toValue = minValue + range * 0.85f,
                    color = ChartyColor.Solid(bandWarnColor),
                ),
                GaugeBand(
                    fromValue = minValue + range * 0.85f,
                    toValue = maxValue.toFloat(),
                    color = ChartyColor.Solid(bandDangerColor),
                ),
            )
        } else {
            emptyList()
        }

    val bandsCode =
        if (showBands) {
            """listOf(
                    GaugeBand(fromValue = ${fc(
                minValue.toFloat(),
            )}, toValue = ${fc(minValue + range * 0.6f)}, color = ChartyColor.Solid(Color(${colorHex(bandSafeColor)}))),
                    GaugeBand(fromValue = ${fc(
                minValue + range * 0.6f,
            )}, toValue = ${fc(
                minValue + range * 0.85f,
            )}, color = ChartyColor.Solid(Color(${colorHex(bandWarnColor)}))),
                    GaugeBand(fromValue = ${fc(
                minValue + range * 0.85f,
            )}, toValue = ${fc(maxValue.toFloat())}, color = ChartyColor.Solid(Color(${colorHex(bandDangerColor)}))),
                )"""
        } else {
            "emptyList()"
        }
    val code =
        """
        AngularGaugeChart(
            value = { ${value}f },
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            config = AngularGaugeConfig(
                minValue = ${minValue}f,
                maxValue = ${maxValue}f,
                startAngleDegrees = ${fc(startAngle)},
                sweepAngleDegrees = ${fc(sweepAngle)},
                trackWidthFraction = ${fc(trackWidthFraction)},
                tickCount = $tickCount,
                plotBands = $bandsCode,
                needleColor = ChartyColor.Solid(Color(${colorHex(color)})),
                showValueLabel = $showValueLabel,
                animation = ${if (LocalPlaygroundAnimate.current) "Animation.Default" else "Animation.Disabled"},
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            AngularGaugeChart(
                value = { value.toFloat() },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color),
                config =
                    AngularGaugeConfig(
                        minValue = minValue.toFloat(),
                        maxValue = maxValue.toFloat(),
                        startAngleDegrees = startAngle,
                        sweepAngleDegrees = sweepAngle,
                        trackWidthFraction = trackWidthFraction,
                        tickCount = tickCount,
                        plotBands = bands,
                        needleColor = ChartyColor.Solid(color),
                        showValueLabel = showValueLabel,
                        animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled,
                    ),
            )
        },
        controls = {
            ControlSection(title = "Value")
            IntSliderRow(
                label = "Value",
                value = value,
                valueRange = minValue..maxValue,
                onValueChange = { value = it },
            )
            ControlSection(title = "Range")
            IntSliderRow(
                label = "Min",
                value = minValue,
                valueRange = 0..50,
                onValueChange = { minValue = it },
            )
            IntSliderRow(
                label = "Max",
                value = maxValue,
                valueRange = 60..200,
                onValueChange = { maxValue = it },
            )
            ControlSection(title = "Dial")
            IntSliderRow(
                label = "Ticks",
                value = tickCount,
                valueRange = 2..11,
                onValueChange = { tickCount = it },
            )
            SliderRow(
                label = "Start angle",
                value = startAngle,
                valueRange = 0f..270f,
                onValueChange = { startAngle = it },
                decimals = 0,
            )
            SliderRow(
                label = "Sweep angle",
                value = sweepAngle,
                valueRange = 90f..360f,
                onValueChange = { sweepAngle = it },
                decimals = 0,
            )
            SliderRow(
                label = "Track width",
                value = trackWidthFraction,
                valueRange = 0.02f..0.4f,
                onValueChange = { trackWidthFraction = it },
                decimals = 2,
            )
            SwitchRow(label = "Plot bands", checked = showBands, onCheckedChange = { showBands = it })
            SwitchRow(label = "Value label", checked = showValueLabel, onCheckedChange = { showValueLabel = it })
            ControlSection(title = "Color")
            ColorRow(label = "Progress & needle", selected = color, onSelect = { color = it })
        },
    )
}
