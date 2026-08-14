package com.himanshoe.charty.bar.config

/**
 * Moved to `com.himanshoe.charty.common.config` — line, area, point and combo charts all take this
 * setting, so it was never a bar-specific choice, and reading it from `bar.config` in a line chart
 * looked like a mistake at every call site.
 */
@Deprecated(
    message = "Moved to com.himanshoe.charty.common.config.NegativeValuesDrawMode",
    replaceWith = ReplaceWith("NegativeValuesDrawMode", "com.himanshoe.charty.common.config.NegativeValuesDrawMode"),
)
typealias NegativeValuesDrawMode = com.himanshoe.charty.common.config.NegativeValuesDrawMode
