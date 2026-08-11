# Compose: side effects

## Core principle

Composable bodies describe UI. They can be recomposed, skipped, or abandoned. Work that changes the outside world belongs in an effect API whose lifecycle matches the work.

## Pick the smallest effect

| Need | API |
|---|---|
| Publish Compose state to non-Compose code after every successful recomposition | `SideEffect` |
| Register/unregister a listener, callback, observer, or resource | `DisposableEffect(keys...)` |
| Run suspending, deferred, or keyed one-shot work | `LaunchedEffect(keys...)` |
| Launch suspending work from a user event callback | `rememberCoroutineScope()` |
| Convert Compose snapshot reads into a Flow inside a coroutine | `snapshotFlow { ... }` inside `LaunchedEffect` |

## Effect keys

Keys define restart identity. When any key changes, the old effect is cancelled/disposed and a new one starts.

```kotlin
// ✅ Restart collection when userId changes
LaunchedEffect(userId) {
    repository.events(userId).collect { event -> handle(event) }
}

// ❌ Unit hides a changing input; collection keeps using the first userId
LaunchedEffect(Unit) {
    repository.events(userId).collect { event -> handle(event) }
}
```

Use stable, semantic keys:

- Use the thing whose lifecycle the effect follows: `userId`, `screenId`, `lifecycleOwner`, `focusRequester`.
- Do not use broad objects (`state`, `viewModel`) when only one property matters.
- Do not add changing lambdas as keys unless you really want restarts on every lambda change.

## Avoid stale captures

For long-running effects that should not restart but need the latest callback or value, use `rememberUpdatedState`.

```kotlin
@Composable
fun Timeout(onTimeout: () -> Unit) {
    val latestOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(Unit) {
        delay(1_000)
        latestOnTimeout()
    }
}
```

Use this when the lifecycle is "start once" but the invoked lambda should stay fresh. Common cases:

- A timeout or splash effect should not restart when `onTimeout` changes, but it should call the latest callback.
- A lifecycle observer should stay registered to the same owner, but invoke the latest `onStart` / `onStop` lambdas.
- A long-running collector should keep its collection lifecycle, but call the latest event handler.

Do not use `rememberUpdatedState` to avoid choosing proper keys. If the changed value should restart the work, make it a key instead:

```kotlin
// BAD: userId changes should restart the collection, not update a captured value.
val latestUserId by rememberUpdatedState(userId)
LaunchedEffect(Unit) {
    repository.events(latestUserId).collect { event -> handle(event) }
}

// GOOD: the collection lifecycle follows userId.
LaunchedEffect(userId) {
    repository.events(userId).collect { event -> handle(event) }
}
```

### `rememberUpdatedState` values are stale inside `remember {}` blocks

`rememberUpdatedState` returns a `State` object whose `.value` is updated on every recomposition. The "latest" behavior only helps when the State is **read lazily** — inside an effect body or a lambda that runs later — not when the value is captured eagerly.

Inside a `remember {}` block the producer lambda runs once. Reading the delegate there snapshots the current `.value` into the remembered object — future State updates never reach it:

```kotlin
val latestChannelId by rememberUpdatedState(channelId)

// ❌ BAD — channelId is read once when remember's lambda executes;
// the destination holds the initial value forever
val destination = remember {
    Destination(channelId = latestChannelId)
}

// ✅ GOOD — skip rememberUpdatedState; key remember on the changing value
val destination = remember(channelId) {
    Destination(channelId = channelId)
}

// ✅ ALSO GOOD — wrapping lambda defers the read to each invocation
val destination = remember {
    Destination(channelId = { latestChannelId })
}
```

The same trap applies anywhere a `rememberUpdatedState` delegate is **read eagerly** rather than deferred behind a lambda or effect body: data classes constructed in `remember`, objects built once in `DisposableEffect`'s setup block, or any expression evaluated at creation time.

When the captured value should trigger recreation of the remembered object, make it a `remember` key and skip `rememberUpdatedState` entirely. Reserve `rememberUpdatedState` for values that must stay fresh inside a long-lived scope (effect coroutine, event callback) **without** restarting that scope.

`rememberUpdatedState` also does not make render state "non-recomposing." If the UI needs to display a changing value, read normal `State` in composition or use [Compose performance](../../compose-performance/SKILL.md) for frame-rate values.

## Collecting Flow

Use `LaunchedEffect` for **side-effect/event flows**: snackbars, navigation events, analytics events, focus commands, or other streams where each emission triggers imperative work.

```kotlin
LaunchedEffect(events) {
    events.collect { event ->
        snackbarHostState.showSnackbar(event.message)
    }
}
```

Do not collect render state imperatively just to mutate local state. For UI state, collect near the state holder and pass plain values into the UI composable—the **state-holder vs UI split**, `collectAsStateWithLifecycle()` / `collectAsState()`, and preview-friendly wiring are covered in [State hoisting](state-hoisting.md). Do not duplicate that architecture here.

