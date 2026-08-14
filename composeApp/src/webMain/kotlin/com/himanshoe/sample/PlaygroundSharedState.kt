@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "ktlint:standard:max-line-length",
    "ktlint:standard:function-naming",
    "CompositionLocalAllowlist",
)

package com.himanshoe.sample

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartAnnotation
import com.himanshoe.charty.common.axis.LabelRotation
import com.himanshoe.charty.common.brush.BrushSelectionState
import com.himanshoe.charty.common.brush.rememberBrushSelectionState
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.ScrollEdgeFadeConfig
import com.himanshoe.charty.common.theme.ChartyTheme
import com.himanshoe.charty.common.tooltip.TooltipConfig

/**
 * The settings every Cartesian chart shares — the axis/grid scaffold, the tooltip bubble, and the
 * ambient theme — hoisted once so a single set of controls drives whichever chart is on screen.
 *
 * Each chart playground reads [scaffoldConfig] and passes it to its chart; the tooltip and theme
 * reach the chart through the config it already builds and through `ChartyThemeProvider`.
 */
internal class PlaygroundSharedState {
    var showAxis by mutableStateOf(true)
    var showGrid by mutableStateOf(true)
    var showLabels by mutableStateOf(true)
    var axisColor by mutableStateOf(Color.Black)
    var gridColor by mutableStateOf(Color.LightGray)
    var axisThickness by mutableIntStateOf(2)
    var gridThickness by mutableIntStateOf(1)
    var rotateLabels by mutableStateOf(false)

    var tooltipBackground by mutableStateOf(Color(0xFF2D2D2D))
    var tooltipCorner by mutableIntStateOf(8)
    var tooltipArrow by mutableStateOf(true)
    var tooltipBorder by mutableStateOf(false)
    var tooltipElevation by mutableIntStateOf(4)

    var brushSelection by mutableStateOf(false)
    var annotate by mutableStateOf(false)
    var dragTooltip by mutableStateOf(false)
    var autoScrollToLatest by mutableStateOf(false)
    var edgeFade by mutableStateOf(false)
    var describeForScreenReaders by mutableStateOf(false)
    var lastRange by mutableStateOf<String?>(null)

    var referenceDashed by mutableStateOf(false)
    var referenceLabelBelow by mutableStateOf(false)
    var bandBordered by mutableStateOf(false)

    var themed by mutableStateOf(false)
    var themePrimary by mutableStateOf(playgroundPalette[0])

    /** The scaffold config the controls currently describe. */
    val scaffoldConfig: ChartScaffoldConfig
        get() =
            ChartScaffoldConfig(
                showAxis = showAxis,
                showGrid = showGrid,
                showLabels = showLabels,
                axisColor = ChartyColor.Solid(axisColor),
                gridColor = ChartyColor.Solid(gridColor),
                axisThickness = axisThickness.toFloat(),
                gridThickness = gridThickness.toFloat(),
                leftLabelRotation =
                    if (rotateLabels) {
                        LabelRotation.Angle45
                    } else {
                        LabelRotation.Straight
                    },
            )

    /** The tooltip styling the controls currently describe, or `null` to let the theme decide. */
    val tooltipConfig: TooltipConfig
        get() =
            TooltipConfig(
                backgroundColor = ChartyColor.Solid(tooltipBackground),
                cornerRadius = tooltipCorner.dp,
                borderColor =
                    if (tooltipBorder) {
                        ChartyColor.Solid(Color.White)
                    } else {
                        null
                    },
                elevation = tooltipElevation.dp,
                showArrow = tooltipArrow,
            )

    /** The theme to provide around the chart, or `null` to leave Charty's own defaults in place. */
    fun theme(dark: Boolean): ChartyTheme {
        val base =
            if (dark) {
                ChartyTheme.dark()
            } else {
                ChartyTheme.light()
            }
        val palette =
            if (themed) {
                base.copy(
                    primaryColor = ChartyColor.Solid(themePrimary),
                    palette = playgroundPalette.map { hue -> ChartyColor.Solid(hue) },
                )
            } else {
                base
            }
        return palette.withTooltipStyle(this)
    }

