package com.himanshoe.sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform