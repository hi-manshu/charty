# Compose stability diagnostics

## Core principle

Compose parameter fixes start from evidence. First identify the compiler mode and the parameter comparison behavior, then change the model or call site that is actually defeating skipping.

With Kotlin 2.0.20+ strong skipping is enabled by default. Unstable parameters no longer automatically make restartable composables non-skippable, but unstable parameters compare by instance identity (`===`) while stable parameters compare by equality (`equals`). Churny unstable instances can still defeat skipping.

## Diagnostic procedure

1. Confirm the symptom: recomposition counts, compiler report output, or a suspected churny parameter.
2. Identify compiler mode: Kotlin/Compose compiler version and whether strong skipping is enabled.
3. Generate or read the Compose compiler reports for the shipped variant.
4. For each suspicious parameter, decide whether the problem is stability semantics, instance churn, or a caller-created lambda/derived value.
5. Apply the lightest fix that makes the type/call site truthful.
6. Re-measure the same interaction or re-read the same report before claiming the issue is fixed.

## 1. Interpret strong skipping first

On Kotlin 2.0.20+, strong skipping is enabled by default. In that mode:

- Restartable composables are skippable even when parameters are unstable, unless explicitly opted out.
- Stable parameters compare with `equals`.
- Unstable parameters compare with instance equality (`===`).
- Lambdas inside composables are automatically remembered based on captures.

Ask: "will these parameters compare the way I expect, and are callers creating new unstable instances every frame?"

For older compiler setups or strong skipping disabled, the legacy rule still matters: a restartable composable with unstable parameters may be restartable but not skippable.

## 2. Generate compiler reports

With Kotlin 2.0+ the Compose Compiler is configured through the Kotlin Gradle plugin:

```kotlin
plugins {
    alias(libs.plugins.android.application) // or android.library / jvm
    alias(libs.plugins.kotlin.android)      // or kotlin.multiplatform / kotlin.jvm
    alias(libs.plugins.compose.compiler)
}

if (providers.gradleProperty("composeReports").orNull == "true") {
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
    }
}
```

Then build the variant whose compiler configuration you care about, for example:

```bash
./gradlew :app:assembleRelease -PcomposeReports=true
```

Use release/non-debuggable builds for runtime profiling. Compiler reports are build-time outputs, so the important thing is matching the variant and compiler flags you ship.

Key files:

| File | What it tells you |
|---|---|
| `<module>-classes.txt` | Stability of classes and properties |
| `<module>-composables.txt` | Restartable/skippable status and parameter stability |
| `<module>-composables.csv` | Same data in sortable form |
| `<module>-module.json` | Aggregate metrics |

## 3. Fix only the proven parameter problem

Pick the lightest fix that makes the type's immutability or equality semantics true.

### Immutable collections

If reports show collection interfaces on UI state, prefer `kotlinx.collections.immutable` at UI-state boundaries:

```kotlin
// Before: unstable collection interfaces
data class UiState(val items: List<Item>, val tags: Set<String>)

// After: immutable collection contracts
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

data class UiState(val items: ImmutableList<Item>, val tags: ImmutableSet<String>)
```

Producers convert once at the boundary with `.toImmutableList()` / `.toImmutableSet()`.

### `@Immutable` / `@Stable`

- Use `@Immutable` when every property is effectively immutable and equality describes all observable state.
- Use `@Stable` for types whose mutable state is observable by Compose, typically via `MutableState`.

Do not annotate to silence a report. A false stability promise can produce stale UI.

### Third-party immutable types

For types you cannot annotate but can truthfully treat as immutable, use `stabilityConfigurationFiles`:

```kotlin
composeCompiler {
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("compose_stability.conf"),
    )
}
```

```text
java.math.BigDecimal
java.math.BigInteger
java.time.*
kotlinx.datetime.*
```

Only list types you are willing to promise are immutable. Do not list mutable types such as `java.util.Date`.

## 4. Stabilize lazy item inputs

When lazy item recomposition comes from call-site churn, stabilize the values passed to each item instead of annotating models blindly.

Hoist and remember per-item inputs that are stable for the item's lifetime:

```kotlin
// ❌ BAD — new lambda instances when parent recomposes
items(list, key = { it.id }) { item ->
    RowCard(
        onClick = { onItemClick(item.id) },
        isHighlighted = { item.id == selectedId },
    )
}

// ✅ GOOD — stable captures for this item instance
items(list, key = { it.id }) { item ->
    val onClick = remember(item.id) { { onItemClick(item.id) } }
    val isHighlighted = remember(item.id, selectedId) { item.id == selectedId }
    RowCard(onClick = onClick, isHighlighted = isHighlighted)
}
```

Also hoist row position metadata (`isFirst`, `isLast`, corner radii) with `remember(index) { … }` when the value depends only on index — but do not expect this alone to fix back-writing or cross-row measurement bugs.

Verify focus moves and insertions with recomposition-count assertions after hoisting.

## Quick reference

| Symptom | Diagnosis | Fix |
|---|---|---|
| Kotlin 2.0.20+ but old docs say unstable means non-skippable | Strong skipping changed the default | Check comparison semantics and instance churn instead |
| `unstable val items: List<Item>` | Interface collection | Use `ImmutableList<Item>` or another true immutable wrapper |
| `unstable val price: BigDecimal` | External immutable type | Add to stability config |
| `@Immutable` on a type with mutable internals | False promise | Fix the model or remove the annotation |
| Composable skips poorly despite strong skipping | New unstable instance each recomposition | Remember, hoist, or make the type stable/equality-based |
| Lazy items recompose on parent recompose despite unchanged data | New lambda or derived-value instance per parent recompose (§4) | Hoist per-item with `remember(item.id) { … }` |
| Reports not generated | Compose compiler plugin missing or flag not set | Apply `org.jetbrains.kotlin.plugin.compose` and enable destinations |

## When NOT to apply

- The issue is back-writing across phases or cross-row measurement reads. Use [Deferred reads](deferred-reads.md).
- The issue is a fast-changing `State` read in composition, such as scroll or animation. Use [Deferred reads](deferred-reads.md).
- The recomposition count matches real data changes.
- The bug is wrong data or stale state, not excess work.
- The code is test-only and readability is more important than report cleanliness.

## Related

- [Deferred reads](deferred-reads.md) - frame-rate state should often be read in layout/draw rather than composition.
- [Diagnosis](diagnosis.md) - use this when the recomposition axis is not yet known.
