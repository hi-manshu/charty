package com.himanshoe.charty.line.internal.area

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.line.data.LineData

/**
 * Remembers the value range for area chart.
 */
@Composable
internal fun rememberAreaValueRange(
    dataList: List<LineData>,
    negativeValuesDrawMode: NegativeValuesDrawMode
): Pair<Float, Float> {
    return remember(dataList, negativeValuesDrawMode) {
        val values = dataList.getValues()
        val minValue = com.himanshoe.charty.common.util.calculateMinValue(values)
        val maxValue = com.himanshoe.charty.common.util.calculateMaxValue(values)
        minValue to maxValue
    }
}

