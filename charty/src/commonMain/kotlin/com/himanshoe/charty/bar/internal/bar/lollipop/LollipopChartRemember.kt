package com.himanshoe.charty.bar.internal.bar.lollipop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.util.calculateMaxValue

/**
 * Remembers the value range (min, max) for the lollipop chart.
 */
@Composable
internal fun rememberLollipopValueRange(dataList: List<BarData>): Pair<Float, Float> =
    remember(dataList) {
        val values = dataList.getValues()
        0f to calculateMaxValue(values)
    }
