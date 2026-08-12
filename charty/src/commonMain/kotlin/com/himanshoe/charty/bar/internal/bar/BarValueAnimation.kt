package com.himanshoe.charty.bar.internal.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.common.animation.rememberAnimatedValues
import com.himanshoe.charty.common.config.Animation

/**
 * Returns [dataList] with each bar's value tweened toward its target whenever the data changes, so
 * bars glide to their new heights instead of snapping. When [enabled] is `false` or [animation] is
 * disabled the list is returned unchanged.
 *
 * @param dataList The bars to display.
 * @param animation The animation configuration driving the transition.
 * @param enabled Opt-in switch mirroring the chart config's `animateValueChanges`.
 * @return The bars to draw this frame.
 */
@Composable
internal fun rememberAnimatedBarValues(
    dataList: List<BarData>,
    animation: Animation,
    enabled: Boolean,
): List<BarData> {
    val animatedValues =
        rememberAnimatedValues(
            targetValues = dataList.fastMap { it.value },
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        dataList.fastMapIndexed { index, bar -> bar.copy(value = animatedValues[index]) }
    }
}

/**
 * Returns [dataList] with every series value of every group tweened toward its target whenever the
 * data changes. All series animate together off a single progress, so a group's segments stay
 * consistent with each other for the whole transition. When [enabled] is `false` or [animation] is
 * disabled the list is returned unchanged.
 *
 * @param dataList The groups to display.
 * @param animation The animation configuration driving the transition.
 * @param enabled Opt-in switch mirroring the chart config's `animateValueChanges`.
 * @return The groups to draw this frame.
 */
@Composable
internal fun rememberAnimatedBarGroups(
    dataList: List<BarGroup>,
    animation: Animation,
    enabled: Boolean,
): List<BarGroup> {
    val flattened = remember(dataList) { dataList.flatMap { it.values } }
    val animatedValues =
        rememberAnimatedValues(
            targetValues = flattened,
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        if (animatedValues.size != flattened.size) {
            dataList
        } else {
            var offset = 0
            dataList.fastMap { group ->
                val slice = animatedValues.subList(offset, offset + group.values.size).toList()
                offset += group.values.size
                group.copy(values = slice)
            }
        }
    }
}

/**
 * Returns [dataList] with each span's start and end tweened toward their targets whenever the data
 * changes. Both ends share one progress so a span never stretches inconsistently mid-flight. When
 * [enabled] is `false` or [animation] is disabled the list is returned unchanged.
 *
 * @param dataList The spans to display.
 * @param animation The animation configuration driving the transition.
 * @param enabled Opt-in switch mirroring the chart config's `animateValueChanges`.
 * @return The spans to draw this frame.
 */
@Composable
internal fun rememberAnimatedSpanValues(
    dataList: List<SpanData>,
    animation: Animation,
    enabled: Boolean,
): List<SpanData> {
    val flattened = remember(dataList) { dataList.flatMap { listOf(it.startValue, it.endValue) } }
    val animatedValues =
        rememberAnimatedValues(
            targetValues = flattened,
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        if (animatedValues.size != flattened.size) {
            dataList
        } else {
            dataList.fastMapIndexed { index, span ->
                span.copy(
                    startValue = animatedValues[index * 2],
                    endValue = animatedValues[index * 2 + 1],
                )
            }
        }
    }
}
