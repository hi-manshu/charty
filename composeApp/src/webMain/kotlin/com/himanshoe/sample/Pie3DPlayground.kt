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
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty3d.pie.Pie3DChart
import com.himanshoe.charty3d.pie.config.Pie3DChartConfig
import com.himanshoe.charty3d.pie.config.Pie3DLabelContent
import kotlin.random.Random

private val SEGMENTS = listOf("Direct", "Search", "Social", "Email", "Referral")

private fun demoSlices(tick: Int): List<PieData> {
    val random = Random(seed = tick * 23 + 5)
    return SEGMENTS.map { label ->
        PieData(label = label, value = 10f + random.nextFloat() * 40f)
    }
}

/**
 * Interactive demo for [Pie3DChart]: preset and hand-tuned viewing angles, disc thickness, the ring
 * hole, exploding, label content, and a tap readout proving the hit-test picks the visible slice.
 */
@Composable
internal fun Pie3DPlayground() {
    var preset by remember { mutableStateOf(ProjectionPreset.Default) }
    var thickness by remember { mutableStateOf(18) }
    var hole by remember { mutableStateOf(0) }
    var explode by remember { mutableStateOf(0) }
    var labelContent by remember { mutableStateOf(Pie3DLabelContent.PERCENTAGE) }
    var tick by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf<PieData?>(null) }

    val pieSlices = remember(tick) { demoSlices(tick) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled
    val palette = remember { playgroundPalette.map { hue -> ChartyColor.Solid(hue) } }

    val code =
        """
        // Projected 3D pie — tilt makes near slices show more ink than far ones of equal value.
        Pie3DChart(
            data = { slices },                  // List<PieData>(label, value)
            colors = playgroundPalette.map { ChartyColor.Solid(it) },
            pieConfig = Pie3DChartConfig(
                projection = Projection3D.${preset.name},
                thicknessFraction = ${fc(thickness / 100f)},
                holeFraction = ${fc(hole / 100f)},
                explodeFraction = ${fc(explode / 100f)},
                labelContent = Pie3DLabelContent.${labelContent.name},
            ),
            onSliceClick = { slice -> /* ${clicked?.label ?: "tap a slice"} */ },
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        cartesian = false,
        chart = {
            Pie3DChart(
                data = { pieSlices },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                pieConfig =
                    Pie3DChartConfig(
                        projection = preset.projection,
                        thicknessFraction = thickness / 100f,
                        holeFraction = hole / 100f,
                        explodeFraction = explode / 100f,
                        animation = animation,
                        labelContent = labelContent,
                    ),
                onSliceClick = { clicked = it },
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
                text = "A tilted pie shows the near slices' walls, so they present more ink than equal slices at the back. Read the shares from the labels, not the shapes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Disc")
            IntSliderRow(
                label = "Thickness (% of radius)",
                value = thickness,
                valueRange = 0..40,
                onValueChange = { thickness = it },
            )
            IntSliderRow(label = "Hole (% of radius)", value = hole, valueRange = 0..80, onValueChange = { hole = it })
            IntSliderRow(
                label = "Explode (% of radius)",
                value = explode,
                valueRange = 0..30,
                onValueChange = { explode = it },
            )
            ControlSection(title = "Labels")
            ChoiceRow(
                label = "Content",
                options = Pie3DLabelContent.entries.toList(),
                selected = labelContent,
                labelOf = { it.name.lowercase().replace('_', ' ') },
                onSelect = { labelContent = it },
            )
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(
                text = clicked?.let { slice -> "${slice.label} → ${slice.value.toInt()}" } ?: "Tap a slice",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
