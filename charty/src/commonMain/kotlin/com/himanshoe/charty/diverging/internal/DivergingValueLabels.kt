package com.himanshoe.charty.diverging.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.diverging.config.DivergingBarChartConfig
import com.himanshoe.charty.diverging.data.DivergingData

/**
 * The value labels of a diverging chart, measured once per data change rather than once per frame,
 * so the draw loop only positions text it already knows the size of.
 *
 * @property left One measured label per row for the left half, in row order.
 * @property right One measured label per row for the right half, in row order.
 */
internal class DivergingValueLabels(
    val left: List<TextLayoutResult>,
    val right: List<TextLayoutResult>,
)

/**
 * Measures the per-bar value labels, or returns `null` when the chart does not show them.
 *
 * @param dataList The rows currently drawn.
 * @param config Supplies the label formatter, text style, and whether labels are shown at all.
 * @param textMeasurer The measurer used to lay the labels out.
 * @return The measured labels, or `null` when [DivergingBarChartConfig.showValueLabels] is off.
 */
@Composable
internal fun rememberDivergingValueLabels(
    dataList: List<DivergingData>,
    config: DivergingBarChartConfig,
    textMeasurer: TextMeasurer,
): DivergingValueLabels? =
    remember(dataList, config, textMeasurer) {
        if (!config.showValueLabels) {
            null
        } else {
            DivergingValueLabels(
                left =
                    dataList.fastMap { row ->
                        textMeasurer.measure(
                            text = config.valueFormatter(row.leftValue),
                            style = config.valueLabelStyle,
                        )
                    },
                right =
                    dataList.fastMap { row ->
                        textMeasurer.measure(
                            text = config.valueFormatter(row.rightValue),
                            style = config.valueLabelStyle,
                        )
                    },
            )
        }
    }
