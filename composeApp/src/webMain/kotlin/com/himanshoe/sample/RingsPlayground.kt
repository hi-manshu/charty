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
import com.himanshoe.charty.circular.rings.ActivityRingsChart
import com.himanshoe.charty.circular.rings.config.ActivityRingsConfig
import com.himanshoe.charty.circular.rings.data.RingData
import com.himanshoe.charty.common.config.Animation

private const val RING_TARGET = 100f

/**
 * Interactive demo of [ActivityRingsChart]: three concentric solid-gauge rings with per-ring value
 * sliders, ring thickness control, and animation toggle. Every control maps to a real config or
 * data change and the code panel mirrors the exact call.
 */
@Composable
internal fun RingsPlayground() {
    var moveValue by remember { mutableStateOf(72) }
    var exerciseValue by remember { mutableStateOf(45) }
    var standValue by remember { mutableStateOf(110) }
    var thicknessPercent by remember { mutableStateOf(12) }
    var gapPercent by remember { mutableStateOf(4) }
    var animate by remember { mutableStateOf(true) }
    var roundedCaps by remember { mutableStateOf(true) }
    var showLabels by remember { mutableStateOf(false) }

    val code =
        """
        ActivityRingsChart(
            data = {
                listOf(
                    RingData(label = "Move", value = ${moveValue}f, target = ${fc(RING_TARGET)}),
                    RingData(label = "Exercise", value = ${exerciseValue}f, target = ${fc(RING_TARGET)}),
                    RingData(label = "Stand", value = ${standValue}f, target = ${fc(RING_TARGET)}),
                )
            },
            config = ActivityRingsConfig(
                ringThicknessFraction = ${fc(thicknessPercent / 100f)},
                ringGapFraction = ${fc(gapPercent / 100f)},
                roundedCaps = $roundedCaps,
                showLabels = $showLabels,
                animation = ${if (animate) "Animation.Default" else "Animation.Disabled"},
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            ActivityRingsChart(
                data = {
                    listOf(
                        RingData(label = "Move", value = moveValue.toFloat(), target = RING_TARGET),
                        RingData(label = "Exercise", value = exerciseValue.toFloat(), target = RING_TARGET),
                        RingData(label = "Stand", value = standValue.toFloat(), target = RING_TARGET),
                    )
                },
                modifier = Modifier.fillMaxSize(),
                config =
                    ActivityRingsConfig(
                        ringThicknessFraction = thicknessPercent / 100f,
                        ringGapFraction = gapPercent / 100f,
                        roundedCaps = roundedCaps,
                        showLabels = showLabels,
                        animation =
                            if (animate) {
                                Animation.Default
                            } else {
                                Animation.Disabled
                            },
                    ),
            )
        },
        controls = {
            ControlSection(title = "Ring values (target ${RING_TARGET.toInt()})")
            IntSliderRow(
                label = "Move",
                value = moveValue,
                valueRange = 0..150,
                onValueChange = { moveValue = it },
            )
            IntSliderRow(
                label = "Exercise",
                value = exerciseValue,
                valueRange = 0..150,
                onValueChange = { exerciseValue = it },
            )
            IntSliderRow(
                label = "Stand",
                value = standValue,
                valueRange = 0..150,
                onValueChange = { standValue = it },
            )
            ControlSection(title = "Style")
            IntSliderRow(
                label = "Thickness (% of radius)",
                value = thicknessPercent,
                valueRange = 4..30,
                onValueChange = { thicknessPercent = it },
            )
            IntSliderRow(
                label = "Gap (% of radius)",
                value = gapPercent,
                valueRange = 0..15,
                onValueChange = { gapPercent = it },
            )
            SwitchRow(label = "Rounded caps", checked = roundedCaps, onCheckedChange = { roundedCaps = it })
            SwitchRow(label = "Show labels", checked = showLabels, onCheckedChange = { showLabels = it })
            ControlSection(title = "Animation")
            SwitchRow(label = "Animate sweep", checked = animate, onCheckedChange = { animate = it })
        },
    )
}
