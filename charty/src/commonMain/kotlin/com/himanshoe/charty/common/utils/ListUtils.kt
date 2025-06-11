package com.himanshoe.charty.common.utils

/**
 * Pads a list with default items at the beginning if its size is less than a minimum count.
 *
 * @param T The type of items in the list.
 * @param originalList The original list.
 * @param minimumCount The minimum desired count of items in the list.
 * @param defaultItemFactory A lambda function that creates a default item.
 * @return A new list padded to the minimum count, or the original list if no padding was needed.
 */
internal fun <T> padListToMinimumCount(
    originalList: List<T>,
    minimumCount: Int,
    defaultItemFactory: () -> T
): List<T> {
    return if (originalList.size < minimumCount) {
        val paddingCount = minimumCount - originalList.size
        val paddingItems = List(paddingCount) { defaultItemFactory() }
        paddingItems + originalList
    } else {
        originalList
    }
}
