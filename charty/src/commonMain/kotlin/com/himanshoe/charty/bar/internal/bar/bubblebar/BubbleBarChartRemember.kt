package com.himanshoe.charty.bar.internal.bar.bubblebar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.util.baselineValueRange

/**
 * Remember functions for BubbleBarChart
 */

@Composable
internal fun rememberValueRange(
    dataList: List<BarData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> =
    remember(dataList, negativeValuesDrawMode) {
        baselineValueRange(dataList.getValues())
    }
