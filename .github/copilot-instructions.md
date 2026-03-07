# Copilot Instructions for MalefiKeep

## Project Overview

MalefiKeep is a Google Keep-style notes app built with [Kobweb](https://kobweb.varabyte.com/) — a Kotlin/JS framework built on Compose for Web and Silk. It is a **static single-page application** with no server-side API routes. Notes are persisted exclusively in `localStorage`.

## Build & Dev Commands

All commands run from the repo root. The app lives in the `:site` Gradle subproject.

```bash
# Start dev server at http://localhost:8080
./gradlew :site:kobwebStart

# Export static production build → site/.kobweb/site/
./gradlew :site:kobwebExport

# Export via the kobweb CLI (used in CI)
cd site && ../kobweb-<version>/bin/kobweb export --layout static --notty
```

There are no tests in this project.

## Architecture

```
site/src/jsMain/kotlin/xyz/malefic/malefikeep/
├── AppEntry.kt          # @App entry point, global StyleSheet, shared CssStyle definitions
├── pages/
│   └── Index.kt         # @Page composable → maps to "/" route, owns notes state
├── components/
│   ├── CreateNote.kt    # Expanded/collapsed note creation form with color picker
│   ├── Header.kt        # App bar with icon and title
│   ├── NoteItem.kt      # Single note card with hover-reveal delete
│   └── NotesGrid.kt     # Responsive CSS grid of NoteItem cards
└── models/
    └── Note.kt          # @Serializable data class (id, title, content, color, createdAt)
```

**Data flow:** `HomePage` (Index.kt) owns the `notes: List<Note>` state via `remember`/`mutableStateOf`. A `LaunchedEffect(notes)` syncs to `localStorage` on every change using `kotlinx-serialization`. The `localStorage` key is `"kobweb-notes"`. Note IDs are random 6-digit integers.

**Routing:** Kobweb uses file-based routing. Any `@Page`-annotated composable under `pages/` automatically becomes a URL route.

**Styling:** Two mechanisms are used together:
- `CssStyle` blocks (defined in `AppEntry.kt` as `NotesStyles`) for reusable styles with pseudo-class variants (e.g., hover). Apply with `.then(NotesStyles.NoteCard.toModifier())`.
- Inline `Modifier` chains on composables for one-off layout/style.
- Global CSS resets live in the `Style : StyleSheet()` object in `AppEntry.kt`.

**Responsive layout:** `NotesGrid` uses `rememberBreakpoint()` from Silk to switch between a 1-column layout (`SM`/`MD`) and an auto-fill grid of 300px-minimum columns at larger breakpoints.

## Key Conventions

### Kotlin/Kobweb
- **No star imports** — enforced by `.editorconfig` (`ij_kotlin_name_count_to_use_star_import = 2147483647`).
- Composable function names may use noun/capitalized style without being flagged — `ktlint_function_naming_ignore_when_annotated_with = Composable` is set.
- Use `kotlin.code.style=official` (set in `gradle.properties`).
- Gradle configuration cache and build cache are both enabled; avoid tasks that break cache compatibility.

### Commit Messages
Commits follow **Conventional Commits** (enforced by `kommit`). Valid types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`, `update`. Defined scopes: `index`, `github`, `gradle`, `kommit` (custom scopes also allowed).

### Dependency Management
All versions are managed in `gradle/libs.versions.toml`. Reference libraries and plugins via `libs.*` aliases; do not hardcode version strings in `build.gradle.kts` files.

### CI / Deployment
The GitHub Actions workflow (`.github/workflows/export-and-deploy.yml`) runs on push to `main`, exports the static site, and deploys to GitHub Pages. The exported output is `site/.kobweb/site/`.
