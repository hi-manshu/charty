---
name: control-flow-braces
description: Brace style for the Charty codebase — use when writing or editing any Kotlin in charty/ or composeApp/, and before committing. Every `if`/`else if`/`else`/`for`/`while` body must be wrapped in braces `{ }`, even a single statement, a one-liner, or an `if`/`else` used as a value expression. Braceless forms like `if (w > m) m = w` or `val x = if (a) 1 else 2` are not allowed.
---

# Control-flow braces

Charty always braces the bodies of control-flow statements **and** the branches of `if`/`else` used
as a value expression. A single guarded statement or a terse ternary-style `if` is easy to misread or
to break when a second line is added later, so we never write the braceless form.

## Rule

**Every `if`, `else if`, `else`, `for`, and `while` body is wrapped in `{ }`** — including a body
that is a single statement, the one-line form, **and an `if`/`else` whose value is assigned,
returned, or passed as an argument.**

Banned (braceless body or branch):

```kotlin
if (width > maxWidth) maxWidth = width
if (range == 0f) return bottom
for (point in points) draw(point)
if (a) b() else c()
val label = if (v == v.toLong().toFloat()) v.toLong().toString() else v.toString()
fun sweep(total: Float): Float = if (total > 0f) value / total else 0f
foo(bar = if (enabled) onClick else null)
```

Required (braced):

```kotlin
if (width > maxWidth) {
    maxWidth = width
}
for (point in points) {
    draw(point)
}
val label =
    if (v == v.toLong().toFloat()) {
        v.toLong().toString()
    } else {
        v.toString()
    }
fun sweep(total: Float): Float =
    if (total > 0f) {
        value / total
    } else {
        0f
    }
foo(
    bar =
        if (enabled) {
            onClick
        } else {
            null
        },
)
```

Inside a string template, brace the branches in place (it cannot span lines):
`"${if (n == 1) { "" } else { "s" }}"`.

## Not covered by this rule

- **`when` branches** — a single-expression branch stays as `arm -> expression`; do not add braces
  (`else ->` is a `when` branch, not an `if`/`else`).
- **Elvis / single-expression functions with no `if`** (`fun f() = expr`, `x ?: return`) — not
  control-flow, not affected. But when the single-expression body **is** an `if`/`else`, brace its
  branches as shown above.

## Applying it

- When you write or edit a control-flow statement, brace its body even if it is one line.
- When editing nearby code, convert any braceless `if`/`for`/`while` you touch to the braced form.
- This is a **commit gate**: a staged diff that adds a braceless control-flow body is not ready.
  `commit-with-stats` checks for it.

Quick self-check on a staged diff (flags added `if (...)`/`for (...)`/`while (...)` lines whose body
is on the same line without an opening brace — review each hit):

```bash
git diff --cached -U0 -- '*.kt' | grep -E '^\+' \
  | grep -E '\b(if|for|while)\s*\(' \
  | grep -vE '\{\s*$' | grep -vE '\)\s*$' \
  | grep -vE '=\s*if|=\s*when|return (if|when)'
```

Every line it prints should be inspected: if it is a braceless statement body, add braces; expression
forms and `when` arms are fine.
