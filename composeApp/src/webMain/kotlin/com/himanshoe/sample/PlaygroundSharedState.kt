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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.axis.LabelRotation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
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
    fun theme(dark: Boolean): ChartyTheme? =
        if (!themed) {
            null
        } else {
            val base =
                if (dark) {
                    ChartyTheme.dark()
                } else {
                    ChartyTheme.light()
                }
            base.copy(
                primaryColor = ChartyColor.Solid(themePrimary),
                palette = playgroundPalette.map { hue -> ChartyColor.Solid(hue) },
            )
        }

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
