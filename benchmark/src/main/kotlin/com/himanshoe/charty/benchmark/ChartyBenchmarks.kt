package com.himanshoe.charty.benchmark

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.downsample.lttbDownsample
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import kotlin.math.sin
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

private const val SERIES_SIZE = 50_000
private const val DOWNSAMPLE_TARGET = 800
private const val TRANSFORM_COUNT = 10_000

/**
 * JVM microbenchmarks for Charty's per-data / per-frame hot paths (all pure, public commonMain code).
 * Run with `./gradlew :benchmark:mainBenchmark`; numbers are average time per invocation.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
open class ChartyBenchmarks {
    private lateinit var values: List<Float>
    private lateinit var context: ChartContext

    @Setup
    open fun setup() {
        values =
            List(SERIES_SIZE) { i ->
                val noise = if (i % 500 == 0) 40f else 0f
                50f + 40f * sin(i / 250.0).toFloat() + noise
            }
        context =
            ChartContext(
                left = 0f,
                top = 0f,
                right = 1000f,
                bottom = 500f,
                minValue = 0f,
                maxValue = 100f,
            )
    }

    /** LTTB reduction of a 50k-point series to 800 points — the large-dataset render path. */
    @Benchmark
    open fun lttbDownsample50kTo800(blackhole: Blackhole) {
        blackhole.consume(lttbDownsample(data = values, threshold = DOWNSAMPLE_TARGET) { it })
    }

    /** Axis min/max "nice" rounding over a 50k-point series — runs whenever data changes. */
    @Benchmark
    open fun valueRange50k(blackhole: Blackhole) {
        blackhole.consume(calculateMinValue(values))
        blackhole.consume(calculateMaxValue(values))
    }

    /** 10k value→pixel coordinate transforms — the per-point, per-frame draw cost. */
    @Benchmark
    open fun coordinateTransforms10k(blackhole: Blackhole) {
        var i = 0
        while (i < TRANSFORM_COUNT) {
            blackhole.consume(context.convertValueToYPosition(values[i]))
            blackhole.consume(context.calculateCenteredXPosition(index = i, totalItems = TRANSFORM_COUNT))
            i++
        }
    }
}
