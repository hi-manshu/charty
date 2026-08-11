---
name: compose-state-and-effects
description: Use when writing or reviewing Jetpack Compose state ownership, remember state, state hoisting, screen state holders, LaunchedEffect, DisposableEffect, SideEffect, Flow collection, navigation, snackbar, analytics, or focus requests.
---

# Compose state and effects

## Core principle

Give every piece of UI state one lowest responsible owner, then run imperative
work through the effect whose lifecycle follows that owner. Composition renders;
state and effects make rendering change safely.

## Procedure

1. Inventory mutable UI state, app state, event streams, app dependencies, and
   imperative work in the affected screen or component.
2. Place each state value at its lowest necessary owner: local UI state,
   hoisted state, a plain UI state holder, or a screen state holder.
3. Keep app wiring and business state at the screen boundary; expose plain UI
   state and explicit callbacks to previewable rendering.
4. Choose an effect API whose lifecycle matches the work, and key it by the
   semantic input that should restart or dispose it.
5. Load the focused reference for every material concern below. Do not use a
   reference merely because its topic is adjacent.
6. Route frame-rate reads, cross-phase back-writing, and
   `@ReadOnlyComposable` contracts to [Compose performance](../compose-performance/SKILL.md).
7. Finish when every state value has one owner, every effect has a justified
   lifecycle and key, and the UI can be previewed and tested without app
   dependencies.

## Topic router

| Signal | Read |
|---|---|
| Bare local `var`, `remember { mutableStateOf(...) }`, state lists/maps, or reset state | [Local state](references/local-state.md) |
| State shared by siblings, UI state holders, ViewModel/component wiring, or previewable screen boundaries | [State hoisting](references/state-hoisting.md) |
| `LaunchedEffect`, `DisposableEffect`, `SideEffect`, `snapshotFlow`, `rememberCoroutineScope`, `rememberUpdatedState`, `produceState`, imperative `requestFocus`, callbacks, event Flow collection, snackbar, navigation, or analytics | [Side effects](references/side-effects.md) |
| Focus ownership and keyboard/TV/D-pad behavior | [Compose focus navigation](../compose-focus-navigation/SKILL.md) |
| Tests or previews for the resulting UI contract | [Compose UI testing patterns](../compose-ui-testing-patterns/SKILL.md) |

## RED/GREEN agent scenarios

1. RED keeps a component, collected `StateFlow`, navigation event, and screen
   layout in one composable. GREEN leaves wiring and effects at the screen
   boundary and gives plain rendering immutable state plus callbacks.
2. Novel case: a query drives repository suggestions while a list state and
   focus requester coordinate UI behavior. GREEN puts query and suggestions in
   the screen state holder, but keeps Compose runtime objects in plain UI state.
3. Counterexample: a one-off expandable badge has one private Boolean. GREEN
   keeps it local and does not introduce a state holder or an effect.
