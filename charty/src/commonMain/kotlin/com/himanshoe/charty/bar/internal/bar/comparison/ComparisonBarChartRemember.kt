package com.himanshoe.charty.bar.internal.bar.comparison

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.ext.getAllValues
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue

@Composable
internal fun rememberComparisonChartValues(
    dataList: List<BarGroup>,
): Pair<Float, Float> {
    return remember(dataList) {
        val allValues = dataList.getAllValues()
        val calculatedMin = calculateMinValue(allValues)
        val calculatedMax = calculateMaxValue(allValues)
        val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin
        finalMin to calculatedMax
    }
}

