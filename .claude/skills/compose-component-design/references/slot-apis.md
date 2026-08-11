# Compose: slot API pattern

## Core principle

A reusable Compose component describes layout structure. Callers provide variable visual content through slots.

## API review procedure

1. Confirm the component is reusable. For a true single-use composable, do not add slot ceremony.
2. Mark which regions vary by caller: headline, supporting text, leading visual, trailing visual, actions, body.
3. Replace caller-controlled, unconstrained primitive content and shape flags
   with slots. Preserve primitive parameters when they intentionally enforce a
   semantic, design-system, constrained-type, or measured fast-path contract.
4. Add receiver scopes only when the slot is emitted inside a layout whose scope APIs callers should use.
5. Make absent optional regions nullable (`null`), so the component can omit their containers and spacing.
6. Put repeated default content or tokens in `XxxDefaults`.
7. Pair this with the `modifier` rules in [Modifier and layout](modifier-layout.md).

## 1. Replace primitive content with `@Composable` slots

Where the component asks for caller-controlled *content*, prefer a `@Composable () -> Unit` slot. Where the slot is structurally required, leave it non-nullable with no default. Where it's optional, make it nullable with a `null` default.

```kotlin
// ❌ BAD — primitive parameters; trailing area is the only slot; everything else is locked
@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) { … }
```

```kotlin
// ✅ GOOD — every visual region is a slot; the row describes structure, not content
@Composable
fun SettingsRow(
    headlineContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) { … }
```

Call sites stay short when the typical content is a one-liner:

```kotlin
SettingsRow(
    headlineContent = { Text("Account") },
    leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
    trailingContent = { SettingsRowDefaults.Chevron() },
    onClick = { … },
)
```

The unusual cases no longer require new component parameters:

```kotlin
SettingsRow(
    headlineContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Inbox")
            Spacer(Modifier.width(8.dp))
            Badge { Text("3") }
        }
    },
    onClick = { … },
)
```

### Slot naming

- Use `xxxContent` for free-form `@Composable () -> Unit` slots (`headlineContent`, `supportingContent`, `trailingContent`) — matches Material 3.
- Use a singular noun (`title`, `icon`, `actions`) when the slot is semantically constrained and the component name disambiguates (`Scaffold(topBar = { … }, bottomBar = { … }, floatingActionButton = { … })`).
- Don't use `content` *and* other `xxxContent` slots together — pick one convention per component.

## 2. Scope receivers when the slot emits into a layout

If the slot's content will sit inside a `Row`/`Column`/`Box` whose layout features (`Modifier.weight`, `BoxScope.matchParentSize`, alignment) should be available to the caller, declare the slot as a receiver lambda: `@Composable RowScope.() -> Unit`.

```kotlin
// ❌ BAD — actions render inside a Row, but callers can't use RowScope.weight()
@Composable
fun MyTopBar(
    title: @Composable () -> Unit,
    actions: @Composable () -> Unit = {},   // ← caller has no Row scope
)
```

```kotlin
// ✅ GOOD — caller gets RowScope; .weight() and alignment-by works inside
@Composable
fun MyTopBar(
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
)
```

This is what makes `TopAppBar(actions = { IconButton(…); IconButton(…) })` work — the caller is implicitly inside a `RowScope`.

Don't bolt a scope receiver onto every slot reflexively. The receiver should match the actual parent layout the slot emits into. If the slot is rendered inside a `Box`, use `BoxScope`. If it's inside a `Column`, use `ColumnScope`. If the parent is not a standard layout (or none of its scope APIs are useful in slot content), no receiver.

## 3. Optional slots — nullable with `null` default

For slots that may be absent, prefer `(@Composable () -> Unit)? = null` over `@Composable () -> Unit = {}`:

```kotlin
// ❌ BAD — empty default; "no leading content" is the empty lambda
leadingContent: @Composable () -> Unit = {}

// ✅ GOOD — null means "no slot"; the component can omit space/padding when absent
leadingContent: (@Composable () -> Unit)? = null
```

With a nullable slot, the component can branch on `leadingContent != null` and skip the slot's container, spacing, and padding entirely. With an empty default, the layout often still allocates space for absent content.

## 4. Defaults live in `XxxDefaults`

When you find yourself documenting "the trailing slot should usually be a chevron" or "pass `MaterialTheme.colorScheme.surface` for the default background", co-locate the helpers in a `XxxDefaults` object next to the component:

```kotlin
object SettingsRowDefaults {
    @Composable
    fun Chevron() = Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
    )

    @Composable
    fun TrailingValue(text: String) = Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
```

Call sites stay declarative for the common cases and the slot is still fully open for one-offs:

