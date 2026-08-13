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
import com.himanshoe.charty3d.scatter.Scatter3DChart
import com.himanshoe.charty3d.scatter.config.Scatter3DChartConfig
import com.himanshoe.charty3d.scatter.data.Scatter3DPoint
import kotlin.random.Random

private fun demoCloud(
    tick: Int,
    count: Int,
): List<Scatter3DPoint> {
    val random = Random(seed = tick * 61 + 7)
    return (1..count).map { index ->
        val base = random.nextFloat()
        Scatter3DPoint(
            x = base * 100f,
            y = base * 60f + random.nextFloat() * 40f,
            z = random.nextFloat() * 100f,
            label = "P$index",
        )
    }
}

/**
 * Interactive demo for [Scatter3DChart]: the one projected chart where depth is a measured variable
 * rather than decoration. Viewing angle, point size and depth scaling, the wireframe box and drop
 * lines, and a tap readout of all three values.
 */
@Composable
internal fun Scatter3DPlayground() {
    var preset by remember { mutableStateOf(ProjectionPreset.Isometric) }
    var count by remember { mutableStateOf(24) }
    var pointRadius by remember { mutableStateOf(7) }
    var depthScaling by remember { mutableStateOf(true) }
    var showBox by remember { mutableStateOf(true) }
    var showDropLines by remember { mutableStateOf(true) }
    var showLabels by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf<Scatter3DPoint?>(null) }

    val cloud = remember(tick, count) { demoCloud(tick = tick, count = count) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled
    val color = ChartyColor.Solid(playgroundPalette[0])

    val code =
        """
        // The one projected chart where depth carries data: z is a measured variable.
        Scatter3DChart(
            data = { observations },            // List<Scatter3DPoint>(x, y, z, label)
            color = ChartyColor.Solid(Color(${colorHex(playgroundPalette[0])})),
            scatterConfig = Scatter3DChartConfig(
                projection = Projection3D.${preset.name},
                pointRadius = ${fc(pointRadius.toFloat())},
                nearScale = ${if (depthScaling) "1.25f" else "1f"},
                farScale = ${if (depthScaling) "0.7f" else "1f"},
                showBox = $showBox,
                showDropLines = $showDropLines,
                showLabels = $showLabels,
            ),
            onPointClick = { point -> /* ${clicked?.label ?: "tap a point"} */ },
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        cartesian = false,
        chart = {
            Scatter3DChart(
                data = { cloud },
                modifier = Modifier.fillMaxSize(),
                color = color,
                scatterConfig =
                    Scatter3DChartConfig(
                        projection = preset.projection,
                        pointRadius = pointRadius.toFloat(),
                        nearScale = if (depthScaling) 1.25f else 1f,
                        farScale = if (depthScaling) 0.7f else 1f,
                        showBox = showBox,
                        showDropLines = showDropLines,
                        showLabels = showLabels,
                        animation = animation,
                    ),
                onPointClick = { clicked = it },
            )
        },
        controls = {
            ControlSection(title = "Viewing angle")
            ChoiceRow(
                label = "Preset",
                options = ProjectionPreset.entries.filter { it != ProjectionPreset.Custom },
                selected = preset,
                labelOf = { it.label },
                onSelect = { preset = it },
            )
            Text(
                text = "Depth is a measured variable here, not decoration — but two points can still land on each other from one angle and separate from another, so the angle is part of what the reader sees.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Points")
            IntSliderRow(label = "Count", value = count, valueRange = 4..80, onValueChange = { count = it })
            IntSliderRow(
                label = "Radius",
                value = pointRadius,
                valueRange = 2..16,
                onValueChange = { pointRadius = it },
            )
            SwitchRow(
                label = "Scale by depth",
                checked = depthScaling,
                onCheckedChange = { depthScaling = it },
            )
            SwitchRow(label = "Point labels", checked = showLabels, onCheckedChange = { showLabels = it })
            ControlSection(title = "Frame")
            SwitchRow(label = "Wireframe box", checked = showBox, onCheckedChange = { showBox = it })
            SwitchRow(label = "Drop lines", checked = showDropLines, onCheckedChange = { showDropLines = it })
            Text(
                text = "Turn both off to see why they are on by default.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(
                text =
                    clicked?.let { point ->
                        "${point.label} → x ${point.x.toInt()}, y ${point.y.toInt()}, z ${point.z.toInt()}"
                    } ?: "Tap a point",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
