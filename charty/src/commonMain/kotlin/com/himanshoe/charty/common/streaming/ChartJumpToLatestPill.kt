package com.himanshoe.charty.common.streaming

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.theme.currentChartyTheme
import com.himanshoe.charty.common.tooltip.toTooltipBrush
import kotlinx.coroutines.launch

private const val PILL_CORNER_RADIUS_DP = 999

/**
 * The built-in "jump to latest" control for a streaming chart: a rounded pill labelled with how many
 * points arrived while the reader was looking back, which animates the window to the newest point and
 * resumes following when tapped.
 *
 * Pass it to a chart through [com.himanshoe.charty.common.config.ChartInteractionConfig.jumpToLatest];
 * the chart shows it only while the window is detached, so it needs no visibility logic of its own.
 *
 * ```kotlin
 * val streaming = rememberStreamingState()
 * LineChart(
 *     data = { points },
 *     lineConfig = LineChartConfig(visibleWindow = 30),
 *     interactionConfig =
 *         ChartInteractionConfig(
 *             streamingState = streaming,
 *             jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
 *         ),
 * )
 * ```
 *
 * It is built purely on `compose.foundation`, so it carries no Material dependency and inherits none of
 * a host theme's typography — style it through [backgroundColor] and [textStyle].
 *
 * @param state The chart's streaming state; supplies [StreamingState.pendingCount] and performs the jump.
 * @param modifier Modifier applied to the pill.
 * @param text Builds the label from the pending-arrival count. Defaults to `"12 new"`.
 * @param backgroundColor Pill fill as a [ChartyColor] (solid or gradient); defaults to the ambient
 *   [com.himanshoe.charty.common.theme.ChartyTheme] primary colour.
 * @param textStyle Text style for the label.
 * @param contentPadding Padding between the label and the pill edges.
 * @param animation Drives the scroll back to the newest point; [Animation.Disabled] jumps instantly.
 */
@Composable
fun ChartJumpToLatestPill(
    state: StreamingState,
    modifier: Modifier = Modifier,
    text: (Int) -> String = { "$it new" },
    backgroundColor: ChartyColor = ChartyColor.Solid(currentChartyTheme.primaryColor),
    textStyle: TextStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    animation: Animation = Animation.Default,
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(PILL_CORNER_RADIUS_DP.dp))
                .background(brush = backgroundColor.toTooltipBrush())
                .clickable { scope.launch { state.jumpToLatest(animation) } }
                .padding(contentPadding),
    ) {
        BasicText(text = text(state.pendingCount), style = textStyle)
    }
}