```kotlin
SettingsRow(
    headlineContent = { Text("Notifications") },
    trailingContent = { SettingsRowDefaults.Chevron() },
    onClick = { … },
)
```

This matches Material 3's `ButtonDefaults`, `TopAppBarDefaults`, etc. — defaults that are themselves composable belong here, not as new component parameters with `MaterialTheme.x.y` defaults expanded inline.

## Quick reference

| Symptom | Diagnosis | Fix |
|---|---|---|
| Caller-controlled `title: String, subtitle: String?, leadingIcon: ImageVector?` on a reusable component | Unconstrained primitive content (§1) | Convert to `xxxContent: (@Composable () -> Unit)?` slots |
| Multiple Boolean flags selecting caller-controlled trailing shapes | Enumerating unconstrained shapes (§1) | One `trailingContent: (@Composable () -> Unit)?` slot |
| A mode parameter enumerating caller-controlled visual variants | Same as flag soup (§1) | Replace the visual variants with a slot |
| Primitive or mode parameter enforces semantic, design-system, constrained-type, or measured fast-path behavior | Deliberate component contract | Keep the primitive parameter and document the invariant |
| `actions: @Composable () -> Unit = {}` inside a `Row` body | Missing scope receiver (§2) | `actions: @Composable RowScope.() -> Unit = {}` |
| `slot: @Composable () -> Unit = {}` for an optional area | Empty-lambda default (§3) | `slot: (@Composable () -> Unit)? = null` and branch on it |
| Component param `defaultColor: Color = MaterialTheme.colorScheme.surface` | Defaults inlined (§4) | Move to `XxxDefaults.color` and reference it |
| Common trailing content repeats at every call site | Missing default helper (§4) | Add `XxxDefaults.Chevron()` etc. |

## When NOT to apply

- **Single-use components.** A composable used in exactly one place, with no plan to reuse, doesn't benefit from slot flexibility — and the slot indirection makes the code harder to read for the one reader. Primitive params + inline content is fine. (As soon as a second call site appears, slot it.)
- **Design-system primitives where every caller must look identical.** A `Heading2(text: String)` exists *because* you want every H2 to look the same; making it `headlineContent: @Composable () -> Unit` invites callers to break the rule. Keep it primitive. (Conversely: if `Heading2` ever needs a badge inline, slot it.)
- **Semantic parameters the component intentionally owns.** If the component owns typography, iconography, accessibility wording, or product consistency, a primitive parameter may be the constraint you want.
- **Constrained-type parameters that genuinely are constrained.** A `Switch(checked: Boolean, onCheckedChange: ...)` doesn't need its checked indicator to be a slot. Booleans-with-callbacks are not "content."
- **Performance-critical fast paths** (rare in app code; common in framework primitives). A slot is an allocated lambda. In the deepest LazyList item layer, sometimes primitives win. If you're not writing the framework, this doesn't apply.

## Red flags during review

| Thought | Reality |
|---|---|
| "Title is *always* a String — making it a slot is over-engineering" | "Always today" is the trap. Material's `ListItem.headlineContent` exists because tomorrow someone wants a `Text + Badge`. The slot is `8` characters of extra wrapping at every call site (`{ Text(…) }`); the refactor to add a slot later edits every existing call site. |
| "Lambdas are heavier than strings" | At the scale of typical Compose UI, this isn't measurable — and the framework's own components (`Button`, `ListItem`, `TopAppBar`, `Scaffold`) all slot. If your component is in the hottest of hot paths, see "When NOT to apply." |
| "I'll add a slot later if someone asks" | The slot turns one parameter into two parameters (the slot itself + maybe an internal flag) and edits every call site. The shape change isn't a "later" change. |
| "I'll model the variants with a sealed `Trailing` type instead" | Sealed enumeration is bounded; slots are unbounded. A sealed type works *until* the day someone needs a variant you didn't anticipate — at which point you're back to editing the component. The slot avoids the cycle. |
| "The leading area is *always* an icon, the trailing area varies — I'll slot only the trailing" | This is the partial-slot trap. The "always-an-icon" assumption breaks the first time a row needs an avatar or a flag emoji or a coloured shape. Slot leading too. |
| "There's only one call site today" | If there's only one call site, you're probably not designing a reusable component yet. See "When NOT to apply" — primitives are fine for a true single-use. The moment you copy-paste it, slot it. |

## Related

- [Modifier and layout](modifier-layout.md) — the modifier-parameter rule (§1–§3 there) travels with slot APIs. A reusable component takes a `modifier` parameter *and* slots its content; the caller owns both placement and what to place.
