package com.himanshoe.docsite

/** Where a page sits in the site, used to build its links back out to everything else. */
data class PageContext(
    /** The page's path on the site, relative to its root. */
    val url: String,
    /** How many directories deep it sits, for building relative links back to the root. */
    val depth: Int,
)
