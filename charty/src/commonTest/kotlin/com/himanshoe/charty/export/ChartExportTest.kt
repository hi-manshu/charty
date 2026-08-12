package com.himanshoe.charty.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChartExportTest {
    @Test
    fun blankNameFallsBackToDefault() {
        assertEquals(expected = "chart.png", actual = sanitizeExportFileName(""))
        assertEquals(expected = "chart.png", actual = sanitizeExportFileName("   "))
        assertEquals(expected = "chart.png", actual = sanitizeExportFileName("///"))
        assertEquals(expected = "chart.png", actual = sanitizeExportFileName(".png"))
    }

    @Test
    fun extensionIsAppendedOnlyOnce() {
        assertEquals(expected = "revenue.png", actual = sanitizeExportFileName("revenue"))
        assertEquals(expected = "revenue.png", actual = sanitizeExportFileName("revenue.png"))
        assertEquals(expected = "revenue.png", actual = sanitizeExportFileName("revenue.PNG"))
    }

    @Test
    fun illegalCharactersBecomeUnderscores() {
        assertEquals(expected = "q1_q2.png", actual = sanitizeExportFileName("q1/q2"))
        assertEquals(expected = "a_b_c.png", actual = sanitizeExportFileName("a:b*c"))
        assertEquals(expected = "my_chart.png", actual = sanitizeExportFileName("  my chart  "))
    }

    @Test
    fun longNamesAreCapped() {
        val result = sanitizeExportFileName("x".repeat(500))
        assertEquals(expected = 96 + ".png".length, actual = result.length)
        assertTrue(actual = result.endsWith(".png"))
    }

    @Test
    fun base64MatchesKnownVectors() {
        assertEquals(expected = "", actual = ByteArray(0).encodeChartBase64())
        assertEquals(expected = "TWFu", actual = "Man".encodeToByteArray().encodeChartBase64())
        assertEquals(expected = "TWE=", actual = "Ma".encodeToByteArray().encodeChartBase64())
        assertEquals(expected = "TQ==", actual = "M".encodeToByteArray().encodeChartBase64())
        assertEquals(expected = "aGVsbG8gd29ybGQ=", actual = "hello world".encodeToByteArray().encodeChartBase64())
    }

    @Test
    fun base64HandlesHighBytes() {
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        assertEquals(expected = "iVBORw==", actual = bytes.encodeChartBase64())
    }
}
