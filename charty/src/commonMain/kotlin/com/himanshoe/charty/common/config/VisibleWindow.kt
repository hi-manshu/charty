package com.himanshoe.charty.common.config

/** The smallest meaningful rolling window (a line needs at least two points). */
internal const val MIN_VISIBLE_WINDOW = 2

/**
 * Validates a chart config's `visibleWindow` (rolling "show last N" window): it must be `null`
 * (disabled) or at least [MIN_VISIBLE_WINDOW]. Call from a config's `init` block.
 */
internal fun requireValidVisibleWindow(visibleWindow: Int?) {
    require(visibleWindow == null || visibleWindow >= MIN_VISIBLE_WINDOW) {
        "visibleWindow must be null or >= $MIN_VISIBLE_WINDOW"
    }
}