    /**
     * Folds the tooltip controls into the theme, which is how they reach a chart at all.
     *
     * Tooltip styling lives on each chart's own config, so a shared panel has nowhere to send it —
     * which is why these five controls used to adjust nothing. But a config left unset resolves
     * against the ambient theme, and the theme carries every one of these values. Putting them here
     * reaches all thirty-five charts at once, and does it the way the library intends rather than by
     * threading an argument through thirty-five call sites.
     */
    private fun ChartyTheme.withTooltipStyle(state: PlaygroundSharedState): ChartyTheme =
        copy(
            componentColors = componentColors.copy(tooltipBackground = ChartyColor.Solid(state.tooltipBackground)),
            shapes =
                shapes.copy(
                    tooltip = RoundedCornerShape(state.tooltipCorner.dp),
                    tooltipCornerRadius = state.tooltipCorner.dp,
                ),
            dimensions =
                dimensions.copy(
                    tooltipElevation = state.tooltipElevation.dp,
                    tooltipBorderWidth =
                        if (state.tooltipBorder) {
                            dimensions.tooltipBorderWidth
                        } else {
                            0.dp
                        },
                    tooltipArrowSize =
                        if (state.tooltipArrow) {
                            dimensions.tooltipArrowSize
                        } else {
                            0.dp
                        },
                ),
        )

    /**
     * The interaction settings the controls currently describe, given the brush state the chart owns.
     *
     * Everything here is a capability a chart already has and that nothing in the playground used to
     * reach: dragging to select a range, pinning a note to a point, scrubbing a tooltip, following a
     * live feed, fading the scroll edges. A feature no one can find is not shipped.
     *
     * @param brushState The chart's own brush selection state, needed because brush selection is
     *   hoisted rather than owned by the config.
     * @param pointCount How many points the chart is drawing, so a demo annotation lands on one.
     */
    fun interactionConfig(
        brushState: BrushSelectionState,
        pointCount: Int,
    ): ChartInteractionConfig =
        ChartInteractionConfig(
            brushSelectionState =
                if (brushSelection) {
                    brushState
                } else {
                    null
                },
            onRangeSelect =
                if (brushSelection) {
                    { start, end -> lastRange = "points $start to $end" }
                } else {
                    null
                },
            annotations =
                if (annotate) {
                    // The first point, not the middle one. Most screens call this without a count —
                    // the parameter defaults to zero — so "middle" resolved to index 0 anyway and the
                    // label was describing something that never happened. Annotating the first point
                    // is true on every screen, whether or not the caller knows how many there are.
                    listOf(ChartAnnotation(xIndex = 0, label = "Note"))
                } else {
                    emptyList()
                },
            accessibilityDescription =
                if (describeForScreenReaders) {
                    "A demonstration chart with $pointCount points"
                } else {
                    null
                },
            dragTooltipEnabled = dragTooltip,
            autoScrollToLatest = autoScrollToLatest,
            edgeFade =
                if (edgeFade) {
                    ScrollEdgeFadeConfig()
                } else {
                    null
                },
        )

    /** Restores every shared control to the value a chart would have without the playground. */
    fun reset() {
        showAxis = true
        showGrid = true
        showLabels = true
        axisColor = Color.Black
        gridColor = Color.LightGray
        axisThickness = 2
        gridThickness = 1
        rotateLabels = false
        tooltipBackground = Color(0xFF2D2D2D)
        tooltipCorner = 8
        tooltipArrow = true
        tooltipBorder = false
        tooltipElevation = 4
        referenceDashed = false
        referenceLabelBelow = false
        bandBordered = false
        brushSelection = false
        annotate = false
        dragTooltip = false
        autoScrollToLatest = false
        edgeFade = false
        describeForScreenReaders = false
        lastRange = null
        themed = false
        themePrimary = playgroundPalette[0]
    }
}

/** The shared settings for the chart currently on screen. */
internal val LocalPlaygroundShared = staticCompositionLocalOf { PlaygroundSharedState() }

/**
 * The scaffold config a chart playground should hand to its chart, so the shared axis and grid
 * controls reach it.
 */
@Composable
internal fun playgroundScaffoldConfig(): ChartScaffoldConfig = LocalPlaygroundShared.current.scaffoldConfig

/** The tooltip styling a chart playground should hand to its config. */
@Composable
internal fun playgroundTooltipConfig(): TooltipConfig = LocalPlaygroundShared.current.tooltipConfig

/** Controls for every [ChartScaffoldConfig] property, shared by every Cartesian chart. */
@Composable
internal fun AxisAndGridControls(state: PlaygroundSharedState) {
    ControlSection(title = "Axes and grid")
    SwitchRow(label = "Show axis", checked = state.showAxis, onCheckedChange = { state.showAxis = it })
    SwitchRow(label = "Show grid", checked = state.showGrid, onCheckedChange = { state.showGrid = it })
    SwitchRow(label = "Show labels", checked = state.showLabels, onCheckedChange = { state.showLabels = it })
    SwitchRow(label = "Rotate left labels 45°", checked = state.rotateLabels, onCheckedChange = {
        state.rotateLabels =
            it
    })
    IntSliderRow(
        label = "Axis thickness",
        value = state.axisThickness,
        valueRange = 0..8,
        onValueChange = { state.axisThickness = it },
    )
    IntSliderRow(
        label = "Grid thickness",
        value = state.gridThickness,
        valueRange = 0..8,
        onValueChange = { state.gridThickness = it },
    )
    ColorRow(label = "Axis colour", selected = state.axisColor, onSelect = { state.axisColor = it })
    ColorRow(label = "Grid colour", selected = state.gridColor, onSelect = { state.gridColor = it })
}

