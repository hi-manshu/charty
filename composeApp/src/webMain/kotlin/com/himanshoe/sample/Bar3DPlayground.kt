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
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.bar.Bar3DChart
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.projection.Projection3D
import kotlin.random.Random

internal enum class ProjectionPreset(
    val label: String,
    val projection: Projection3D,
) {
    Default(label = "Default", projection = Projection3D.Default),
    Isometric(label = "Isometric", projection = Projection3D.Isometric),
    Subtle(label = "Subtle", projection = Projection3D.Subtle),
    Dramatic(label = "Dramatic", projection = Projection3D.Dramatic),
    Custom(label = "Custom", projection = Projection3D.Default),
}

private fun demoBars(tick: Int): List<BarData> {
    val random = Random(seed = tick * 41 + 11)
    return listOf("Q1", "Q2", "Q3", "Q4", "Q5").map { label ->
        BarData(label = label, value = 30f + random.nextFloat() * 70f)
    }
}

/**
 * Interactive demo for [Bar3DChart]: preset and hand-tuned viewing angles, bar depth and width,
 * value labels, the floor plane, and a tap readout proving the hit-test picks the visible bar.
 */
@Composable
internal fun Bar3DPlayground() {
    var preset by remember { mutableStateOf(ProjectionPreset.Default) }
    var pitch by remember { mutableStateOf(18) }
    var yaw by remember { mutableStateOf(14) }
    var perspective by remember { mutableStateOf(0) }
    var depthFraction by remember { mutableStateOf(45) }
    var widthFraction by remember { mutableStateOf(62) }
    var showValueLabels by remember { mutableStateOf(false) }
    var showFloor by remember { mutableStateOf(true) }
    var tick by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf<BarData?>(null) }

    val bars = remember(tick) { demoBars(tick) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled
    val projection =
        if (preset == ProjectionPreset.Custom) {
            Projection3D(pitch = pitch.toFloat(), yaw = yaw.toFloat(), perspective = perspective / 100f)
        } else {
            preset.projection
        }
    val color = ChartyColor.Solid(playgroundPalette[4])

    val projectionCode =
        if (preset == ProjectionPreset.Custom) {
            "Projection3D(pitch = ${fc(pitch.toFloat())}, yaw = ${fc(yaw.toFloat())}, perspective = ${fc(
                perspective / 100f,
            )})"
        } else {
            "Projection3D.${preset.name}"
        }
    val code =
        """
        // Projected 3D bars — depth is decoration, not a third variable.
        Bar3DChart(
            data = { bars },                    // List<BarData>(label, value)
            color = ChartyColor.Solid(Color(${colorHex(playgroundPalette[4])})),
            barConfig = Bar3DChartConfig(
                projection = $projectionCode,
                barWidthFraction = ${fc(widthFraction / 100f)},
                barDepthFraction = ${fc(depthFraction / 100f)},
                showValueLabels = $showValueLabels,
                showFloor = $showFloor,
            ),
            onBarClick = { bar -> /* ${clicked?.label ?: "tap a bar"} */ },
        )
        """.trimIndent()

    PlaygroundScaffold(
        cartesian = false,
        code = code,
        chart = {
            Bar3DChart(
                data = { bars },
                modifier = Modifier.fillMaxSize(),
                color = color,
                barConfig =
                    Bar3DChartConfig(
                        projection = projection,
                        barWidthFraction = widthFraction / 100f,
                        barDepthFraction = depthFraction / 100f,
                        animation = animation,
                        showValueLabels = showValueLabels,
                        showFloor = showFloor,
                    ),
                onBarClick = { clicked = it },
            )
        },
        controls = {
            ControlSection(title = "Viewing angle")
            ChoiceRow(
                label = "Preset",
                options = ProjectionPreset.entries.toList(),
                selected = preset,
                labelOf = { it.label },
                onSelect = { preset = it },
            )
            if (preset == ProjectionPreset.Custom) {
                IntSliderRow(label = "Pitch (°)", value = pitch, valueRange = -45..60, onValueChange = { pitch = it })
                IntSliderRow(label = "Yaw (°)", value = yaw, valueRange = -60..60, onValueChange = { yaw = it })
                IntSliderRow(
                    label = "Perspective (%)",
                    value = perspective,
                    valueRange = 0..100,
                    onValueChange = { perspective = it },
                )
            }
            Text(
                text =
                    if (projection.perspective > 0f) {
                        "Perspective is on, so bars of equal value now draw at different lengths depending on where they sit. Good for a slide, bad for comparing."
                    } else {
                        "A parallel projection: equal values draw at equal lengths wherever they sit, so bars stay comparable."
                    },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Bars")
            IntSliderRow(
                label = "Width (%)",
                value = widthFraction,
                valueRange = 20..95,
                onValueChange = { widthFraction = it },
            )
            IntSliderRow(
                label = "Depth (% of width)",
                value = depthFraction,
                valueRange = 0..120,
                onValueChange = { depthFraction = it },
            )
            SwitchRow(label = "Value labels", checked = showValueLabels, onCheckedChange = { showValueLabels = it })
            SwitchRow(label = "Floor plane", checked = showFloor, onCheckedChange = { showFloor = it })
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(
                text =
                    clicked?.let { bar -> "${bar.label} → ${bar.value.toInt()}" }
                        ?: "Tap a bar — the hit-test resolves nearest face first",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
