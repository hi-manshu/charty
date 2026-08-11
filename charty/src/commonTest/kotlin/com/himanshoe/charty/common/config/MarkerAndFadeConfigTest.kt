package com.himanshoe.charty.common.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MarkerAndFadeConfigTest {
    @Test
    fun persistentMarker_validValues_construct() {
        val marker = PersistentMarker(dataIndex = 2, label = "Peak")
        assertEquals(2, marker.dataIndex)
        assertEquals("Peak", marker.label)
    }

    @Test
    fun persistentMarker_negativeIndex_throws() {
        assertFailsWith<IllegalArgumentException> { PersistentMarker(dataIndex = -1) }
    }

    @Test
    fun persistentMarker_negativeRadius_throws() {
        assertFailsWith<IllegalArgumentException> { PersistentMarker(dataIndex = 0, dotRadius = -1f) }
    }

    @Test
    fun scrollEdgeFade_defaults_areValid() {
        val fade = ScrollEdgeFadeConfig()
        assertEquals(32f, fade.width)
    }

    @Test
    fun scrollEdgeFade_alphaOutOfRange_throws() {
        assertFailsWith<IllegalArgumentException> { ScrollEdgeFadeConfig(alpha = 1.5f) }
        assertFailsWith<IllegalArgumentException> { ScrollEdgeFadeConfig(alpha = -0.1f) }
    }

    @Test
    fun scrollEdgeFade_negativeWidth_throws() {
        assertFailsWith<IllegalArgumentException> { ScrollEdgeFadeConfig(width = -4f) }
    }
}
