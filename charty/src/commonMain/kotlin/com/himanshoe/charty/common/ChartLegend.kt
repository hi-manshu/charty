package com.himanshoe.charty.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed

private val LEGEND_DOT_SIZE = 10.dp
private val LEGEND_ITEM_SPACING = 16.dp
private val LEGEND_DOT_TEXT_GAP = 4.dp
private val LEGEND_VERTICAL_PADDING = 4.dp

/**
 * A horizontal legend row showing a colored dot + label for each series.
 *
 * @param labels Series labels to display.
 * @param colors Colors matching each label by index (wrapped with modulo if shorter than labels).
 * @param modifier Modifier applied to the wrapping Row.
 * @param textStyle Text style for the labels; defaults to unspecified (inherits theme).
 */
@Composable
fun ChartLegend(
    labels: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
) {
    if (labels.isEmpty() || colors.isEmpty()) return

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = LEGEND_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.fastForEachIndexed { index, label ->
            if (index > 0) Spacer(modifier = Modifier.width(LEGEND_ITEM_SPACING))
            val dotColor = colors[index % colors.size]
            Surface(
                modifier = Modifier.size(LEGEND_DOT_SIZE),
                shape = CircleShape,
                color = dotColor,
            ) {}
            Spacer(modifier = Modifier.width(LEGEND_DOT_TEXT_GAP))
            Text(
                text = label,
                style =
                    if (textStyle.color == Color.Unspecified) {
                        textStyle.copy(color = dotColor)
                    } else {
                        textStyle
                    },
            )
        }
    }
}
