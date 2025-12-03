package com.himanshoe.charty.bar.internal.bar.mosiac

import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.MosiacBarSegment

/**
 * Parameters for a mosiac bar segment bound calculation.
 *
 * @property rect The rectangular bounds of the segment
 * @property segment The segment data
 */
internal typealias MosiacSegmentBound = Pair<Rect, MosiacBarSegment>

