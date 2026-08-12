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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.Sparkline
import com.himanshoe.charty.line.config.SparklineConfig
import kotlin.random.Random

private const val WALK_POINTS = 40
private const val WALK_START = 50f
private const val WALK_STEP = 12f

private val sparklineSeriesNames = listOf("Revenue", "Sessions", "Latency", "Errors")

private fun randomWalk(seed: Int): List<Float> {
    val random = Random(seed)
    var current = WALK_START
    return List(WALK_POINTS) {
        current += (random.nextFloat() - 0.5f) * WALK_STEP
        current
    }
}

/**
 * Interactive demo of [Sparkline]: several inline trends over random walks, stacked in a column the
 * way they would sit in a dashboard or table. Every control maps straight onto [SparklineConfig].
 */
@Composable
internal fun SparklinePlayground() {
    var showFill by remember { mutableStateOf(true) }
    var showDot by remember { mutableStateOf(true) }
    var smooth by remember { mutableStateOf(false) }
    var animate by remember { mutableStateOf(false) }
    var lineWidth by remember { mutableStateOf(1.5f) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }
    var tick by remember { mutableStateOf(0) }

    val walks = remember(tick) { sparklineSeriesNames.indices.map { randomWalk(seed = tick * 31 + it) } }
    val config =
        SparklineConfig(
            lineWidth = lineWidth,
            showFill = showFill,
            showLastPointDot = showDot,
            smoothCurve = smooth,
            animation =
                if (animate) {
                    Animation.Fast
                } else {
                    Animation.Disabled
                },
        )

    val code =
        """
        // Inline trend — no axes, labels, padding, or interaction.
        Sparkline(
            data = { values },              // any List<Float>
            modifier = Modifier.fillMaxWidth().height(40.dp),
            color = ChartyColor.Solid(Color(${colorHex(color)})),
            config = SparklineConfig(
                lineWidth = ${fc(lineWidth)},
                showFill = $showFill,
                showLastPointDot = $showDot,
                smoothCurve = $smooth,
                animation = ${if (animate) "Animation.Fast" else "Animation.Disabled"},
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            key(tick) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(space = 14.dp, alignment = Alignment.CenterVertically),
                ) {
                    walks.forEachIndexed { index, walk ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = sparklineSeriesNames[index],
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Sparkline(
                                data = { walk },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                color = ChartyColor.Solid(color),
                                config = config,
                            )
                        }
                    }
                }
            }
        },
        controls = {
            ControlSection(title = "Line")
            SliderRow(
                label = "Line width",
                value = lineWidth,
                valueRange = 0.5f..6f,
                onValueChange = { lineWidth = it },
            )
            SwitchRow(label = "Smooth curve", checked = smooth, onCheckedChange = { smooth = it })
            ControlSection(title = "Accents")
            SwitchRow(label = "Fill under line", checked = showFill, onCheckedChange = { showFill = it })
            SwitchRow(label = "Last point dot", checked = showDot, onCheckedChange = { showDot = it })
            ControlSection(title = "Animation")
            SwitchRow(label = "Animate entry", checked = animate, onCheckedChange = { animate = it })
            ControlSection(title = "Data")
            PlaygroundActionRow(
                primaryLabel = "Reshuffle walks",
                onPrimary = { tick++ },
            )
            ControlSection(title = "Color")
            ColorRow(label = "Line color", selected = color, onSelect = { color = it })
        },
    )
}
