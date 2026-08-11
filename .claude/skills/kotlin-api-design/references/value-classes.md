# Kotlin value class vs data class

## Core principle

Prefer `@JvmInline value class` for single-field types that carry domain meaning. Data classes are for aggregating multiple fields.

## Review procedure

1. Find single-property wrappers, primitive-heavy APIs, and `@Immutable` wrappers in UI state.
2. Decide whether the single value is a real domain distinction. If not, keep the primitive or use a typealias.
3. Check whether replacing the type changes equality, serialization, Java interop, or hot-path boxing.
4. Convert only when the domain meaning is clear and the contract changes are acceptable.
5. Re-run the affected compiler/tests; for Compose performance work, re-check compiler reports or recomposition evidence.

## Decision flow

| Situation | Prefer |
|---|---|
| Single field + domain-meaningful (`UserId`, `EmailAddress`, `Percentage`) | `@JvmInline value class` |
| Single field + no domain meaning (just grouping) | Type alias or keep the primitive |
| Multiple fields | Data class |
| Needs custom `equals`/`hashCode` beyond the wrapped value | Data class (value classes delegate to the underlying type) |
| Used as a generic type argument or nullable in a proven hot path | Data class or primitive |

```kotlin
// GOOD: domain-meaningful single field
@JvmInline value class UserId(val value: String)
@JvmInline value class EmailAddress(val value: String)
@JvmInline value class Percentage(val value: Float)

// BAD: data class wrapping a single domain field
data class UserId(val value: String)

// BAD: value class with no domain meaning
@JvmInline value class Wrapper(val value: String) // just use the String, or a type alias

// BAD: value class needing custom equality
@JvmInline value class CaseInsensitiveString(val value: String)
// value class equals delegates to String equals, which IS case-sensitive
// Use a data class if you need different equality semantics
```

## Compose stability procedure

When a Compose report points at a single-field wrapper:

1. Confirm the underlying type is stable (`String`, primitives, or another stable type).
2. Prefer a value class over `@Immutable` on a wrapper whose only job is type distinction.
3. Do not change public serialization/API contracts just to silence a report.

```kotlin
// Before: primitive value can be mixed up with other strings
data class UiState(val userId: String)

// After: domain type is stable at the Compose boundary
@JvmInline value class UserId(val value: String)
data class UiState(val userId: UserId)
```

## Refactor checks

Before replacing an existing wrapper, check the contract that callers observe:

| Check | Action |
|---|---|
| JSON/API format matters | Verify serialization. `@Serializable data class A(val value: String)` encodes as an object; a value class encodes as the wrapped value. |
| Custom equality or hashing is required | Keep a data class. Value-class equality follows the wrapped value. |
| Callers use `copy()` or destructuring | Keep a data class or update callers deliberately. Value classes do not provide data-class conveniences. |
| Java or reflection-heavy framework boundary | Verify interop. Java callers see the underlying type; generic/`Any` use boxes. |
| Nullable/generic/vararg hot path | Measure before converting; those uses box. |
| Constructor body, `lateinit`, delegated properties, backing fields | Keep a data class or redesign; value classes only store the constructor value. |

## Packing multiple values only after evidence

Do not replace a clear multi-field data class with bit-packing unless profiling shows allocation cost on a hot path. If needed, Compose provides `packFloats`, `packInts`, and matching `unpack*` functions in `androidx.compose.ui.util`:

```kotlin
@JvmInline value class Offset(val packedValue: Long)

fun Offset(x: Float, y: Float): Offset = Offset(packFloats(x, y))
val Offset.x: Float get() = unpackFloat1(packedValue)
val Offset.y: Float get() = unpackFloat2(packedValue)
```

## Common mistakes

| Mistake | Fix |
|---|---|
| Data class wrapping a single domain field | Replace with `@JvmInline value class` |
| Value class with no domain meaning (just a wrapper) | Use a type alias or the primitive directly |
| Value class needing custom equality | Use a data class instead |
| Value class as generic type argument in a hot path | Measure boxing cost; keep the primitive/data class if it matters |
| `@Immutable` annotation on a type that could be a value class | Replace with a value class when the underlying type is stable |
| Forgetting `@JvmInline` annotation | Always pair `value class` with `@JvmInline` for single-field classes |

## Red flags during review

- A data class with exactly one property
- A `String`, `Long`, or `Int` used where different values should not be interchangeable (e.g., `fun transfer(from: String, to: String, amount: Long)`)
- An `@Immutable` annotation on a single-field wrapper
- A type alias used for domain distinction where value-class semantics are needed (type aliases are type-erased, no runtime protection)

## When NOT to apply

- The type needs multiple fields → data class
- The type needs custom `equals`/`hashCode` → data class
- The type is used heavily as a nullable or generic in performance-critical code → measure autoboxing cost first
- The project does not need the type-safety distinction → a type alias or primitive is sufficient
- The replacement would silently change JSON, Java, reflection, or framework behavior

## Related

- [Compose performance](../../compose-performance/SKILL.md) — diagnose unstable Compose parameters; value classes are one fix
