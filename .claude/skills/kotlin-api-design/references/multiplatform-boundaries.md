# Kotlin Multiplatform: expect/actual boundaries

## Core principle

Keep common APIs semantic and stable. Put platform mechanics behind small `expect`/`actual` declarations or interfaces, and keep Android/iOS/Desktop details out of `commonMain`.

## Boundary procedure

1. Name the product capability in common terms: share text, read clipboard, request haptic feedback, resolve current region.
2. Check whether common callers need fakes, injected dependencies, lifecycle ownership, or runtime implementation choice.
3. Pick the smallest boundary from the table below.
4. Keep the common signature free of platform types and platform vocabulary.
5. Put business branching in common code; keep actuals/platform bindings as translation layers.
6. Validate by compiling every affected source set and testing common code with a fake where possible.

## Choose the boundary

| Situation | Prefer |
|---|---|
| Simple compile-time platform specialization | `expect`/`actual` function, value, typealias, or leaf composable |
| Implementation needs injected dependencies, lifecycle ownership, runtime choice, or test fakes | Common interface plus platform binding |
| UI is mostly shared, one leaf differs | Common composable calling an `expect` leaf |
| Entire screen differs by platform | Separate platform screens behind a common navigation contract |
| Only constants/resources differ | Common API exposing semantic values, actual values per platform |

## Keep common APIs semantic

Write common APIs so callers describe intent, not platform mechanics:

```kotlin
// GOOD: common API is semantic
expect fun currentRegion(): Region
```

```kotlin
// BAD: common API leaks Android implementation
expect fun currentRegionFromAndroidLocale(context: Context): Region
```

The Android actual can use `Locale` APIs. The iOS actual can use Foundation APIs. Common callers should not know.

## Keep actuals thin

Actual implementations should translate the semantic API into platform calls. If the operation needs an Activity, view controller, lifecycle owner, DI, or fakes, stop and use an interface supplied by platform code instead of an `expect class`:

```kotlin
// commonMain
interface ShareSheet {
    suspend fun shareText(text: String)
}
```

```kotlin
// androidMain
class AndroidShareSheet(
    private val activity: Activity,
) : ShareSheet {
    override suspend fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
        activity.startActivity(Intent.createChooser(intent, null))
    }
}
```

The Android implementation is explicitly Activity-owned. A generic `Context` often hides the UI lifecycle requirement. Define what `suspend` means: for many platform UI actions it means "the sheet was launched", not "the user completed sharing."

If the actual starts accumulating business rules, move those rules back to common code and leave only platform translation in the actual.

## Prefer interfaces when tests or DI matter

Use `expect/actual` for simple compile-time platform APIs. Use interfaces when common code needs fakes, multiple implementations, runtime selection, or lifecycle ownership:

```kotlin
interface Clipboard {
    suspend fun setText(text: String)
}
```

Platform modules bind `Clipboard` to Android/iOS implementations. Common tests use a fake.

## Compose-specific guidance

When shared UI reaches a platform leaf:

1. Keep platform-specific composables at leaf nodes.
2. Pass `Modifier` through every expected composable that emits UI.
3. Reject platform types in `commonMain` signatures (`Context`, `Activity`, Android resource IDs, `Uri`, `Bundle`, `UIViewController`, `NSBundle`, platform permission enums, etc.).
4. Hide native view lifecycle inside the platform actual and use the right interop container (`AndroidView`, `UIKitView`, etc.).
5. Do not launch platform work directly from a composable body. Use `remember`, `LaunchedEffect`, `DisposableEffect`, and stable keys inside actual composables just as you would in common Compose code.
6. Preview/test the common plain UI composable with fake platform services where possible.

## Common mistakes

| Mistake | Fix |
|---|---|
| `commonMain` API exposes Android/iOS types | Replace with semantic common types |
| `expect` function has parameters for one platform only | Move those details into the actual |
| Business branching duplicated in each actual | Move business rules to common code |
| One huge `Platform` expect object | Split by capability: `Clipboard`, `ShareSheet`, `Haptics` |
| Platform UI leaks high in the tree | Push platform-specific Composable to a leaf |
| No fakeable boundary for common tests | Use an interface instead of direct `expect` call |
| Only one target compiles after the change | Compile all affected source sets before finishing |

## Red flags during review

- Common code imports platform packages.
- An actual implementation knows product state, navigation decisions, or domain rules.
- A platform API name appears in a common function name.
- Adding a third platform would require changing common callers.
- Tests need Android/iOS runtime just to verify common business behavior.

## Related (Compose / shared UI)

Stay focused on platform boundaries in this skill; wire shared UI like any other Compose target:

- [Kotlin control flow](../../kotlin-control-flow/SKILL.md) — keeping common-code business branching explicit with `when`, guard conditions, exhaustiveness, and smart casts.
- [Compose state and effects](../../compose-state-and-effects/SKILL.md) — shared plain UI composables versus state-holder wiring and effect lifecycle.
- [Compose component design](../../compose-component-design/SKILL.md) — reusable shared Compose APIs (modifiers, slots).
