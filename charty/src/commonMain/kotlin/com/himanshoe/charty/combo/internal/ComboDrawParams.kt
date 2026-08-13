package com.himanshoe.charty.combo.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Everything [drawComboContent] needs for one canvas pass, bundled so the drawing entry point keeps
 * a single parameter.
 *
 * @property dataList The points being drawn, each carrying its bar value and its line value.
 * @property chartContext The pixel bounds and value range of the plotting area.
 * @property comboConfig The bar/line styling, markers, reference band and line, and crosshair setup.
 * @property barColor The bars' colour or gradient.
 * @property lineColor The line's colour or gradient.
 * @property minValue The primary axis minimum, which decides where the bars' baseline sits.
 * @property lineRange The secondary axis `(min, max)` the line is scaled against, or `null` when the
 *   line shares the primary axis.
 * @property isBelowAxisMode Whether negative bars are drawn below a zero axis rather than clamped.
 * @property animationProgress The reveal progress, from `0f` (nothing drawn) to `1f` (fully drawn).
 * @property recordDataBounds Whether the pass collects [dataBounds]; `false` skips the bookkeeping
 *   when nothing hit-tests them.
 * @property dataBounds Collects each drawn bar and point rect with its data, for tap hit-testing.
 * @property crosshairBounds Collects the line points the crosshair snaps to, or `null` when off.
 * @property crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @property crosshairState The crosshair's resolved position, or `null` when it is not showing.
 * @property tooltipState The active tooltip, or `null` when none is showing.
 * @property drawTooltipBubble Whether the active tooltip is drawn as the built-in canvas bubble; a
 *   Compose-overlay tooltip is hosted above the canvas instead.
 * @property textMeasurer Measurer used for the reference band, markers, tooltip, and crosshair.
 * @property interactionConfig Supplies the annotation, brush-selection, and edge-fade overlays.
 * @property drawCrosshairLabel Whether the crosshair label is drawn on the canvas rather than as a
 *   composable overlay above it.
 * @property tooltipConfig Styling for the tooltip bubble, already resolved against the theme.
 */
internal data class ComboDrawParams(
    val dataList: List<ComboChartData>,
    val chartContext: ChartContext,
    val comboConfig: ComboChartConfig,
    val barColor: ChartyColor,
    val lineColor: ChartyColor,
    val minValue: Float,
    val lineRange: Pair<Float, Float>?,
    val isBelowAxisMode: Boolean,
    val animationProgress: Float,
    val recordDataBounds: Boolean,
    val dataBounds: MutableList<Pair<Rect, ComboChartData>>,
    val crosshairBounds: MutableList<Pair<Offset, ComboChartData>>?,
    val crosshairManager: CrosshairManager<ComboChartData>?,
    val crosshairState: CrosshairState?,
    val tooltipState: TooltipState?,
    val drawTooltipBubble: Boolean,
    val textMeasurer: TextMeasurer,
    val interactionConfig: ChartInteractionConfig,
    val drawCrosshairLabel: Boolean,
    val tooltipConfig: TooltipConfig,
)
