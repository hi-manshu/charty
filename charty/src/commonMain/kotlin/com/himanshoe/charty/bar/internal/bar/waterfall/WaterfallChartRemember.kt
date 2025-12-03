package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.BarData

/**
 * Remember cumulative values for waterfall chart
 */
@Composable
internal fun rememberCumulativeValues(items: List<BarData>): List<Float> {
    return remember(items) {
        calculateCumulativeValues(items)
    }
}


