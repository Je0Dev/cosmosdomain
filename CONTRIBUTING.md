# Contributing & Git workflow

## Workflow rule: one feature at a time

- Implement **one feature at a time**, finish it before starting the next.
- **Test first**: write the unit test, see it fail, then implement until green.
- Verify each feature (build clean, `mvn test` green, app boots) before moving on.
- Commit each feature on its own.

## Branching model

- `main` is the protected default branch. **Never commit directly to it.**
- Every feature or fix gets its own branch created from an up-to-date `main`:

```bash
git switch main
git pull
git switch -c feat/your-feature
```

Branch naming:
- `feat/<name>` - new feature
- `fix/<name>` - bug fix
- `test/<name>` - tests only
- `docs/<name>` - documentation only
- `chore/<name>` - housekeeping

## Commit messages

Use Conventional Commits. A commit must summarize exactly one logical change:

```text
feat: add local media streaming endpoint
fix: correct subtitle language parsing
test: cover rating average calculation
docs: add git workflow guide
chore: update dependencies
```

- Subject line: lowercase, imperative, <= 72 chars, no trailing period.
- Body (blank line after subject) explains *why* when it is not obvious.
- Do not bundle unrelated changes in one commit.

## Before merging to main

1. Rebase onto the latest `main` so history stays linear:

```bash
git switch feat/your-feature
git fetch origin
git rebase origin/main
```

2. Build clean and run all tests:

```bash
./mvnw clean compile
./mvnw test
```

3. Review your own diff first:

```bash
git status            # see staged/unstaged state
git diff              # unstaged changes
git diff --staged     # staged changes
git log --oneline -10 # recent history for context
```

4. Merge with a clean, descriptive message:

```bash
git switch main
git pull
git merge --no-ff feat/your-feature -m "feat: your feature summary"
```

5. Update `CHANGELOG.md` for the push (see below), then commit and push.

## Useful git commands

| Task          | Command |
|---------------|---------|
| Diff          | `git diff`, `git diff --staged` |
| Log graph     | `git log --oneline --graph --all` |
| Log a file    | `git log --oneline -- <file>` |
| Show a commit | `git show <sha>` |
| Undo staging  | `git restore --staged <file>` |
| Undo file     | `git restore <file>` |
| Cherry-pick   | `git cherry-pick <sha>` |

## Feature summaries

For any non-trivial feature, add a **summary** in the merge commit body and a
matching `CHANGELOG.md` entry. A good summary answers:

- What problem does it solve?
- What changed (modules/files touched)?
- How was it verified (tests/commands)?

## Changelog per push

- `CHANGELOG.md` follows [Keep a Changelog](https://keepachangelog.com/) format.
- Every push updates the `Unreleased` section with the new feature/fix entries.
- When a release is tagged, move `Unreleased` into a `[x.y.z]` section.
- Versioning follows [SemVer](https://semver.org/).

## Code rules

Read [CONSTRAINTS.md](CONSTRAINTS.md) before writing code. Reviews must not
merge code that violates it (file size, monolithic methods, missing tests on
important features, TODO left).

Note: tests are for **really important features** (auth logic, ratings,
persistence, TMDB client), not for everything implemented. Small glue helpers
and thin wrappers need no tests.