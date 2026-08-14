package com.himanshoe.docsite

/** Where a page sits in the site, used to build its links back out to everything else. */
data class PageContext(
    val url: String,
    val depth: Int,
)
