package com.himanshoe.charty.combo

import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.combo.internal.comboLineRange
import com.himanshoe.charty.combo.internal.comboPrimaryRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val TOL = 0.001f

class ComboRangeTest {
    private val data =
        listOf(
            ComboChartData(label = "A", barValue = 100f, lineValue = 80f),
            ComboChartData(label = "B", barValue = 150f, lineValue = 140f),
        )

    @Test
    fun lineRange_null_whenSingleAxis() {
        assertNull(comboLineRange(dataList = data, secondaryAxisForLine = false))
    }

    @Test
    fun lineRange_fromLineValues_whenDualAxis() {
        val (lo, hi) = comboLineRange(dataList = data, secondaryAxisForLine = true)!!
        assertEquals(80f, lo, TOL)
        assertEquals(140f, hi, TOL)
    }

    @Test
    fun primaryRange_dualAxis_usesBarValuesOnly() {
        val (lo, hi) =
            comboPrimaryRange(
                dataList = data,
                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                secondaryAxisForLine = true,
            )
        assertEquals(100f, lo, TOL)
        assertEquals(150f, hi, TOL)
    }

    @Test
    fun primaryRange_singleAxis_usesAllValues() {
        val (lo, hi) =
            comboPrimaryRange(
                dataList = data,
                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                secondaryAxisForLine = false,
            )
        assertEquals(80f, lo, TOL) // min across bar + line
        assertEquals(150f, hi, TOL)
    }

    @Test
    fun primaryRange_belowAxis_clampsMinToZero() {
        val (lo, hi) =
            comboPrimaryRange(
                dataList = listOf(ComboChartData(label = "A", barValue = 100f, lineValue = 5f)),
                negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                secondaryAxisForLine = true,
            )
        assertEquals(0f, lo, TOL)
        assertEquals(100f, hi, TOL)
    }
}
