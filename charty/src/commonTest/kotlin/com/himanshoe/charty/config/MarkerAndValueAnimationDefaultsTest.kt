package com.himanshoe.charty.config

import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.point.config.PointChartConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkerAndValueAnimationDefaultsTest {
    @Test
    fun everyCartesianConfig_drawsNoMarkersByDefault() {
        val markerLists =
            listOf(
                BarChartConfig().markers,
                BubbleBarChartConfig().markers,
                ComparisonBarChartConfig().markers,
                GroupedHorizontalBarChartConfig().markers,
                LollipopBarChartConfig().markers,
                MosaicBarChartConfig().markers,
                NormalizedHorizontalBarChartConfig().markers,
                StackedBarChartConfig().markers,
                StackedHorizontalBarChartConfig().markers,
                WaterfallChartConfig().markers,
                WavyChartConfig().markers,
                CandlestickChartConfig().markers,
                ComboChartConfig().markers,
                PointChartConfig().markers,
            )
        assertTrue(markerLists.all { it.isEmpty() })
    }

    @Test
    fun everyCartesianConfig_leavesValueChangeTweeningOffByDefault() {
        val flags =
            listOf(
                BarChartConfig().animateValueChanges,
                BubbleBarChartConfig().animateValueChanges,
                ComparisonBarChartConfig().animateValueChanges,
                GroupedHorizontalBarChartConfig().animateValueChanges,
                LollipopBarChartConfig().animateValueChanges,
                MosaicBarChartConfig().animateValueChanges,
                NormalizedHorizontalBarChartConfig().animateValueChanges,
                StackedBarChartConfig().animateValueChanges,
                StackedHorizontalBarChartConfig().animateValueChanges,
                WaterfallChartConfig().animateValueChanges,
                WavyChartConfig().animateValueChanges,
                CandlestickChartConfig().animateValueChanges,
                ComboChartConfig().animateValueChanges,
                PointChartConfig().animateValueChanges,
            )
        assertFalse(flags.any { it })
    }

    @Test
    fun markersCanBePinnedToTheLatestValue() {
        val config = BarChartConfig(markers = listOf(PersistentMarker(dataIndex = -1)))
        assertEquals(-1, config.markers.single().dataIndex)
    }

    @Test
    fun valueChangeTweeningIsOptIn() {
        assertTrue(CandlestickChartConfig(animateValueChanges = true).animateValueChanges)
    }
}
