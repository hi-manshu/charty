# Kotlin function ownership

## Core principle

Put a function on the smallest accurate semantic owner. Extension syntax changes call shape, not ownership.

Reject primitive, common, and library-owned extensions by default: they create false ownership, domain pollution, noisy completion/imports, and collisions.

## Procedure

Apply in order.

### 1. Name the semantic owner

Name the operation and the concept that owns it. If ownership is unclear, stop before selecting syntax.

### 2. Reject a misleading receiver early

For `String`, primitives, collections, `Flow`, framework, or third-party receivers, require **all**:

- Narrow `private`/`internal` cohesive scope.
- Valid for every receiver value.
- No policy, state, I/O, or dependency.
- Materially clearer receiver syntax.
- No better project-owned owner.

Any failure forbids an extension on that receiver; choose a non-extension form in step 3. `private fun <T> MutableList<T>.swap(...)` can pass: it is list-native, policy-free, and algorithm-local.

### 3. Choose the function form

| Meaning | Prefer |
|---|---|
| Project-owned intrinsic behavior | Member |
| Cross-type, stateless operation | Top-level function |
| Construction or parsing | Target factory or named top-level function |
| Retained policy, state, I/O, clock, locale, or dependencies | Injected service/collaborator |
| Type-native operation with a clearer receiver and every step-2 gate passed | Extension |

Use a service/collaborator only when behavior retains policy, state, I/O, clock/locale, or dependencies; otherwise use explicit parameters on a stateless function.

### 4. Move behavior and callers

Move the implementation, then update calls, imports, and function references. Preserve or deprecate public entry points unless this is an explicit breaking release; add non-public migration support only for concrete consumers.

```kotlin
// Before: String falsely owns UserId construction.
fun String.toUserId(): UserId = UserId(this)

// After: UserId owns construction.
@JvmInline
value class UserId private constructor(val value: String) {
    companion object {
        fun parse(raw: String): UserId = UserId(raw)
    }
}

val id = UserId.parse(raw)
```

### 5. Verify and finish

For every form, check visibility, imports, collisions, and compatibility. For extensions, also check nullable receivers, generics, and future-member precedence. Compile and test; on failure, narrow the API or return to step 1.

## Rationalizations

| “But…” | Counter |
|---|---|
| Fluent syntax | Readability does not create ownership. |
| Kotlin uses extensions | Idiom still requires accurate semantics. |
| It is private/internal | Scope helps only when every gate passes. |
| Utility objects are worse | Use a top-level function or target factory. |
| Default policy is obvious | Time zone/locale defaults are policy; keep them explicit. |
| Already in the PR | Existing code does not prove ownership. |

## Red flags

- Domain meaning on `String`, numbers, collections, `Flow`, or vendor types.
- Clock, locale, I/O, policy, or dependencies hidden in an extension.

## Common mistakes

| Mistake | Fix |
|---|---|
| `Long.toDisplayDate()` | A formatter owns time-zone/locale policy. |
| Extension hides parsing | Use `Type.parse(raw)` or a named parser. |
| Public library-type extension | Reclassify it using steps 1-3. |

## Related

- [Value classes](value-classes.md)
