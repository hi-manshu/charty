# Composition contracts

## `@ReadOnlyComposable`

`@ReadOnlyComposable` declares that a composable only reads composition state:
it does not emit layout, allocate positional state, invoke composable content, or
run effects. The runtime can then avoid allocating a group for accessor-style
calls such as design-system token accessors.

Add the annotation only when every composable call in the body is itself
read-only, or the body only reads values such as `LocalFoo.current` and performs
pure computation. Do not add it because a function merely looks small.

```kotlin
@Composable
@ReadOnlyComposable
fun appSpacing(): Dp = LocalDimensions.current.spacing
```

Do not annotate a body that calls layout (`Box`, `Column`, `Text`), effects
(`LaunchedEffect`, `DisposableEffect`, `SideEffect`, `produceState`),
`remember`, a composable lambda, or any normal composable.

```kotlin
@Composable
@ReadOnlyComposable
fun Header(): Int {
    Box {}
    return 42
}
```

The annotation is fixed by an overridden or abstract declaration's existing
contract. If the base is not read-only, do not add it locally; refactor the base
only when that contract change is justified.
