package com.himanshoe.charty.common.config

/**
 * Sealed class for corner radius sizes with value parameter
 */
sealed class CornerRadius(val value: Float) {

    data object None : CornerRadius(0f)
    data object Small : CornerRadius(4f)
    data object Medium : CornerRadius(8f)
    data object Large : CornerRadius(12f)
    data object ExtraLarge : CornerRadius(16f)
    data class Custom(private val radius: Float) : CornerRadius(radius) {
        init {
            require(radius >= 0f) { "Corner radius must be non-negative" }
        }
    }
}

