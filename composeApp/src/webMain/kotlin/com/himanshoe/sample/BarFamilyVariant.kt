package com.himanshoe.sample

/** The bar-family charts that share this one screen, each with its own shape of data. */
internal enum class BarFamilyVariant(
    val label: String,
    val blurb: String,
) {
    Waterfall(
        label = "Waterfall",
        blurb = "Running total built from signed steps. Each bar floats between the totals either side of it.",
    ),
    Lollipop(
        label = "Lollipop",
        blurb = "A bar reduced to a stem and a head — less ink for the same reading.",
    ),
    BubbleBar(
        label = "Bubble bar",
        blurb = "A bar drawn as a stack of bubbles; the count carries the value.",
    ),
    Span(
        label = "Span",
        blurb = "One start-to-end range per row: confidence intervals, highs and lows, durations.",
    ),
    Comparison(
        label = "Comparison",
        blurb = "Two or more bars per category, side by side against one baseline.",
    ),
    Mosaic(
        label = "Mosaic",
        blurb = "Column width carries the group total while its segments carry the split.",
    ),
    StackedHorizontal(
        label = "Stacked horizontal",
        blurb = "Segments accumulating rightwards, one row per group.",
    ),
    NormalizedHorizontal(
        label = "Normalized horizontal",
        blurb = "The same rows stretched to equal length, so the split is comparable across groups.",
    ),
}