On Android, prefer lifecycle-aware collection where available; use `collectAsState()` on targets without lifecycle-aware APIs.

For Compose state reads, use `snapshotFlow`:

```kotlin
LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .distinctUntilChanged()
        .collect { index -> analytics.visibleIndex(index) }
}
```

`snapshotFlow { ... }.map { ... }` without a terminal `collect` does nothing.

## User events

Use `rememberCoroutineScope()` when a click or gesture starts suspending work:

```kotlin
@Composable
fun SaveButton(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {
                snackbarHostState.showSnackbar("Saved")
            }
        },
    ) {
        Text("Save")
    }
}
```

Avoid "event flag" state just to trigger a `LaunchedEffect`. The click already is the event.

## Registration and cleanup

Use `DisposableEffect` for paired setup/teardown:

```kotlin
@Composable
fun ObserveLifecycle(owner: LifecycleOwner, observer: LifecycleObserver) {
    DisposableEffect(owner, observer) {
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }
}
```

Every registration path should have a matching `onDispose` cleanup path.

## Common mistakes

| Mistake | Diagnosis | Fix |
|---|---|---|
| Network request directly in the composable body | Side work in composition | Usually move to a ViewModel/state holder; use `LaunchedEffect` only for UI-owned keyed work |
| Analytics property written from the composable body | Side work in composition | Use `SideEffect` when it should publish after every successful recomposition |
| Impression/event logged from the composable body | Side work in composition | Use `LaunchedEffect(key)` when it should run once for that key |
| `LaunchedEffect(Unit)` captures changing `id` | Missing key | Key by `id`, or use `rememberUpdatedState` if it must not restart |
| `rememberUpdatedState(id)` used so `LaunchedEffect(Unit)` keeps running after `id` changes | Hidden lifecycle bug | Key the effect by `id` |
| Long-lived effect invokes an old callback after recomposition | Stale capture | Wrap the callback with `rememberUpdatedState` and call the wrapper inside the effect |
| `rememberUpdatedState` delegate read directly in `remember {}` (e.g. `Destination(id = latestId)`) | Value captured once, never refreshed | Make the value a `remember` key: `remember(id) { Destination(id = id) }` |
| `LaunchedEffect(state) { ... }` restarts too often | Overly broad key | Key by the specific property |
| `LaunchedEffect(...) { nonSuspendSetter() }` | Wrong effect type | Usually `SideEffect`; keep `LaunchedEffect` only for keyed one-shot/deferred work |
| Listener added in `LaunchedEffect` with no cleanup | Missing disposal | Use `DisposableEffect` |
| Launching from click by setting `shouldShowSnackbar = true` | Event flag anti-pattern | Use `rememberCoroutineScope()` in the click callback |
| `if (isFocused) { … }` or focus read in composable body for side work | Side work during composition | `LaunchedEffect(focused) { … }` or `snapshotFlow` |
| `onSizeChanged { heightState = it.height }` on measured composable | Layout → composition back-write if a sibling reads `heightState` in composition | Siblings must consume height in measure phase, not `Modifier.height(state.dp)` in composition |

## Focus and measurement

**Focus:** Reading focus in the composable body to drive **side work** (preloading, analytics, toasts) runs that work during composition. Observe focus in an effect instead:

```kotlin
// ❌ BAD — side work runs during composition every time `focused` is true,
// including transient focus passes; `SideEffect` re-runs after every successful recomposition
@Composable
fun Preloader(interactionSource: MutableInteractionSource) {
    val focused by interactionSource.collectIsFocusedAsState()
    if (focused) {
        preloadImages()
    }
}

// ✅ GOOD — side work in a keyed effect
@Composable
fun Preloader(interactionSource: MutableInteractionSource) {
    val focused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(focused) {
        if (focused) preloadImages()
    }
}
```

Use `snapshotFlow { … }` inside `LaunchedEffect` when you need to sample multiple snapshot reads or debounce rapid changes without keying the effect on every derived value. For TV/D-pad focus navigation semantics, see [Compose focus navigation](../../compose-focus-navigation/SKILL.md).

**Measurement:** `onSizeChanged` / `onGloballyPositioned` are valid **callbacks**, but they fire during the layout phase. Writing snapshot state there is only safe if no earlier phase reads it. If a sibling reads that state in composition, layout is back-writing into composition and the sibling will recompose every measure pass. Apply captured dimensions in `Modifier.layout` (see [Compose component design](../../compose-component-design/SKILL.md) and [Compose performance](../../compose-performance/SKILL.md)).

## Red flags during review

- "This only runs once" about code in a composable body.
- `LaunchedEffect(Unit)` in a function with changing parameters.
- A flow chain inside an effect with no terminal collection.
- Effects whose keys are chosen to silence lint instead of model lifecycle.
- Callback lambdas used from long-lived effects without either a key or `rememberUpdatedState`.
- `rememberUpdatedState` delegate read eagerly inside a `remember {}` block or object constructor — the value is captured once and never refreshes.
