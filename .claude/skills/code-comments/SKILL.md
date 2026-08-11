---
name: code-comments
description: Comment style for the Charty codebase — use when writing or editing any Kotlin in charty/ or composeApp/, and before committing. This codebase is self-documenting: essentially no inline `//` comments. Do NOT add comments that restate what the code, a name, or a test already says (e.g. `LineChartConfig() // must not throw`, `if (x == 0) return // guard`). Keep KDoc; drop inline noise.
---

# Code comments

Charty is written to read without inline commentary: clear names, small functions, and KDoc on
public declarations. Match that. The bar for adding an inline `//` comment is very high.

## Rule

**Do not add inline `//` comments.** Before writing one, assume it should not exist. The codebase
has almost none, and they read as noise against that style.

**Never** write a comment that restates what the code, a symbol name, or a test name already
conveys. These are all banned:

```kotlin
LineChartConfig()               // must not throw          ← the test name already says this
if (range == 0f) return          // guard against zero range ← the code already says this
val brush = Brush.linearGradient(color.value) // create brush ← restates the call
values.filter { it > 0f }        // keep positives           ← restates the call
// regression: ...                                          ← put context in the test name instead
```

Instead, encode the intent where it belongs:
- **What / behaviour** → a good function or variable name, or a descriptive `@Test` name
  (`fromMinValue_negativeBar_growsUpward`).
- **Non-obvious rationale (a real "why")** → the function's **KDoc**, not an inline comment. If a
  guard or fallback exists for a subtle reason (overflow, a Compose phase, a platform quirk), state
  it in the KDoc of the function that contains it.

## Applying it

- When you catch yourself about to write `//`, first try to rename something or add/extend KDoc.
- When editing existing code, remove inline `//` comments that only restate the code — but keep:
  - KDoc (`/** … */`) and its `@param`/`@property`/`@return` tags,
  - license/copyright headers,
  - comments **inside** KDoc ```kotlin examples (they document the example),
  - `// TODO`/`// FIXME` that track real outstanding work.
- This is a **commit gate**: a diff that adds inline `//` comments restating the code is not ready.
  `commit-with-stats` checks for it.

Quick self-check on a staged diff:
```bash
git diff --cached -U0 -- '*.kt' | grep -E '^\+' | grep -E '//' | grep -vE '/\*\*|^\+\s*\*|https?://|TODO|FIXME'
```
Every line it prints should be justified as a real "why" that could not live in a name or KDoc —
otherwise delete the comment.
