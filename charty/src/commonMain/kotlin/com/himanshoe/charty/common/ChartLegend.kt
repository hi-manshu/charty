package com.himanshoe.charty.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.withChartyColor

private val LEGEND_DOT_SIZE = 10.dp
private val LEGEND_ITEM_SPACING = 16.dp
private val LEGEND_DOT_TEXT_GAP = 4.dp
private val LEGEND_VERTICAL_PADDING = 4.dp

/**
 * A horizontal legend row showing a colored dot + label for each series.
 *
 * @param labels Series labels to display.
 * @param colors Colors matching each label by index (wrapped with modulo if shorter than labels).
 *   A [ChartyColor.Gradient] paints its swatch as a gradient; the label inherits the same fill when
 *   [textStyle] carries no color of its own.
 * @param modifier Modifier applied to the wrapping Row.
 * @param textStyle Text style for the labels; defaults to unspecified (inherits theme).
 * @param dotSize Diameter of the color swatch shown before each label.
 * @param itemSpacing Gap between one legend entry and the next.
 * @param dotTextGap Gap between a swatch and its label.
 * @param contentPadding Vertical padding around the legend row.
 */
@Composable
fun ChartLegend(
    labels: List<String>,
    colors: List<ChartyColor>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    dotSize: Dp = LEGEND_DOT_SIZE,
    itemSpacing: Dp = LEGEND_ITEM_SPACING,
    dotTextGap: Dp = LEGEND_DOT_TEXT_GAP,
    contentPadding: Dp = LEGEND_VERTICAL_PADDING,
) {
    if (labels.isEmpty() || colors.isEmpty()) {
        return
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.fastForEachIndexed { index, label ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(itemSpacing))
            }
            val dotColor = colors[index % colors.size]
            Spacer(
                modifier =
                    Modifier
                        .size(dotSize)
                        .background(brush = Brush.linearGradient(dotColor.value), shape = CircleShape),
            )
            Spacer(modifier = Modifier.width(dotTextGap))
            Text(
                text = label,
                style =
                    if (textStyle.color == Color.Unspecified) {
                        textStyle.withChartyColor(dotColor)
                    } else {
                        textStyle
                    },
            )
        }
    }
}
