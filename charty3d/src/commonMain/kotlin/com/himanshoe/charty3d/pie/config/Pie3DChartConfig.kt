package com.himanshoe.charty3d.pie.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.projection.Projection3D

private const val DEFAULT_THICKNESS_FRACTION = 0.18f
private const val DEFAULT_ARC_SEGMENTS = 72
private const val MIN_ARC_SEGMENTS = 12
private const val LABEL_SP = 11f

/**
 * What a slice's label reads.
 */
@ChartyExperimental
enum class Pie3DLabelContent {
    /** Nothing; the legend or a tap does the naming. */
    NONE,

    /** The slice's own label. */
    NAME,

    /** The slice's share of the whole, as a percentage. */
    PERCENTAGE,

    /** Both, on one line. */
    NAME_AND_PERCENTAGE,
}

/**
 * How a [com.himanshoe.charty3d.pie.Pie3DChart] draws its slices.
 *
 * @property projection The viewing angle the disc is flattened by. A pitch of `0f` would show the
 *   disc edge-on and hide every slice, so the chart raises it to a shallow minimum; the presets are
 *   all well clear of that.
 * @property thicknessFraction How deep the disc is, as a fraction of its radius. `0f` draws a flat
 *   pie, which is the ordinary 2D chart seen at an angle.
 * @property holeFraction Inner radius as a fraction of the outer, turning the pie into a ring. `0f`
 *   is a solid pie.
 * @property arcSegments How many straight edges approximate a full circle. Higher is smoother and
 *   costs more geometry; the default is smooth at any size a chart is drawn at.
 * @property explodeFraction How far every slice is pushed out from the centre, as a fraction of the
 *   radius. `0f` keeps the disc whole.
 * @property animation How slices sweep in. See [Animation].
 * @property labelContent What each slice's label reads. See [Pie3DLabelContent].
 * @property labelStyle Text style for the slice labels.
 * @property minimumSharePercentageForLabel Slices thinner than this share of the whole go unlabelled,
 *   because their label cannot fit inside them and would overlap their neighbours'.
 * @property plotBackground Fill painted behind the whole scene; `null` leaves the chart transparent.
 */
@ChartyExperimental
@Stable
data class Pie3DChartConfig(
    val projection: Projection3D = Projection3D.Default,
    val thicknessFraction: Float = DEFAULT_THICKNESS_FRACTION,
    val holeFraction: Float = 0f,
    val arcSegments: Int = DEFAULT_ARC_SEGMENTS,
    val explodeFraction: Float = 0f,
    val animation: Animation = Animation.Default,
    val labelContent: Pie3DLabelContent = Pie3DLabelContent.NONE,
    val labelStyle: TextStyle = TextStyle(fontSize = LABEL_SP.sp),
    val minimumSharePercentageForLabel: Float = 5f,
    val plotBackground: ChartyColor? = null,
) {
    init {
        require(thicknessFraction >= 0f) { "thicknessFraction must be non-negative, got: $thicknessFraction" }
        require(holeFraction in 0f..1f) { "holeFraction must be between 0 and 1, got: $holeFraction" }
        require(arcSegments >= MIN_ARC_SEGMENTS) { "arcSegments must be at least $MIN_ARC_SEGMENTS, got: $arcSegments" }
        require(explodeFraction >= 0f) { "explodeFraction must be non-negative, got: $explodeFraction" }
        require(minimumSharePercentageForLabel >= 0f) {
            "minimumSharePercentageForLabel must be non-negative, got: $minimumSharePercentageForLabel"
        }
    }
}
