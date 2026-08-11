package com.himanshoe.charty.bar.internal.bar.comparison

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.ext.getAllValues
import com.himanshoe.charty.common.util.baselineValueRange

@Composable
internal fun rememberComparisonChartValues(dataList: List<BarGroup>): Pair<Float, Float> =
    remember(dataList) {
        baselineValueRange(dataList.getAllValues())
    }
