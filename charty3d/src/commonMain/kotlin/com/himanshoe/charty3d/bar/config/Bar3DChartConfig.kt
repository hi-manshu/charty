package com.himanshoe.charty3d.bar.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.projection.Projection3D

private const val DEFAULT_BAR_WIDTH_FRACTION = 0.62f
private const val DEFAULT_BAR_DEPTH_FRACTION = 0.45f
private const val LABEL_SP = 11f

/**
 * Where a bar's category label is drawn.
 *
 * The choice is really about the viewing angle. Head-on or gently tilted, a label under the bar
 * reads exactly like a flat bar chart's axis and belongs there. Swing the yaw far enough — an
 * isometric view especially — and the floor beneath one bar is behind the next one along, so foot
 * labels start colliding with the bars they are meant to name.
 *
 * [AUTO] measures that rather than guessing at it, which is why it is the default.
 */
@ChartyExperimental
enum class Bar3DLabelPlacement {
    /**
     * Let the chart decide, by measuring whether floor labels would actually collide at the angle in
     * use. This is the default, so a caller who simply wants a readable chart gets one at every
     * angle without having to know the rule.
     */
    AUTO,

    /** Always on the floor beneath the bar, where a flat chart's axis label would sit. */
    FLOOR,

    /** Always centred on the bar's top face, which stays clear however far the scene is rotated. */
    TOP_FACE,
}

/**
 * How a [com.himanshoe.charty3d.bar.Bar3DChart] draws its bars.
 *
 * @property projection The viewing angle the scene is flattened by. See [Projection3D] for the
 *   presets, and note that a projection with `perspective` above `0f` makes bars of equal value draw
 *   at different lengths depending on where they sit — pleasing to look at, harder to read.
 * @property barWidthFraction How much of each category slot a bar fills across, from `0f` to `1f`.
 * @property barDepthFraction How deep each bar is, as a fraction of its width. `0f` draws flat bars,
 *   which is a 2D bar chart drawn the long way round.
 * @property animation How bars grow in. See [Animation].
 * @property showValueLabels Whether each bar's value is printed above it.
 * @property valueFormatter Converts a bar's value into its printed label.
 * @property valueLabelStyle Text style for the value labels.
 * @property showCategoryLabels Whether each bar's label is printed at all.
 * @property categoryLabelPlacement Where those labels sit — see [Bar3DLabelPlacement]. Defaults to
 *   [Bar3DLabelPlacement.AUTO], which keeps them on the floor until the angle would make them
 *   collide with the bars and moves them onto the top faces when it would.
 * @property categoryLabelStyle Text style for the category labels.
 * @property showFloor Whether a shaded floor plane is drawn under the bars, which gives the eye a
 *   ground to read the depth against.
 * @property plotBackground Fill painted behind the whole scene; `null` leaves the chart transparent
 *   so whatever it sits on shows through.
 */
@ChartyExperimental
@Stable
data class Bar3DChartConfig(
    val projection: Projection3D = Projection3D.Default,
    val barWidthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    val barDepthFraction: Float = DEFAULT_BAR_DEPTH_FRACTION,
    val animation: Animation = Animation.Default,
    val showValueLabels: Boolean = false,
    val valueFormatter: (Float) -> String = { value -> value.toBar3DLabel() },
    val valueLabelStyle: TextStyle = TextStyle(fontSize = LABEL_SP.sp),
    val showCategoryLabels: Boolean = true,
    val categoryLabelPlacement: Bar3DLabelPlacement = Bar3DLabelPlacement.AUTO,
    val categoryLabelStyle: TextStyle = TextStyle(fontSize = LABEL_SP.sp),
    val showFloor: Boolean = true,
    val plotBackground: ChartyColor? = null,
) {
    init {
        require(barWidthFraction in 0f..1f) { "barWidthFraction must be between 0 and 1, got: $barWidthFraction" }
        require(barDepthFraction >= 0f) { "barDepthFraction must be non-negative, got: $barDepthFraction" }
    }
}

/**
 * Formats a value the way the chart's default label does: whole numbers without a trailing decimal,
 * and the same string on every platform, because `Float.toString` does not agree across JVM and JS.
 */
internal fun Float.toBar3DLabel(): String {
    val rounded = (this * ROUNDING_SCALE).toLong() / ROUNDING_SCALE
    val whole = rounded.toLong()
    return if (rounded == whole.toFloat()) {
        whole.toString()
    } else {
        rounded.toString()
    }
}

private const val ROUNDING_SCALE = 100f