/** Controls for the shared [TooltipConfig], which almost every chart draws its tap bubble with. */
@Composable
internal fun TooltipStyleControls(state: PlaygroundSharedState) {
    ControlSection(title = "Tooltip style")
    SwitchRow(label = "Arrow", checked = state.tooltipArrow, onCheckedChange = { state.tooltipArrow = it })
    SwitchRow(label = "Border", checked = state.tooltipBorder, onCheckedChange = { state.tooltipBorder = it })
    IntSliderRow(
        label = "Corner radius (dp)",
        value = state.tooltipCorner,
        valueRange = 0..24,
        onValueChange = { state.tooltipCorner = it },
    )
    IntSliderRow(
        label = "Elevation (dp)",
        value = state.tooltipElevation,
        valueRange = 0..16,
        onValueChange = { state.tooltipElevation = it },
    )
    ColorRow(label = "Background", selected = state.tooltipBackground, onSelect = { state.tooltipBackground = it })
}

/** Controls for the ambient [ChartyTheme], so a design system can be tried on the live chart. */
@Composable
internal fun ThemeControls(state: PlaygroundSharedState) {
    ControlSection(title = "Theme")
    SwitchRow(
        label = "Provide a ChartyTheme",
        checked = state.themed,
        onCheckedChange = { state.themed = it },
    )
    if (state.themed) {
        ColorRow(label = "Primary", selected = state.themePrimary, onSelect = { state.themePrimary = it })
    }
}

/**
 * Controls for the parts of [ChartInteractionConfig] the playground never reached.
 *
 * These are all capabilities the charts already had. Brush selection, annotations, drag-to-scrub and
 * the scroll-edge fade were reachable only by reading the source, which is the same as not shipping
 * them.
 */
@Composable
internal fun InteractionControls(state: PlaygroundSharedState) {
    ControlSection(title = "Interaction")
    SwitchRow(
        label = "Drag to select a range",
        checked = state.brushSelection,
        onCheckedChange = { state.brushSelection = it },
    )
    if (state.brushSelection && state.lastRange != null) {
        Text(
            text = "Selected ${state.lastRange}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    SwitchRow(
        label = "Annotate the first point",
        checked = state.annotate,
        onCheckedChange = { state.annotate = it },
    )
    SwitchRow(
        label = "Drag to scrub the tooltip",
        checked = state.dragTooltip,
        onCheckedChange = { state.dragTooltip = it },
    )
    // "Fade the scroll edges" and "Follow the newest point" are not offered here. Both only take
    // effect when the chart has a viewport — the library guards each on `viewPortState != null` —
    // and this panel supplies none, so the switches moved but nothing ever did. They belong on a
    // screen that sets one up, not on every screen as controls that quietly do nothing.
    SwitchRow(
        label = "Screen-reader description",
        checked = state.describeForScreenReaders,
        onCheckedChange = { state.describeForScreenReaders = it },
    )
}

/**
 * The interaction config a chart playground should hand to its chart, so the shared interaction
 * controls reach it.
 *
 * @param pointCount How many points the chart draws, so a demo annotation lands on a real one.
 *   Left at zero the annotation pins to the first point, which every non-empty chart has.
 */
@Composable
internal fun playgroundInteractionConfig(pointCount: Int = 0): ChartInteractionConfig {
    val brushState = rememberBrushSelectionState()
    return LocalPlaygroundShared.current.interactionConfig(brushState = brushState, pointCount = pointCount)
}

/**
 * The shared interaction settings, laid over a config a screen has already built for itself.
 *
 * The streaming and synced screens each construct a [ChartInteractionConfig] carrying something only
 * they know about — a streaming state, a viewport, a jump-to-latest slot. They used to build it and
 * stop there, which left the shared switches beside them doing nothing at all. Copying the shared
 * settings onto that config keeps what the screen needs and adds back what the panel promises.
 */
@Composable
internal fun withPlaygroundInteractions(
    base: ChartInteractionConfig,
    pointCount: Int = 0,
): ChartInteractionConfig {
    val shared = playgroundInteractionConfig(pointCount = pointCount)
    return base.copy(
        brushSelectionState = shared.brushSelectionState,
        onRangeSelect = shared.onRangeSelect,
        annotations = shared.annotations,
        accessibilityDescription = shared.accessibilityDescription ?: base.accessibilityDescription,
        dragTooltipEnabled = shared.dragTooltipEnabled,
    )
}
