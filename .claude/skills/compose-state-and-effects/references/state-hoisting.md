# Compose state hoisting

## Core principle

Hoist state only as far as the logic needs it. Keep simple UI element state local, move shared UI element state to the lowest common composable owner, extract a plain state holder when UI-only behavior becomes a concept, and use a screen state holder when business logic or app data is involved. At the screen boundary, keep state-holder wiring separate from plain state-driven UI rendering.

## Review procedure

1. List the state, operations, app dependencies, event streams, and imperative effects involved.
2. Assign each item to the lowest owner that needs to read or change it using the decision guide below.
3. Extract a plain state holder only when coordinated UI-only behavior has become a concept.
4. When a screen mixes app wiring with layout, keep a small state-holder composable and move rendering to a plain state-driven composable.
5. Pass immutable UI state and explicit event callbacks across that boundary; keep UI mechanics in composition unless business logic needs their values.
6. Load focused effect, testing, focus, or deferred-read skills when those concerns need deeper treatment.
7. Finish when the UI can be previewed and tested without app dependencies, business work remains in the screen state holder, and no state has been hoisted farther than its logic requires.

## Decision guide

| Situation | Owner |
|---|---|
| One composable reads/writes simple state | Keep local with `remember` / `rememberSaveable` |
| Sibling or parent composables need to read/write it | Hoist state and events to their lowest common composable ancestor |
| Related UI element state plus UI logic is making a composable hard to read, preview, or test | Extract a plain state holder class remembered in composition |
| Repository calls, persistence, business rules, or screen UI state production are involved | Use a screen-level state holder such as a `ViewModel` or component |
| A screen composable collects app state/effects and also owns most layout | Keep a small wiring composable and extract a plain UI composable that takes immutable state and callbacks |

UI element state includes things like expansion, sheet visibility, scroll position, focus, text field editing state, selection, and animation/interaction state. Screen UI state is app data prepared for display.

If UI element state is an input to business logic, it may need to live in the screen state holder too. For example, text used to query repository-backed suggestions belongs with the state holder that produces those suggestions.

## Plain state holder trigger

Extract a plain state holder when several of these are true:

- Multiple related `remember` values are coordinated by the same callbacks.
- Scroll, focus, text, selection, or sheet state needs named operations such as `clear`, `submit`, `jumpToTop`, or `openFilters`.
- Derived UI flags are scattered through the composable.
- Child composables receive mechanics they do not conceptually own.
- Previews or tests must drive a long sequence of UI details to check one behavior.
- Helper functions need many state parameters just to keep the composable readable.

Do not extract for one boolean, one text field, or trivial show/hide logic. Ceremony is not separation of concerns.

## Pattern

Use a plain class for UI element state and UI logic, plus a `remember...State` function for composition-owned objects:

```kotlin
@Stable
class ProductSearchState(
    query: String,
    private val listState: LazyListState,
    private val focusRequester: FocusRequester,
) {
    var query by mutableStateOf(query)
        private set

    var filtersOpen by mutableStateOf(false)
        private set

    val canClear: Boolean
        get() = query.isNotEmpty()

    fun updateQuery(value: String) {
        query = value
    }

    fun clear() {
        query = ""
        focusRequester.requestFocus()
    }

    suspend fun jumpToTop() {
        listState.animateScrollToItem(0)
    }
}

@Composable
fun rememberProductSearchState(
    initialQuery: String = "",
    listState: LazyListState = rememberLazyListState(),
    focusRequester: FocusRequester = remember { FocusRequester() },
): ProductSearchState {
    return remember(listState, focusRequester) {
        ProductSearchState(initialQuery, listState, focusRequester)
    }
}
```

The composable renders from the state holder and calls intent-style methods. If a parent needs to coordinate the same UI behavior, accept the state holder as a parameter with a default:

```kotlin
@Composable
fun ProductSearchPanel(
    state: ProductSearchState = rememberProductSearchState(),
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    SearchField(
        query = state.query,
        onQueryChange = state::updateQuery,
        onClear = state::clear,
    )

    JumpToTopButton(onClick = {
        scope.launch { state.jumpToTop() }
    })
}
```

## Composition ownership

Plain state holders created with `remember` follow the composable lifecycle. This makes them a good home for Compose UI objects such as `LazyListState`, `FocusRequester`, `PagerState`, `DrawerState`, and `TextFieldState`.

Keep suspend UI operations that require a frame clock, such as scroll or drawer animations, in a composition-scoped coroutine (`rememberCoroutineScope`, `LaunchedEffect`, or another composition-owned scope). Do not move those calls to `viewModelScope`.

## Saving state

Use `rememberSaveable` or a custom `Saver` only for values that should survive Activity or process recreation, such as a query string, selected filter IDs, or a current tab key.

