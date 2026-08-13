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
import com.himanshoe.charty.funnel.FunnelChart
import com.himanshoe.charty.funnel.config.FunnelChartConfig
import com.himanshoe.charty.funnel.config.FunnelOrientation
import com.himanshoe.charty.funnel.data.FunnelStage
import kotlin.random.Random

private val STAGE_LABELS = listOf("Visited", "Signed up", "Activated", "Subscribed", "Renewed")

private fun demoStages(tick: Int): List<FunnelStage> {
    val random = Random(seed = tick * 17 + 3)
    var running = 10_000f
    return STAGE_LABELS.mapIndexed { index, label ->
        if (index > 0) {
            running *= 0.32f + random.nextFloat() * 0.4f
        }
        FunnelStage(label = label, value = running.toInt().toFloat())
    }
}

/**
 * Interactive demo for [FunnelChart]: a signup funnel with an orientation switch, stage-gap slider,
 * stage and conversion labels, and a tap readout of the selected stage.
 */
@Composable
internal fun FunnelPlayground() {
    var vertical by remember { mutableStateOf(true) }
    var stageGap by remember { mutableStateOf(4) }
    var showStageLabels by remember { mutableStateOf(true) }
    var showConversionLabels by remember { mutableStateOf(true) }
    var tick by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf<FunnelStage?>(null) }

    val stages = remember(tick) { demoStages(tick) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled
    val orientation = if (vertical) FunnelOrientation.VERTICAL else FunnelOrientation.HORIZONTAL
    val stageColors = remember { playgroundPalette.map { hue -> ChartyColor.Solid(hue) } }

    val code =
        """
        // Signup funnel: each band tapers from its own width to the next stage's.
        FunnelChart(
            data = { stages },                  // List<FunnelStage>(label, value)
            stageColors = listOf(${playgroundPalette.take(
            3,
        ).joinToString { "ChartyColor.Solid(Color(${colorHex(it)}))" }}, …),
            funnelConfig = FunnelChartConfig(
                orientation = FunnelOrientation.${orientation.name},
                stageGap = ${fc(stageGap.toFloat())},
                showStageLabels = $showStageLabels,
                showConversionLabels = $showConversionLabels,
            ),
            onStageClick = { stage -> /* ${clicked?.label ?: "tap a stage"} */ },
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            FunnelChart(
                data = { stages },
                modifier = Modifier.fillMaxSize(),
                stageColors = stageColors,
                funnelConfig =
                    FunnelChartConfig(
                        orientation = orientation,
                        stageGap = stageGap.toFloat(),
                        showStageLabels = showStageLabels,
                        showConversionLabels = showConversionLabels,
                        animation = animation,
                    ),
                onStageClick = { clicked = it },
            )
        },
        controls = {
            ControlSection(title = "Orientation")
            SwitchRow(label = "Vertical", checked = vertical, onCheckedChange = { vertical = it })
            ControlSection(title = "Bands")
            IntSliderRow(
                label = "Stage gap (px)",
                value = stageGap,
                valueRange = 0..24,
                onValueChange = { stageGap = it },
            )
            SwitchRow(
                label = "Stage labels",
                checked = showStageLabels,
                onCheckedChange = { showStageLabels = it },
            )
            SwitchRow(
                label = "Conversion labels",
                checked = showConversionLabels,
                onCheckedChange = { showConversionLabels = it },
            )
            Text(
                text = "Conversion is measured from the previous stage, not from the first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(
                text =
                    clicked?.let { stage -> "${stage.label} → ${stage.value.toInt()}" }
                        ?: "Tap a stage (FunnelChart has no tooltip slot — it is not a Cartesian chart)",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
