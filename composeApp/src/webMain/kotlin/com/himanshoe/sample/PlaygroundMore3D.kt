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
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty3d.matrix.Bar3DMatrixChart
import com.himanshoe.charty3d.matrix.config.Bar3DMatrixConfig
import com.himanshoe.charty3d.ribbon.Ribbon3DChart
import com.himanshoe.charty3d.ribbon.config.Ribbon3DChartConfig
import com.himanshoe.charty3d.surface.Surface3DChart
import com.himanshoe.charty3d.surface.config.Surface3DChartConfig
import com.himanshoe.charty3d.surface.config.SurfaceShading
import com.himanshoe.charty3d.surface.data.SurfaceData
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** The three projected charts whose depth carries something other than decoration. */
internal enum class More3DVariant(
    val label: String,
    val blurb: String,
) {
    Matrix(
        label = "Bar matrix",
        blurb = "Both floor axes are variables and the height is a third. A heatmap you can also read by height.",
    ),
    Surface(
        label = "Surface",
        blurb = "A height sampled over a grid — the one figure that genuinely cannot be flattened.",
    ),
    Ribbon(
        label = "Ribbons",
        blurb = "Series pulled onto their own planes. Depth encodes which series, which beats six lines crossing.",
    ),
}

private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
private val SLOTS = listOf("06", "10", "14", "18", "22")

private fun matrixCells(tick: Int): List<HeatmapCell> {
    val random = Random(seed = tick * 19 + 4)
    return DAYS.flatMap { day ->
        SLOTS.map { slot ->
            HeatmapCell(rowLabel = day, columnLabel = slot, value = 5f + random.nextFloat() * 45f)
        }
    }
}

private fun surfaceGrid(tick: Int): SurfaceData {
    val phase = tick * 0.4f
    return SurfaceData.of(rowCount = 22, columnCount = 22) { row, column ->
        sin(row / 3.4f + phase) * cos(column / 3.4f) + sin((row + column) / 7f)
    }
}

private fun ribbonSeries(tick: Int): List<LineGroup> {
    val random = Random(seed = tick * 23 + 6)
    return listOf("EU", "US", "APAC", "LATAM").map { name ->
        LineGroup(label = name, values = List(12) { 10f + random.nextFloat() * 50f })
    }
}

/** Interactive demo for the three projected charts that arrived after the first 3D batch. */
@Composable
internal fun More3DPlayground() {
    var variant by remember { mutableStateOf(More3DVariant.Matrix) }
    var preset by remember { mutableStateOf(ProjectionPreset.Isometric) }
    var wireframe by remember { mutableStateOf(true) }
    var slopeShading by remember { mutableStateOf(true) }
    var fillUnder by remember { mutableStateOf(true) }
    var tick by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf<String?>(null) }

    val cells = remember(tick) { matrixCells(tick) }
    val surface = remember(tick) { surfaceGrid(tick) }
    val series = remember(tick) { ribbonSeries(tick) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled

    val code =
        """
        // ${variant.blurb}
        ${variant.label.replace(" ", "")}3DChart(
            data = { data },
            modifier = Modifier.fillMaxWidth().height(340.dp),
            // projection = Projection3D.${preset.name}
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        cartesian = false,
        chart = {
            val chartModifier = Modifier.fillMaxSize()
            when (variant) {
                More3DVariant.Matrix ->
                    Bar3DMatrixChart(
                        data = { cells },
                        modifier = chartModifier,
                        matrixConfig =
                            Bar3DMatrixConfig(projection = preset.projection, animation = animation),
                        onCellClick = { clicked = "${it.rowLabel} × ${it.columnLabel} → ${it.value.toInt()}" },
                    )

                More3DVariant.Surface ->
                    Surface3DChart(
                        data = { surface },
                        modifier = chartModifier,
                        surfaceConfig =
                            Surface3DChartConfig(
                                projection = preset.projection,
                                showWireframe = wireframe,
                                shading =
                                    if (slopeShading) {
                                        SurfaceShading.HEIGHT_AND_SLOPE
                                    } else {
                                        SurfaceShading.HEIGHT
                                    },
                                animation = animation,
                            ),
                        onCellClick = { height -> clicked = "height ${(height * 100).toInt() / 100f}" },
                    )

                More3DVariant.Ribbon ->
                    Ribbon3DChart(
                        data = { series },
                        modifier = chartModifier,
                        ribbonConfig =
                            Ribbon3DChartConfig(
                                projection = preset.projection,
                                fillUnderRibbon = fillUnder,
                                animation = animation,
                            ),
                        onSegmentClick = { clicked = "${it.seriesName} at ${it.pointIndex} → ${it.value.toInt()}" },
                    )
            }
        },
        controls = {
            ControlSection(title = "Chart")
            ChoiceRow(
                label = "Variant",
                options = More3DVariant.entries.toList(),
                selected = variant,
                labelOf = { it.label },
                onSelect = {
                    variant = it
                    clicked = null
                },
            )
            Text(
                text = variant.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ControlSection(title = "Viewing angle")
            ChoiceRow(
                label = "Preset",
                options = ProjectionPreset.entries.filter { it != ProjectionPreset.Custom },
                selected = preset,
                labelOf = { it.label },
                onSelect = { preset = it },
            )
            when (variant) {
                More3DVariant.Surface -> {
                    ControlSection(title = "Mesh")
                    SwitchRow(label = "Wireframe", checked = wireframe, onCheckedChange = { wireframe = it })
                    SwitchRow(
                        label = "Shade by slope",
                        checked = slopeShading,
                        onCheckedChange = { slopeShading = it },
                    )
                    Text(
                        text = "Turn slope shading off to see why it is on: height alone gives no relief, so two hills of the same height look identical.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                More3DVariant.Ribbon -> {
                    ControlSection(title = "Ribbons")
                    SwitchRow(label = "Fill underneath", checked = fillUnder, onCheckedChange = { fillUnder = it })
                }

                More3DVariant.Matrix -> Unit
            }
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(text = clicked ?: "Tap the chart", style = MaterialTheme.typography.bodySmall)
        },
    )
}
