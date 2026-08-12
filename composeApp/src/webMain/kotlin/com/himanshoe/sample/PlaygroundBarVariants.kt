@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "CyclomaticComplexMethod",
    "ktlint:standard:max-line-length",
)

package com.himanshoe.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.bar.GroupedHorizontalBarChart
import com.himanshoe.charty.bar.HorizontalBarChart
import com.himanshoe.charty.bar.StackedBarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.CornerRadius
import kotlin.random.Random

private fun randomBarGroups(
    groups: Int,
    series: Int,
    tick: Int,
): List<BarGroup> {
    val random = Random(seed = tick * 1000 + groups * 10 + series)
    return List(groups) { g -> BarGroup(label = "G${g + 1}", values = List(series) { 20f + random.nextFloat() * 80f }) }
}

private fun groupsCode(data: List<BarGroup>): String =
    data.joinToString(
        ",\n            ",
    ) { g -> "BarGroup(\"${g.label}\", listOf(${g.values.joinToString(", ") { fc(it) }}))" }

private class HorizontalBarState {
    var bars by mutableStateOf(6)
    var tick by mutableStateOf(0)
    var negatives by mutableStateOf(false)
    var barWidthFraction by mutableStateOf(0.6f)
    var barSpacing by mutableStateOf(0f)
    var corner by mutableStateOf<CornerRadius>(CornerRadius.Medium)
    var color by mutableStateOf(playgroundPalette[3])
    var showDataLabels by mutableStateOf(false)
    var animateValueChanges by mutableStateOf(false)
    var negativeMode by mutableStateOf(NegativeValuesDrawMode.BELOW_AXIS)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(60f)
    var markers by mutableStateOf(false)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(5)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun HorizontalBarPlayground() {
    val state = remember { HorizontalBarState() }
    val animate = LocalPlaygroundAnimate.current
    val series =
        remember(state.bars, state.tick, state.negatives) {
            signedValues(count = state.bars, tick = state.tick, negatives = state.negatives)
                .mapIndexed { i, v -> BarData(label = "B${i + 1}", value = v) }
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            series
        }

    val code =
        """
        val data = listOf(${series.joinToString(", ") { fc(it.value) }})
            .mapIndexed { i, v -> BarData("B${'$'}{i + 1}", v) }

        HorizontalBarChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            color = ChartyColor.Solid(Color(${colorHex(state.color)})),
            barConfig = BarChartConfig(
                barWidthFraction = ${fc(state.barWidthFraction)},
                barSpacing = ${fc(state.barSpacing)},
                cornerRadius = CornerRadius.${cornerName(state.corner)},${codeArg(
            include = state.negatives,
            text = "negativeValuesDrawMode = NegativeValuesDrawMode.${state.negativeMode.name},",
            indent = 16,
        )}${codeArg(
            include = state.showDataLabels,
            text = "showDataLabels = true,",
            indent = 16,
        )}${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${series.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            HorizontalBarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                color = ChartyColor.Solid(state.color),
                barConfig =
                    BarChartConfig(
                        barWidthFraction = state.barWidthFraction,
                        barSpacing = state.barSpacing,
                        cornerRadius = state.corner,
                        negativeValuesDrawMode = state.negativeMode,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        markers = demoMarkers(enabled = state.markers, size = series.size, label = "Peak"),
                        showDataLabels = state.showDataLabels,
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { HorizontalBarControls(state = state) },
    )
}

@Composable
private fun HorizontalBarControls(state: HorizontalBarState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Bars", value = state.bars, valueRange = 2..15, onValueChange = { state.bars = it })
    SwitchRow(label = "Include negatives", checked = state.negatives, onCheckedChange = { state.negatives = it })
    if (state.negatives) {
        ChoiceRow(
            label = "Negative values",
            options = NegativeValuesDrawMode.entries.toList(),
            selected = state.negativeMode,
            labelOf = { it.name },
            onSelect = { state.negativeMode = it },
        )
    }
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Bars")
    SliderRow(
        label = "Width fraction",
        value = state.barWidthFraction,
        valueRange = 0.1f..1f,
        onValueChange = { state.barWidthFraction = it },
        decimals = 2,
    )
    SliderRow(
        label = "Bar spacing",
        value = state.barSpacing,
        valueRange = 0f..24f,
        onValueChange = { state.barSpacing = it },
        decimals = 0,
    )
    CornerRadiusControls(
        label = "Corner radius",
        corner = state.corner,
        onCornerChange = { state.corner = it },
    )
    SwitchRow(label = "Value labels", checked = state.showDataLabels, onCheckedChange = { state.showDataLabels = it })
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..100f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    ControlSection(title = "Behaviour")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..15,
    )
    ControlSection(title = "Color")
    ColorRow(label = "Bar color", selected = state.color, onSelect = { state.color = it })
}

private class StackedBarState {
    var groups by mutableStateOf(6)
    var series by mutableStateOf(3)
    var tick by mutableStateOf(0)
    var barWidthFraction by mutableStateOf(0.6f)
    var barSpacing by mutableStateOf(0f)
    var corner by mutableStateOf<CornerRadius>(CornerRadius.Medium)
    var showDataLabels by mutableStateOf(false)
    var animateValueChanges by mutableStateOf(false)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(150f)
    var markers by mutableStateOf(false)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(4)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun StackedBarPlayground() {
    val state = remember { StackedBarState() }
    val animate = LocalPlaygroundAnimate.current
    val groups =
        remember(state.groups, state.series, state.tick) {
            randomBarGroups(groups = state.groups, series = state.series, tick = state.tick)
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            groups
        }

    val code =
        """
        val data = listOf(
            ${groupsCode(data = groups)},
        )

        StackedBarChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            colors = ChartyColor.Gradient(palette),
            stackedConfig = StackedBarChartConfig(
                barWidthFraction = ${fc(state.barWidthFraction)},
                barSpacing = ${fc(state.barSpacing)},
                topCornerRadius = CornerRadius.${cornerName(state.corner)},${codeArg(
            include = state.showDataLabels,
            text = "showDataLabels = true,",
            indent = 16,
        )}${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${groups.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            StackedBarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                colors = ChartyColor.Gradient(playgroundPalette.take(state.series.coerceAtLeast(2))),
                stackedConfig =
                    StackedBarChartConfig(
                        barWidthFraction = state.barWidthFraction,
                        barSpacing = state.barSpacing,
                        topCornerRadius = state.corner,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        markers = demoMarkers(enabled = state.markers, size = groups.size, label = "Peak"),
                        showDataLabels = state.showDataLabels,
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { StackedBarControls(state = state) },
    )
}

@Composable
private fun StackedBarControls(state: StackedBarState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Groups", value = state.groups, valueRange = 2..12, onValueChange = { state.groups = it })
    IntSliderRow(label = "Series", value = state.series, valueRange = 2..5, onValueChange = { state.series = it })
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Bars")
    SliderRow(
        label = "Width fraction",
        value = state.barWidthFraction,
        valueRange = 0.2f..1f,
        onValueChange = { state.barWidthFraction = it },
        decimals = 2,
    )
    SliderRow(
        label = "Bar spacing",
        value = state.barSpacing,
        valueRange = 0f..24f,
        onValueChange = { state.barSpacing = it },
        decimals = 0,
    )
    CornerRadiusControls(
        label = "Top corner",
        corner = state.corner,
        onCornerChange = { state.corner = it },
    )
    SwitchRow(
        label = "Total labels",
        checked = state.showDataLabels,
        onCheckedChange = { state.showDataLabels = it },
    )
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..300f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    ControlSection(title = "Behaviour")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..12,
    )
}

private class GroupedBarState {
    var groups by mutableStateOf(5)
    var series by mutableStateOf(3)
    var tick by mutableStateOf(0)
    var barWidthFraction by mutableStateOf(0.8f)
    var barSpacing by mutableStateOf(4f)
    var corner by mutableStateOf<CornerRadius>(CornerRadius.Medium)
    var animateValueChanges by mutableStateOf(false)
    var referenceLine by mutableStateOf(false)
    var referenceValue by mutableStateOf(60f)
    var markers by mutableStateOf(false)
    var window by mutableStateOf(false)
    var windowSize by mutableStateOf(4)
    var forceEmpty by mutableStateOf(false)
    var customEmpty by mutableStateOf(true)
}

@Composable
internal fun GroupedBarPlayground() {
    val state = remember { GroupedBarState() }
    val animate = LocalPlaygroundAnimate.current
    val groups =
        remember(state.groups, state.series, state.tick) {
            randomBarGroups(groups = state.groups, series = state.series, tick = state.tick)
        }
    val data =
        if (state.forceEmpty) {
            emptyList()
        } else {
            groups
        }

    val code =
        """
        val data = listOf(
            ${groupsCode(data = groups)},
        )

        GroupedHorizontalBarChart(
            data = { data },${emptyContentArg(custom = state.customEmpty)}
            colors = ChartyColor.Gradient(palette),
            config = GroupedHorizontalBarChartConfig(
                barWidthFraction = ${fc(state.barWidthFraction)},
                barSpacing = ${fc(state.barSpacing)},
                cornerRadius = CornerRadius.${cornerName(state.corner)},${codeArg(
            include = state.animateValueChanges,
            text = "animateValueChanges = true,",
            indent = 16,
        )}${codeArg(
            include = state.referenceLine,
            text = "referenceLine = ReferenceLineConfig(value = ${fc(state.referenceValue)}, label = \"Target\"),",
            indent = 16,
        )}${codeArg(
            include = state.markers,
            text = "markers = listOf(PersistentMarker(dataIndex = ${groups.size / 2}, label = \"Peak\", showGuideLine = true)),",
            indent = 16,
        )}${codeArg(
            include = state.window,
            text = "visibleWindow = ${state.windowSize},",
            indent = 16,
        )}
            ),
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            GroupedHorizontalBarChart(
                modifier = Modifier.fillMaxSize(),
                data = { data },
                emptyContent = emptyPlaceholder(custom = state.customEmpty, onAction = { state.forceEmpty = false }),
                colors = ChartyColor.Gradient(playgroundPalette.take(state.series.coerceAtLeast(2))),
                config =
                    GroupedHorizontalBarChartConfig(
                        barWidthFraction = state.barWidthFraction,
                        barSpacing = state.barSpacing,
                        cornerRadius = state.corner,
                        animation = playgroundAnimation(animate = animate),
                        animateValueChanges = state.animateValueChanges,
                        referenceLine = demoReferenceLine(enabled = state.referenceLine, value = state.referenceValue),
                        markers = demoMarkers(enabled = state.markers, size = groups.size, label = "Peak"),
                        visibleWindow = windowOf(enabled = state.window, size = state.windowSize),
                    ),
            )
        },
        controls = { GroupedBarControls(state = state) },
    )
}

@Composable
private fun GroupedBarControls(state: GroupedBarState) {
    ControlSection(title = "Data")
    IntSliderRow(label = "Groups", value = state.groups, valueRange = 2..8, onValueChange = { state.groups = it })
    IntSliderRow(label = "Series", value = state.series, valueRange = 2..4, onValueChange = { state.series = it })
    PlaygroundActionRow(primaryLabel = "Randomize", onPrimary = { state.tick++ })
    EmptyStateControls(
        forceEmpty = state.forceEmpty,
        onForceEmptyChange = { state.forceEmpty = it },
        custom = state.customEmpty,
        onCustomChange = { state.customEmpty = it },
    )
    ControlSection(title = "Bars")
    SliderRow(
        label = "Width fraction",
        value = state.barWidthFraction,
        valueRange = 0.3f..1f,
        onValueChange = { state.barWidthFraction = it },
        decimals = 2,
    )
    SliderRow(
        label = "Bar spacing",
        value = state.barSpacing,
        valueRange = 0f..20f,
        onValueChange = { state.barSpacing = it },
        decimals = 0,
    )
    CornerRadiusControls(
        label = "Corner radius",
        corner = state.corner,
        onCornerChange = { state.corner = it },
    )
    ControlSection(title = "Annotations")
    ReferenceLineControls(
        enabled = state.referenceLine,
        onEnabledChange = { state.referenceLine = it },
        value = state.referenceValue,
        onValueChange = { state.referenceValue = it },
        valueRange = 0f..100f,
    )
    SwitchRow(label = "Persistent marker", checked = state.markers, onCheckedChange = { state.markers = it })
    ControlSection(title = "Behaviour")
    SwitchRow(
        label = "Animate value changes",
        checked = state.animateValueChanges,
        onCheckedChange = { state.animateValueChanges = it },
    )
    VisibleWindowControls(
        enabled = state.window,
        onEnabledChange = { state.window = it },
        size = state.windowSize,
        onSizeChange = { state.windowSize = it },
        sizeRange = 2..8,
    )
}
