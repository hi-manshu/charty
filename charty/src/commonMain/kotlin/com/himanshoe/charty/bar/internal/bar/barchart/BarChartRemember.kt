package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.util.baselineValueRange

/**
 * Helper function to remember the value range (min, max) for the Y axis
 */
@Composable
internal fun rememberBarValueRange(
    dataList: List<BarData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> =
    remember(dataList, negativeValuesDrawMode) {
        baselineValueRange(dataList.getValues())
    }
