package com.himanshoe.charty.common.downsample

import kotlin.math.abs
import kotlin.math.floor

private const val MIN_LTTB_THRESHOLD = 3
private const val TRIANGLE_AREA_FACTOR = 0.5

/**
 * Downsamples [data] to at most [threshold] points using the Largest-Triangle-Three-Buckets (LTTB)
 * algorithm, preserving the visual shape of a line far better than naive stride sampling.
 *
 * LTTB keeps the first and last points, splits the remaining points into `threshold - 2` equal
 * buckets, and picks from each bucket the point that forms the largest triangle with the previously
 * selected point and the average of the next bucket. Visually significant peaks and troughs survive;
 * flat runs collapse. This is the standard technique for plotting tens of thousands of points at
 * interactive frame rates.
 *
 * The point's x-coordinate is its index in [data] (charts here are index-positioned), and its
 * y-coordinate is [value]. The returned list is a subset of [data] in the original order.
 *
 * Returns [data] unchanged when it already has [threshold] or fewer points, or when [threshold] is
 * less than 3 (LTTB needs at least a first bucket, a middle bucket, and a last point).
 *
 * ```kotlin
 * val reduced = lttbDownsample(points, threshold = 500) { it.value }
 * ```
 *
 * @param data The full series, in x order.
 * @param threshold The maximum number of points to keep. Values `< 3` disable downsampling.
 * @param value Extracts the y-value of a point.
 * @return A list of at most [threshold] points that approximates [data]'s shape.
 */
fun <T> lttbDownsample(
    data: List<T>,
    threshold: Int,
    value: (T) -> Float,
): List<T> {
    val size = data.size
    if (threshold >= size || threshold < MIN_LTTB_THRESHOLD) {
        return data
    }

    val sampled = ArrayList<T>(threshold)
    val bucketSize = (size - 2).toDouble() / (threshold - 2)

    var selectedIndex = 0
    sampled.add(data[0])

    for (bucket in 0 until threshold - 2) {
        val nextBucketStart = (floor((bucket + 1) * bucketSize) + 1).toInt()
        val nextBucketEnd = minOf((floor((bucket + 2) * bucketSize) + 1).toInt(), size)
        val nextBucketLength = nextBucketEnd - nextBucketStart

        var avgX = 0.0
        var avgY = 0.0
        for (j in nextBucketStart until nextBucketEnd) {
            avgX += j
            avgY += value(data[j])
        }
        avgX /= nextBucketLength
        avgY /= nextBucketLength

        val bucketStart = (floor(bucket * bucketSize) + 1).toInt()
        val bucketEnd = (floor((bucket + 1) * bucketSize) + 1).toInt()

        val anchorX = selectedIndex.toDouble()
        val anchorY = value(data[selectedIndex]).toDouble()

        var maxArea = -1.0
        var nextSelected = bucketStart
        for (j in bucketStart until bucketEnd) {
            val area =
                abs(
                    (anchorX - avgX) * (value(data[j]) - anchorY) -
                        (anchorX - j) * (avgY - anchorY),
                ) * TRIANGLE_AREA_FACTOR
            if (area > maxArea) {
                maxArea = area
                nextSelected = j
            }
        }
        sampled.add(data[nextSelected])
        selectedIndex = nextSelected
    }

    sampled.add(data[size - 1])
    return sampled
}
