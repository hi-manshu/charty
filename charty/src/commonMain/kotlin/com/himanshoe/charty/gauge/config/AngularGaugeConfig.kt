package com.himanshoe.charty.gauge.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.gauge.data.GaugeBand
import kotlin.math.roundToInt

private const val DEFAULT_MAX_VALUE = 100f
private const val DEFAULT_START_ANGLE_DEGREES = 135f
private const val DEFAULT_SWEEP_ANGLE_DEGREES = 270f
private const val DEFAULT_TICK_COUNT = 5
private const val DEFAULT_TRACK_WIDTH_FRACTION = 0.12f
private const val FULL_CIRCLE_DEGREES = 360f
private const val MIN_TICK_COUNT = 2
private const val TICK_LABEL_SIZE_SP = 12f
private const val VALUE_LABEL_SIZE_SP = 20f

/**
 * Configuration for the appearance and behavior of an angular gauge (speedometer dial).
 *
 * Angles follow the Compose drawing convention: `0` degrees points at 3 o'clock and positive angles
 * rotate clockwise. The defaults (`startAngleDegrees = 135f`, `sweepAngleDegrees = 270f`) produce a
 * classic speedometer dial that opens at the bottom.
 *
 * @property minValue The value mapped to the start of the dial. Must be less than [maxValue].
 * @property maxValue The value mapped to the end of the dial. Must be greater than [minValue].
 * @property startAngleDegrees The angle in degrees at which the dial starts.
 * @property sweepAngleDegrees The angular extent of the dial in degrees; must be in `(0, 360]`.
 * @property tickCount The number of major ticks (including both endpoints); must be at least 2.
 * @property tickLabelFormatter Formats a tick value into its label text.
 * @property plotBands Colored value zones painted inside the track; each band must lie within
 *   `[minValue, maxValue]`.
 * @property needleColor The color of the needle and its pivot circle.
 * @property trackColor The color of the background track arc and the tick marks.
 * @property trackWidthFraction The track stroke width as a fraction of the dial radius; must be in
 *   `(0, 1)`.
 * @property showValueLabel Whether the current value is drawn as text below the pivot.
 * @property valueFormatter Formats the current value into the value label text.
 * @property animation The animation driving the needle sweep from [minValue] to the current value.
 * @property tickLabelTextStyle The text style used for tick labels.
 * @property valueTextStyle The text style used for the value label.
 */
@Stable
@Suppress("LongParameterList")
data class AngularGaugeConfig(
    val minValue: Float = 0f,
    val maxValue: Float = DEFAULT_MAX_VALUE,
    val startAngleDegrees: Float = DEFAULT_START_ANGLE_DEGREES,
    val sweepAngleDegrees: Float = DEFAULT_SWEEP_ANGLE_DEGREES,
    val tickCount: Int = DEFAULT_TICK_COUNT,
    val tickLabelFormatter: (Float) -> String = { value -> value.roundToInt().toString() },
    val plotBands: List<GaugeBand> = emptyList(),
    val needleColor: ChartyColor = ChartyColor.Solid(Color.DarkGray),
    val trackColor: ChartyColor = ChartyColor.Solid(Color.LightGray),
    val trackWidthFraction: Float = DEFAULT_TRACK_WIDTH_FRACTION,
    val showValueLabel: Boolean = true,
    val valueFormatter: (Float) -> String = { value -> value.roundToInt().toString() },
    val animation: Animation = Animation.Default,
    val tickLabelTextStyle: TextStyle =
        TextStyle(
            fontSize = TICK_LABEL_SIZE_SP.sp,
            color = Color.Gray,
        ),
    val valueTextStyle: TextStyle =
        TextStyle(
            fontSize = VALUE_LABEL_SIZE_SP.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        ),
) {
    init {
        require(maxValue > minValue) {
            "maxValue ($maxValue) must be greater than minValue ($minValue)"
        }
        require(sweepAngleDegrees > 0f && sweepAngleDegrees <= FULL_CIRCLE_DEGREES) {
            "sweepAngleDegrees ($sweepAngleDegrees) must be in (0, $FULL_CIRCLE_DEGREES]"
        }
        require(tickCount >= MIN_TICK_COUNT) {
            "tickCount ($tickCount) must be at least $MIN_TICK_COUNT"
        }
        require(trackWidthFraction > 0f && trackWidthFraction < 1f) {
            "trackWidthFraction ($trackWidthFraction) must be in (0, 1)"
        }
        plotBands.forEach { band ->
            require(band.fromValue >= minValue && band.toValue <= maxValue) {
                "Plot band ${band.fromValue}..${band.toValue} must lie within [$minValue, $maxValue]"
            }
        }
    }
}
