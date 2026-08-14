package com.himanshoe.charty.bar.internal.bar.horizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.util.baselineValueRange

/**
 * The value range a horizontal bar chart scales against, always measured from the zero baseline.
 *
 * It used to accept a [com.himanshoe.charty.bar.config.NegativeValuesDrawMode] and key on it without
 * ever reading it, which promised a choice the chart does not offer: horizontal bars always grow from
 * zero, whichever direction they grow in. Honouring the mode would be a change to what is drawn, not
 * a tidy-up, so the parameter is gone rather than quietly ignored.
 */
@Composable
internal fun rememberHorizontalValueRange(dataList: List<BarData>): Pair<Float, Float> =
    remember(dataList) {
        baselineValueRange(dataList.getValues())
    }