Do not try to save runtime objects like `LazyListState`, `FocusRequester`, coroutine scopes, or callbacks directly. Save the minimal serializable values needed to rebuild behavior.

## Split screen wiring from UI rendering

When a screen takes a `ViewModel`, component, controller, navigator, repository, or service, keep that dependency in a small state-holder composable. Collect app state and effects there, then pass immutable UI state and explicit event callbacks to a plain UI composable.

```kotlin
@Composable
fun ProfileScreen(component: ProfileComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onNameChange = component::onNameChange,
        onSaveClick = component::save,
        onBackClick = component::back,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Layout only.
}
```

Use the boundary deliberately:

| Concern | State-holder composable | Plain UI composable |
|---|---|---|
| Collect app/business state and one-shot effects | Yes | No |
| Hold dependency-injected objects | Yes | No |
| Accept immutable UI state and event callbacks | Usually passes them through | Yes |
| Own layout, modifiers, semantics, and test tags | No or minimal | Yes |
| Own Compose runtime objects such as `LazyListState` or `FocusRequester` | No | Yes, directly or in a plain UI state holder |
| Receive business-relevant values or intents derived from UI mechanics | Yes | Supplies them without exposing runtime objects |

Pass the smallest useful UI contract:

- Prefer a dedicated immutable `UiState` when the screen has cohesive state.
- Prefer explicit event callbacks over passing the whole state holder through the tree.
- Keep navigation as callbacks that describe user intent.
- Map domain models to UI models when direct use would pull business rules into rendering.
- Pass provider lambdas for frame-rate values that should be read in layout or draw, per [Compose performance](../../compose-performance/SKILL.md).

Handle navigation, snackbar, analytics, or event collection near the state holder, where the source and imperative target are available. If effect handling grows, extract a small sibling effect handler rather than passing the state holder into the UI composable. Use [Side effects](side-effects.md) for effect APIs, keys, cleanup, and stale captures.

Do not create a state-holder/UI overload for every small composable. Split at a screen or cohesive section boundary when doing so removes app dependencies from meaningful UI that should be previewed, tested, or reused.

Do not apply the split to tiny one-off composables that already take plain values and callbacks, design-system primitives that should expose slots and modifiers, or wrappers that would only forward one primitive without isolating an app dependency.

## RED/GREEN agent scenarios

For each scenario, establish RED by omitting or reverting the relevant rule, then restore the skill and require the GREEN outcome.

1. A screen takes a component, collects `StateFlow`, handles a navigation event, and owns most layout. GREEN keeps the component, collection, and effect handling in a small wiring composable, then extracts a plain UI composable with immutable state and callbacks.
2. Novel case: a search query drives repository-backed suggestions while a `LazyListState` and `FocusRequester` coordinate the UI. GREEN moves the query and suggestion logic to the screen state holder, but keeps the Compose runtime objects in the plain UI or a plain UI state holder.
3. Over-application counterexample: a stateless design-system badge takes plain values, slots, and a modifier. GREEN does not create a state-holder/UI overload or introduce a `ViewModel` merely for structural symmetry.

## Common mistakes

| Mistake | Fix |
|---|---|
| Hoisting every local state value to a parent "just in case" | Hoist to the lowest owner that actually reads or writes it |
| Extracting a plain state holder for one boolean | Keep simple private UI state local |
| Putting repository calls or product rules in a Compose state holder | Move that logic to a screen state holder such as a `ViewModel` or component |
| Keeping text or selection local when it drives repository-backed screen state | Move that input to the screen state holder with the business logic |
| Passing a state holder deep into unrelated children | Pass plain values and callbacks unless the child truly coordinates the holder's behavior |
| Treating the holder as a dumping ground for a whole screen | Split by cohesive UI behavior, such as search input, sheet coordination, or list controls |
| Calling animation suspend functions from `viewModelScope` | Use a composition-scoped coroutine |
| A screen composable takes a component and renders all layout | Extract a plain UI overload that takes state and callbacks |
| Child composables take a `ViewModel` or component | Pass only the values and callbacks each child needs |
| UI rendering performs navigation or collects app event flows | Handle effects beside the screen state holder |
| Every small composable gets a state-holder overload | Split only at screen or cohesive section boundaries |

## Related

- [Local state](local-state.md) — correct local `remember` and mutable state authoring.
- [Side effects](side-effects.md) — choose effect APIs and composition-scoped coroutine boundaries.
- [Compose focus navigation](../../compose-focus-navigation/SKILL.md) — focus state, requesters, and keyboard/D-pad behavior.
- [Compose UI testing patterns](../../compose-ui-testing-patterns/SKILL.md) — test plain state-driven UI without constructing the full app graph.
- [Kotlin API design](../../kotlin-api-design/SKILL.md) — keep shared UI plain while platform services stay behind semantic boundaries.
