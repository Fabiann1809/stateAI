# Contributing

## Setup

Enable the repository git hooks once after cloning:

```sh
git config core.hooksPath .githooks
```

| Hook | What it checks |
|---|---|
| `commit-msg` | Conventional Commit format, no task numbers, no `Co-Authored-By` trailers |
| `pre-commit` | File size limit, ktlint + detekt (Kotlin), ruff (Python) on staged changes |
| `pre-push` | Full `./gradlew check` and `pytest` |

CI runs the same checks on every push to `main` and on pull requests.

## Commits

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<optional-scope>): <description>
```

- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.
- Describe **what changed**, in English, imperative mood, max 72 characters.
- Do not reference task numbers from the spec (`T-1.2`) in the message.
- One commit per finished task or logical change.

Examples: `feat(haptics): add vibration rate limiter`, `fix(sensors): clamp negative heart rate samples`.

## Branches

Work goes directly to `main`. Use a branch and a pull request only for large or risky changes (e.g. restructuring modules), named `feat/<short-name>`, `fix/<short-name>`, `refactor/<short-name>`.

## Code rules

- Code, folder names, comments and documentation in **English**. Only user-facing UI text is in **Spanish**, always through string resources, never hard-coded.
- Single responsibility: one reason to change per class and per file.
- Source files: max **300 lines**. Functions: max **40 lines**. Lines: max **120 characters**.
- No magic numbers: thresholds, weights and durations are named, configurable constants.
- `:core-domain` is pure Kotlin (no Android imports) and every behavior in it has unit tests.
- Depend on interfaces across modules; implementations are injected.
- Keep product language honest (see `docs/SPEC.md`, section 12).
