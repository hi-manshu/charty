package com.himanshoe.charty.common.annotation

/**
 * Marks an API that is still finding its shape, and that may change or be withdrawn in a minor
 * release without the deprecation cycle the rest of Charty follows.
 *
 * Opting in is deliberate rather than incidental: the compiler refuses the call until you say, at
 * the call site with `@OptIn(ChartyExperimental::class)` or once for a module with the
 * `-opt-in` compiler argument, that you accept the API may move under you. That is the point — an
 * experimental API you can use without noticing tells you nothing.
 *
 * Everything in the `charty-3d` artifact carries this. Projected three-dimensional charts are new,
 * their configuration surface is still being learned from use, and depth is a presentation choice
 * whose conventions are less settled than the flat charts'.
 *
 * ```kotlin
 * @OptIn(ChartyExperimental::class)
 * @Composable
 * fun Dashboard() {
 *     Bar3DChart(data = { bars })
 * }
 * ```
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message =
        "This Charty API is experimental: it may change or be removed in a minor release without " +
            "deprecation. Opt in with @OptIn(ChartyExperimental::class) to accept that.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.TYPEALIAS,
)
annotation class ChartyExperimental
