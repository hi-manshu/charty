package com.himanshoe.charty.bar.internal.bar.mosaic

import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.MosaicBarSegment

/**
 * Parameters for a mosaic bar segment bound calculation.
 *
 * @property rect The rectangular bounds of the segment
 * @property segment The segment data
 */
internal typealias MosaicSegmentBound = Pair<Rect, MosaicBarSegment>
