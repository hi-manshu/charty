# Compose state authoring

Not every `remember { … }` belongs here. This reference covers **local UI
state** (`remember { mutableStateOf(…) }`, `mutableStateListOf` /
`mutableStateMapOf`). Other remembered APIs live elsewhere:

- **`rememberCoroutineScope` / `rememberUpdatedState`** → [Side effects](side-effects.md)
- **`rememberLazyListState` / `rememberScrollState`** used for frame-rate reads → [Compose performance](../../compose-performance/SKILL.md)
- **Focus navigation, focus state, `FocusRequester` ownership, behavior** → [Compose focus navigation](../../compose-focus-navigation/SKILL.md)

## Core principle

A `@Composable` is a function the runtime re-runs whenever its inputs change.
Writing local state correctly asks one question:

1. **Mutable local state** — does my `var` survive recomposition *and* trigger it? If not, it silently resets on every recompose and writes are invisible.

Get it wrong and state vanishes or writes become invisible.

## When to use this skill

You're writing or reviewing Compose code and you see any of these:

- `var x = …` inside a `@Composable fun` or any composable lambda (`Column { var x = … }`)
- A composable whose visible state mysteriously resets on rotation, theme change, or recomposition

## 1. `var` in a composable must be State-backed

Recomposition re-executes the composable from the top. A local `var` is *re-initialized* on every pass — last recompose's value is gone, and writing to it doesn't tell the runtime to recompose.

```kotlin
// ❌ BAD — counter resets on every recomposition; clicks never update the UI
@Composable
fun Counter() {
    var count = 0
    Button(onClick = { count++ }) { Text("$count") }
}

// ❌ ALSO BAD — same rule applies inside composable content lambdas
@Composable
fun Wrapper() {
    Row {
        var count = 0         // Row's content lambda is @Composable too
        // …
    }
}
```

```kotlin
// ✅ GOOD — `remember` survives recomposition, `mutableStateOf` triggers it
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { Text("$count") }
}
```

Two pieces and both matter:

- `remember { … }` — *survives recomposition*. Without it the value is re-created each time.
- `mutableStateOf(…)` — *triggers recomposition*. Without it, mutations are invisible to the runtime.

For collections, prefer `mutableStateListOf` / `mutableStateMapOf` (also `remember`-ed). They emit Snapshot reads on every read and Snapshot writes on every mutation. A `remember { mutableStateOf(mutableListOf<X>()) }` followed by `list.add(x)` will *not* recompose, because `MutableList.add` doesn't go through the State setter — you'd have to replace the value (`state = state + x`).

### Back-writing snapshot state during composition

**Back-writing** means writing observable state in a phase that triggers invalidation of an earlier (or the current) phase. Mutating `mutableState*` from the composable body back-writes into the same composition pass and schedules another. Do not rebuild derived data this way:

```kotlin
// ❌ BAD — clear + putAll on every composition
val merged = remember { mutableStateMapOf<Key, ViewState>() }
merged.clear()
merged.putAll(parent)
merged.putAll(overlay)

// ✅ GOOD — immutable snapshot remembered from inputs
val merged = remember(parent, overlay) {
    if (overlay.isEmpty()) parent else parent + overlay
}
```

If the result is read-only for the current inputs, `remember(keys) { … }` is
enough. See [Compose performance](../../compose-performance/SKILL.md) for
cross-row measurement and measure-phase fixes.

### When this rule does NOT apply

- **Inside `remember { … }`'s producer block.** That runs once per key change, not on every recompose. A local `var` there is fine: `val builder = remember { mutableListOf<X>().apply { var n = 0; … } }`.
- **In non-`@Composable` lambdas passed *out* of a composable.** `onClick = { var a = 0; … }` is a plain `() -> Unit`. Local vars there are normal Kotlin.
- **In plain (non-`@Composable`) helper functions.** Only composable scopes are affected.

## Related

If a composable needs `LaunchedEffect`, `DisposableEffect`, `SideEffect`,
`rememberCoroutineScope`, `rememberUpdatedState`, `snapshotFlow`,
snackbar/navigation handling, analytics, or Flow collection, use [Side
effects](side-effects.md).

Focus splits by question: **navigation, focus state, `FocusRequester`
ownership, behavior** → [Compose focus
navigation](../../compose-focus-navigation/SKILL.md); **when** to call
imperative `requestFocus` (effect timing, lifecycle, keys, API choice) →
[Side effects](side-effects.md).

This skill is about authoring Compose state correctly. `rememberUpdatedState` is effect capture state, not a general replacement for `remember { mutableStateOf(...) }`. Side effects have separate lifecycle and keying rules, and keeping them in one focused skill avoids two sources of truth.

## Quick reference

| Symptom | Diagnosis | Fix |
|---|---|---|
| `var x = …` inside `@Composable fun` body | Not recomposition-safe (§1) | `var x by remember { mutableStateOf(…) }` |
| `var x = …` inside `Column { … }` / `Row { … }` content lambda | Same — content lambdas are `@Composable` (§1) | Same fix |
| `remember { mutableStateOf(list) }` then `.add(x)` not recomposing | Mutation bypasses State setter | Use `mutableStateListOf`, or replace the value: `state = state + x` |
| `stateMap.clear(); stateMap.putAll(...)` in composable body | Back-writing composition → composition | `remember(keys) { derivedSnapshot }` |

## When NOT to apply

- **Tests** with `composeTestRule.setContent { … }` follow the same rules — they're production composables.
- **`produceState`** has its own producer block that runs in a coroutine; you don't need `LaunchedEffect` *inside* it.
- **`derivedStateOf`** has its own concerns around stability and equality — out of scope here; it's about *preventing* recomposition, not authoring state.

## Red flags during review

| Thought | Reality |
|---|---|
| "It's a small composable, the bare `var` is fine" | Recomposition can fire at any time. The reset is non-deterministic by design — and a single bug report later. |
| "I always reach for `LaunchedEffect` because it's the one I know" | Use [Side effects](side-effects.md); effect API choice depends on lifecycle and keys. |
| "I'll just `.add()` to the remembered list" | A `mutableStateOf(List)` doesn't observe internal mutation — use `mutableStateListOf` or replace the value. |
