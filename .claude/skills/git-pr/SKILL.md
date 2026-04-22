---
name: git-pr
description: Safe branch/commit/push/PR workflow for this repo. Use whenever the user wants to commit, push, open a pull request, or walk through the delivery sequence. Also trigger on "ship this", "push and open a PR", "commit these changes", "prépare la PR", "crée une pr", and on ambiguous cues like "let's get this merged" or "wrap up".
---

# Git PR workflow

Keep the sequence disciplined and reversible. The project enforces Google Java Format; there's no CI yet, so mistakes are caught late.

## The sequence

1. **Branch** from `main`: `git checkout -b <type>/<short-desc>` where `<type>` is `feat|fix|chore|refactor|docs`.
2. **Format locally** before the first commit: `mvn git-code-format:format-code`. The cosium plugin also installs a git pre-commit hook that runs `validate-code-format`, so unformatted code is rejected at commit time — formatting first is faster than fighting the hook.
3. **Commit** in imperative mood — one-line subject, optional body. Don't batch unrelated changes.
4. **Build**: `mvn clean install -DskipTests` (no tests yet, but compile + Error Prone + format-validate must pass).
5. **Push**: `git push -u origin <branch>`.
6. **Open PR** via `gh pr create` — title imperative, body explains *why* and what to verify.
7. **Keep PRs small.** A 400-line PR in a small-team project gets rubber-stamped or ignored.

## Guardrails

- **Never `push --force` on `main` / `master`.** On a feature branch it's tolerable after an interactive rebase, but prefer `--force-with-lease`.
- **Never skip hooks** (`--no-verify`, `--no-gpg-sign`) without the user explicitly asking. If a hook fails, fix the underlying issue.
- **Never amend a commit that's already on a shared branch.** Add a new commit; rewriting shared history breaks everyone.
- **Flyway migrations are immutable once applied.** Never edit a migration already run in any environment. Add a new migration; name it per the existing pattern (`V<number>__<description>.sql`).
- **Stage files by name** (`git add gnx-ai-training-api/...`) rather than `git add .` to avoid dragging in `.env`, IDE configs, or half-finished work.

## PR body template

```markdown
## Summary
- <one-sentence what>
- <one-sentence why>

## Changes
- <module>: <what changed>

## How to verify
- Hit `<endpoint>` with `<payload>` → expect `<result>`, or
- `mvn clean install -DskipTests` and check output
```

## By symptom

- **"I need to undo this commit but already pushed"** — `git revert <sha>` + push. Don't rewrite shared history.
- **"CI / format check failed"** — `mvn git-code-format:format-code`, commit, push.
- **"Merge conflict on `pom.xml`"** — resolve by hand; never `git checkout --theirs` / `--ours` blindly on a pom (version drift is silent and nasty).
- **"PR is too big"** — split by module or feature seam (e.g., "new endpoint" and "new migration" as separate PRs).
- **"I committed a secret"** — rotate the secret first, then clean history (`git filter-repo`), then force-push with the team's knowledge. The secret is already public on GitHub mirrors — rotation is the priority.
