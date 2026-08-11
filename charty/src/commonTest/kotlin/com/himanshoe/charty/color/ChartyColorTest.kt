package com.himanshoe.charty.color

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChartyColorTest {
    @Test
    fun solid_valueIsTwoIdenticalColors() {
        val value = ChartyColor.Solid(Color.Red).value
        assertEquals(listOf(Color.Red, Color.Red), value)
    }

    @Test
    fun gradient_valueIsTheColorList() {
        val colors = listOf(Color.Red, Color.Blue)
        assertEquals(colors, ChartyColor.Gradient(colors).value)
    }

    @Test
    fun gradient_rejectsEmptyColorList() {
        // regression: an empty gradient previously crashed at ~47 draw sites via .first()/Brush
        assertFailsWith<IllegalArgumentException> { ChartyColor.Gradient(emptyList()) }
    }
}
