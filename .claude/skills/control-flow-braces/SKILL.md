---
name: control-flow-braces
description: Brace style for the Charty codebase — use when writing or editing any Kotlin in charty/ or composeApp/, and before committing. Every `if`/`else if`/`else`/`for`/`while` body must be wrapped in braces `{ }`, even a single statement on one line. Braceless one-liners like `if (width > maxWidth) maxWidth = width` are not allowed; write `if (width > maxWidth) { maxWidth = width }`.
---

# Control-flow braces

Charty always braces the bodies of control-flow statements. A single guarded statement is easy to
misread or to break when a second line is added later, so we never write the braceless form.

## Rule

**Every `if`, `else if`, `else`, `for`, and `while` body is wrapped in `{ }`** — including a body
that is a single statement, and including the one-line form.

Banned (braceless body):

```kotlin
if (width > maxWidth) maxWidth = width
if (range == 0f) return bottom
for (point in points) draw(point)
if (a) b() else c()
```

Required (braced body):

```kotlin
if (width > maxWidth) {
    maxWidth = width
}
if (range == 0f) {
    return bottom
}
for (point in points) {
    draw(point)
}
if (a) {
    b()
} else {
    c()
}
```

## Not covered by this rule

- **`when` branches** — a single-expression branch stays as `arm -> expression`; do not add braces.
- **`if`/`when` used as an expression** that returns a value (e.g. `val x = if (a) 1 else 2`, or a
  `when` block assigned to a `val`) — leave the expression form as-is; this rule is about
  *statement* bodies that perform an action.
- **Elvis / single-expression functions** (`fun f() = ...`, `x ?: return`) — not control-flow
  bodies, not affected.

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
