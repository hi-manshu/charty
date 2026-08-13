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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.gantt.GanttChart
import com.himanshoe.charty.gantt.config.GanttChartConfig
import com.himanshoe.charty.gantt.data.GanttRow
import com.himanshoe.charty.gantt.data.GanttSegment
import com.himanshoe.charty.gantt.data.GanttSelection

private fun demoSchedule(withProgress: Boolean): List<GanttRow> =
    listOf(
        GanttRow(
            label = "Research",
            segments =
                listOf(
                    GanttSegment(
                        startValue = 0f,
                        endValue = 4f,
                        label = "Interviews",
                        progress = 1f.takeIf { withProgress },
                    ),
                ),
        ),
        GanttRow(
            label = "Design",
            segments =
                listOf(
                    GanttSegment(
                        startValue = 3f,
                        endValue = 8f,
                        label = "Wireframes",
                        progress = 0.8f.takeIf { withProgress },
                    ),
                    GanttSegment(
                        startValue = 10f,
                        endValue = 12f,
                        label = "Revision",
                        progress = 0.2f.takeIf { withProgress },
                    ),
                ),
        ),
        GanttRow(
            label = "Build",
            segments =
                listOf(
                    GanttSegment(
                        startValue = 6f,
                        endValue = 15f,
                        label = "Implementation",
                        progress = 0.45f.takeIf { withProgress },
                    ),
                ),
        ),
        GanttRow(
            label = "QA",
            segments =
                listOf(
                    GanttSegment(startValue = 12f, endValue = 17f, progress = 0.1f.takeIf { withProgress }),
                ),
        ),
        GanttRow(
            label = "Launch",
            segments = listOf(GanttSegment(startValue = 17f, endValue = 19f)),
        ),
    )

/**
 * Interactive demo for [GanttChart]: a project schedule with per-segment progress, segment labels,
 * row thickness and corner controls, and a tap readout of the selected segment.
 */
@Composable
internal fun GanttPlayground() {
    var withProgress by remember { mutableStateOf(true) }
    var showSegmentLabels by remember { mutableStateOf(true) }
    var barHeight by remember { mutableIntStateOf(60) }
    var incompleteAlpha by remember { mutableIntStateOf(35) }
    var cornerRadius by remember { mutableStateOf<CornerRadius>(CornerRadius.Small) }
    var showTooltip by remember { mutableStateOf(true) }
    var clicked by remember { mutableStateOf<GanttSelection?>(null) }

    val rows = remember(withProgress) { demoSchedule(withProgress) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled
    val color = ChartyColor.Solid(playgroundPalette[3])

    val code =
        """
        // Project schedule: several segments per row, each with its own progress.
        GanttChart(
            data = { rows },                    // List<GanttRow>(label, segments)
            color = ChartyColor.Solid(Color(${colorHex(playgroundPalette[3])})),
            ganttConfig = GanttChartConfig(
                barHeightFraction = ${fc(barHeight / 100f)},
                cornerRadius = CornerRadius.${cornerName(cornerRadius)},
                incompleteAlpha = ${fc(incompleteAlpha / 100f)},
                showSegmentLabels = $showSegmentLabels,
                valueFormatter = { value -> "Week ${'$'}{value.toInt()}" },
            ),
            onSegmentClick = { selection -> /* ${clicked?.let {
            "${it.row.label} @ ${it.segment.startValue.toInt()}"
        } ?: "tap a segment"} */ },
            tooltip = ${if (showTooltip) "ChartTooltip.canvas()" else "ChartTooltip.none()"},
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            GanttChart(
                data = { rows },
                modifier = Modifier.fillMaxSize(),
                scaffoldConfig = playgroundScaffoldConfig(),
                color = color,
                ganttConfig =
                    GanttChartConfig(
                        barHeightFraction = barHeight / 100f,
                        cornerRadius = cornerRadius,
                        incompleteAlpha = incompleteAlpha / 100f,
                        showSegmentLabels = showSegmentLabels,
                        valueFormatter = { value -> "Week ${value.toInt()}" },
                        animation = animation,
                    ),
                onSegmentClick = { clicked = it },
                tooltip = if (showTooltip) ChartTooltip.canvas() else ChartTooltip.none(),
            )
        },
        controls = {
            ControlSection(title = "Segments")
            SwitchRow(label = "Show progress", checked = withProgress, onCheckedChange = { withProgress = it })
            if (withProgress) {
                IntSliderRow(
                    label = "Incomplete opacity (%)",
                    value = incompleteAlpha,
                    valueRange = 10..90,
                    onValueChange = { incompleteAlpha = it },
                )
            }
            SwitchRow(
                label = "Segment labels",
                checked = showSegmentLabels,
                onCheckedChange = { showSegmentLabels = it },
            )
            Text(
                text = "Labels are measured during composition and drawn only where they fit inside their segment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Rows")
            IntSliderRow(
                label = "Row thickness (%)",
                value = barHeight,
                valueRange = 20..95,
                onValueChange = { barHeight = it },
            )
            CornerRadiusControls(
                label = "Corner radius",
                corner = cornerRadius,
                onCornerChange = { cornerRadius = it },
            )
            ControlSection(title = "Tooltip")
            SwitchRow(label = "Tap tooltip", checked = showTooltip, onCheckedChange = { showTooltip = it })
            ControlSection(title = "Last click")
            Text(
                text =
                    clicked?.let { selection ->
                        val segment = selection.segment
                        "${selection.row.label} · weeks ${segment.startValue.toInt()}–${segment.endValue.toInt()}"
                    } ?: "Tap a segment",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
