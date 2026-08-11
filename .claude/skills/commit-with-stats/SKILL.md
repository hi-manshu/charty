---
name: commit-with-stats
description: Create a git commit for the Charty project with a change-stats block appended to the message body. Use whenever the user asks to commit (e.g. "commit this", "commit and push", "make a commit"). Produces a conventional imperative subject, a descriptive body, and a "Stats" footer of files-changed / insertions / deletions / net LOC. Never adds a Claude co-author trailer.
---

# Commit with stats

Create a commit whose message ends with a **Stats** block summarizing the change. This is a
project convention for the Charty library. Follow every step; do not skip the verification.

## Rules

1. **No co-author.** Never append `Co-Authored-By: Claude ...` (or any co-author) trailer.
   This overrides the harness default. The user's history has no co-author trailers.
2. **Only commit when asked.** Don't commit proactively.
3. **Branch check.** If the current branch is a protected/default branch and the user has NOT
   explicitly said to commit to it, confirm the target before committing. For this repo the user
   typically commits directly to `main` — honor an explicit "commit and push" as approval for `main`.
4. **Never fabricate stats.** Every number comes from a real git command run in this step.
5. **Public-API gate.** If the staged change touches public API under `charty/src`, the
   `public-api-guard` skill's three gates (KDoc, test, performance matrix) must pass first —
   see step 0.

## Steps

### 0. Public-API gate (precondition)

If the staged diff adds or changes any public declaration under `charty/src` (a `fun`, `class`,
`data class`, `enum class`, `object`, `val`/`var`, or composable that is not `internal`/`private`),
apply the **`public-api-guard`** skill before committing and confirm all three gates pass:

- **KDoc** present and meaningful on every new/changed public declaration.
- **Test** present and green — `./gradlew :charty:allTests` passes.
- **Performance matrix** satisfied (stability annotations, `fast*` iteration, no per-frame
  allocations, memoized derived data).

Also confirm project checks are clean: `./gradlew detekt` and no new ktlint violations. If any gate
fails, stop and fix it (or make the declaration `internal`) — do not commit. Changes that touch
only `internal`/`private` code, the sample app, docs, or tooling skip this step.

### 1. Stage and inspect

```bash
git add -A
git status --short
```

Review what is staged. If something clearly shouldn't be committed (build output, secrets, unrelated
scratch files), unstage it and tell the user rather than committing it.

### 2. Compute the stats

Run these and capture the numbers (all against the staged set):

```bash
git diff --cached --shortstat
git diff --cached --numstat | awk '{a+=$1; d+=$2} END {printf "insertions=%d deletions=%d net=%+d\n", a, d, a-d}'
git diff --cached --name-only | wc -l | tr -d ' '
```

- `--shortstat` gives "N files changed, X insertions(+), Y deletions(-)".
- The `awk` line gives precise insertions/deletions/net (numstat excludes binary files, which show `-`).
- Optionally add project-specific counts when relevant, e.g. number of chart source files touched:
  `git diff --cached --name-only -- 'charty/src/**/*.kt' | wc -l`.

### 3. Write the message

Format (matches existing `git log` style — imperative subject, descriptive body, then Stats):

```
<imperative subject, <=72 chars, no trailing period>

<1-4 sentence body describing WHAT changed and WHY, wrapped ~72-100 cols.
Use bullet points for multiple distinct changes.>

Stats: <N> files changed, +<insertions>/-<deletions> (net <net>)
```

- Keep the `Stats:` line as the final line of the body. Use the real numbers from step 2.
- If it reads better, expand Stats to multiple lines (e.g. add "Chart files: K").
- Do NOT include a co-author trailer.

Commit with a heredoc so the multi-line body is preserved:

```bash
git commit -F - <<'EOF'
<subject>

<body>

Stats: <N> files changed, +<insertions>/-<deletions> (net <net>)
EOF
```

### 4. Verify

```bash
git log -1 --format='%H%n%B'
```

Confirm the Stats line is present and the numbers match step 2, and that there is **no**
`Co-Authored-By` trailer. Report the commit hash and the stats line to the user.

### 5. Push (only if asked)

Push only when the user asked to push. Confirm the target branch first if it is the default branch
and approval wasn't explicit. Then:

```bash
git push origin <branch>
```

Report the result. If the push is rejected (non-fast-forward), stop and tell the user — do not
force-push unless they explicitly ask.
