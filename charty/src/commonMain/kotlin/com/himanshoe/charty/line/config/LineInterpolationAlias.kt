package com.himanshoe.charty.line.config

/**
 * Moved to `com.himanshoe.charty.common.config` — the combo chart draws a line as well, and had to
 * reach into the line package to say how.
 */
@Deprecated(
    message = "Moved to com.himanshoe.charty.common.config.LineInterpolation",
    replaceWith = ReplaceWith("LineInterpolation", "com.himanshoe.charty.common.config.LineInterpolation"),
)
typealias LineInterpolation = com.himanshoe.charty.common.config.LineInterpolation
