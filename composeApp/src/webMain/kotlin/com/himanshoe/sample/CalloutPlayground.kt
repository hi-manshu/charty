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
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.AnnotationAnchor
import com.himanshoe.charty.common.annotation.AnnotationShapeKind
import com.himanshoe.charty.common.annotation.CalloutSide
import com.himanshoe.charty.common.annotation.CalloutStyle
import com.himanshoe.charty.common.annotation.ChartAnnotation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

private const val BAND_TOP_VALUE = 100f
private const val BAND_BOTTOM_VALUE = 0f

private val CALLOUT_VALUES =
    listOf(32f, 48f, 41f, 66f, 58f, 82f, 74f, 95f, 61f, 53f, 70f, 88f)

/**
 * Annotation playground: a real [LineChart] carrying a [ChartAnnotation.Callout] anchored to a data
 * point plus a shaded [ChartAnnotation.Shape] band spanning two indices. Both anchors are resolved at
 * draw time, so moving the sliders re-attaches them to the live plot.
 */
@Composable
internal fun CalloutPlayground() {
    var calloutIndex by remember { mutableStateOf(7) }
    var bandStart by remember { mutableStateOf(2) }
    var bandEnd by remember { mutableStateOf(4) }
    var side by remember { mutableStateOf(CalloutSide.AUTO) }
    var filled by remember { mutableStateOf(true) }
    var bubbleColor by remember { mutableStateOf(playgroundPalette[4]) }
    var bandColor by remember { mutableStateOf(playgroundPalette[2]) }

    val data = CALLOUT_VALUES.mapIndexed { index, value -> LineData(label = (index + 1).toString(), value = value) }
    val safeIndex = calloutIndex.coerceIn(0, CALLOUT_VALUES.lastIndex)
    val calloutText = "Point ${safeIndex + 1}: ${CALLOUT_VALUES[safeIndex].toInt()}"

    val code =
        """
        LineChart(
            data = { data },
            interactionConfig = ChartInteractionConfig(
                annotations = listOf(
                    ChartAnnotation.Callout(
                        text = "$calloutText",
                        anchor = AnnotationAnchor.DataPoint(index = $calloutIndex),
                        style = CalloutStyle(
                            bubbleColor = ChartyColor.Solid(bubbleColor),
                            preferredSide = CalloutSide.${side.name},
                        ),
                    ),
                    ChartAnnotation.Shape(
                        from = AnnotationAnchor.Value(x = ${bandStart}f, y = 100f),
                        to = AnnotationAnchor.Value(x = ${bandEnd}f, y = 0f),
                        kind = AnnotationShapeKind.RECT,
                        color = ChartyColor.Solid(bandColor),
                        filled = $filled,
                    ),
                ),
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            CalloutChart(
                data = data,
                calloutText = calloutText,
                calloutIndex = calloutIndex,
                side = side,
                bubbleColor = ChartyColor.Solid(bubbleColor),
                bandStart = bandStart,
                bandEnd = bandEnd,
                bandColor = ChartyColor.Solid(bandColor),
                bandFilled = filled,
            )
        },
        controls = {
            ControlSection(title = "Callout")
            IntSliderRow(
                label = "Anchor data point",
                value = calloutIndex,
                valueRange = 0..CALLOUT_VALUES.lastIndex,
                onValueChange = { calloutIndex = it },
            )
            ChoiceRow(
                label = "Preferred side",
                options = CalloutSide.entries.toList(),
                selected = side,
                labelOf = { it.name },
                onSelect = { side = it },
            )
            ColorRow(label = "Bubble color", selected = bubbleColor, onSelect = { bubbleColor = it })
            ControlSection(title = "Shape")
            IntSliderRow(
                label = "Band start",
                value = bandStart,
                valueRange = 0..CALLOUT_VALUES.lastIndex,
                onValueChange = { bandStart = it },
            )
            IntSliderRow(
                label = "Band end",
                value = bandEnd,
                valueRange = 0..CALLOUT_VALUES.lastIndex,
                onValueChange = { bandEnd = it },
            )
            SwitchRow(label = "Filled", checked = filled, onCheckedChange = { filled = it })
            ColorRow(label = "Band color", selected = bandColor, onSelect = { bandColor = it })
            ControlSection(title = "Reset")
            PlaygroundActionRow(
                primaryLabel = "Defaults",
                onPrimary = {
                    calloutIndex = 7
                    bandStart = 2
                    bandEnd = 4
                    side = CalloutSide.AUTO
                    filled = true
                },
            )
        },
    )
}

@Composable
private fun CalloutChart(
    data: List<LineData>,
    calloutText: String,
    calloutIndex: Int,
    side: CalloutSide,
    bubbleColor: ChartyColor,
    bandStart: Int,
    bandEnd: Int,
    bandColor: ChartyColor,
    bandFilled: Boolean,
) {
    LineChart(
        data = { data },
        modifier = Modifier.fillMaxSize(),
        color = ChartyColor.Solid(playgroundPalette[0]),
        lineConfig = LineChartConfig(smoothCurve = true, showPoints = true),
        interactionConfig =
            ChartInteractionConfig(
                annotations =
                    listOf(
                        ChartAnnotation.Shape(
                            from = AnnotationAnchor.Value(x = bandStart.toFloat(), y = BAND_TOP_VALUE),
                            to = AnnotationAnchor.Value(x = bandEnd.toFloat(), y = BAND_BOTTOM_VALUE),
                            kind = AnnotationShapeKind.RECT,
                            color = bandColor,
                            filled = bandFilled,
                        ),
                        ChartAnnotation.Callout(
                            text = calloutText,
                            anchor = AnnotationAnchor.DataPoint(index = calloutIndex),
                            style = CalloutStyle(bubbleColor = bubbleColor, preferredSide = side),
                        ),
                    ),
            ),
    )
}
