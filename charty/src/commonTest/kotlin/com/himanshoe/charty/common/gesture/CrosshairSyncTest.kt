package com.himanshoe.charty.common.gesture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CrosshairSyncTest {
    @Test
    fun fractionRoundTrip_recoversOriginalFraction() {
        val plotLeft = 120f
        val plotWidth = 640f
        listOf(0f, 0.1f, 0.25f, 0.5f, 0.75f, 0.9f, 1f).forEach { fraction ->
            val pixel = crosshairXForFraction(fraction = fraction, plotLeft = plotLeft, plotWidth = plotWidth)
            val recovered = normalizedCrosshairFraction(x = pixel, plotLeft = plotLeft, plotWidth = plotWidth)
            assertEquals(expected = fraction, actual = recovered, absoluteTolerance = 1e-5f)
        }
    }

    @Test
    fun normalizedFraction_clampsPixelsOutsidePlot() {
        assertEquals(
            expected = 0f,
            actual = normalizedCrosshairFraction(x = 10f, plotLeft = 100f, plotWidth = 400f),
        )
        assertEquals(
            expected = 1f,
            actual = normalizedCrosshairFraction(x = 900f, plotLeft = 100f, plotWidth = 400f),
        )
    }

    @Test
    fun normalizedFraction_degenerateWidthReturnsZero() {
        assertEquals(
            expected = 0f,
            actual = normalizedCrosshairFraction(x = 250f, plotLeft = 100f, plotWidth = 0f),
        )
        assertEquals(
            expected = 0f,
            actual = normalizedCrosshairFraction(x = 250f, plotLeft = 100f, plotWidth = -5f),
        )
    }

    @Test
    fun crosshairXForFraction_degenerateWidthReturnsPlotLeft() {
        assertEquals(
            expected = 100f,
            actual = crosshairXForFraction(fraction = 0.5f, plotLeft = 100f, plotWidth = 0f),
        )
    }

    @Test
    fun crosshairXForFraction_clampsFractionOutsideUnitRange() {
        assertEquals(
            expected = 100f,
            actual = crosshairXForFraction(fraction = -0.5f, plotLeft = 100f, plotWidth = 400f),
        )
        assertEquals(
            expected = 500f,
            actual = crosshairXForFraction(fraction = 1.5f, plotLeft = 100f, plotWidth = 400f),
        )
    }

    @Test
    fun publish_setsOwnerAndCoercesFraction() {
        val sync = CrosshairSyncState()
        sync.publish(ownerId = "top", fraction = 1.4f)
        assertEquals(expected = "top", actual = sync.owner)
        assertEquals(expected = 1f, actual = sync.fraction)
        sync.publish(ownerId = "top", fraction = -0.2f)
        assertEquals(expected = 0f, actual = sync.fraction)
    }

    @Test
    fun fractionFor_ownerSeesNullObserverSeesValue() {
        val sync = CrosshairSyncState()
        sync.publish(ownerId = "top", fraction = 0.3f)
        assertNull(sync.fractionFor(observerId = "top"))
        assertEquals(expected = 0.3f, actual = sync.fractionFor(observerId = "bottom"))
    }

    @Test
    fun fractionFor_returnsNullWhenIdle() {
        val sync = CrosshairSyncState()
        assertNull(sync.fractionFor(observerId = "top"))
        assertNull(sync.fraction)
        assertNull(sync.owner)
    }

    @Test
    fun publish_byObserverTakesOverOwnership() {
        val sync = CrosshairSyncState()
        sync.publish(ownerId = "top", fraction = 0.3f)
        sync.publish(ownerId = "bottom", fraction = 0.8f)
        assertEquals(expected = "bottom", actual = sync.owner)
        assertEquals(expected = 0.8f, actual = sync.fractionFor(observerId = "top"))
        assertNull(sync.fractionFor(observerId = "bottom"))
    }

    @Test
    fun clear_byNonOwnerIsIgnored() {
        val sync = CrosshairSyncState()
        sync.publish(ownerId = "top", fraction = 0.5f)
        sync.clear(ownerId = "bottom")
        assertEquals(expected = "top", actual = sync.owner)
        assertEquals(expected = 0.5f, actual = sync.fraction)
    }

    @Test
    fun clear_byOwnerClearsFractionAndOwnership() {
        val sync = CrosshairSyncState()
        sync.publish(ownerId = "top", fraction = 0.5f)
        sync.clear(ownerId = "top")
        assertNull(sync.owner)
        assertNull(sync.fraction)
        assertNull(sync.fractionFor(observerId = "bottom"))
    }

    @Test
    fun clear_byDisplacedFormerOwnerIsIgnored() {
        val sync = CrosshairSyncState()
        sync.publish(ownerId = "top", fraction = 0.3f)
        sync.publish(ownerId = "bottom", fraction = 0.8f)
        sync.clear(ownerId = "top")
        assertEquals(expected = "bottom", actual = sync.owner)
        assertEquals(expected = 0.8f, actual = sync.fractionFor(observerId = "top"))
    }
}
