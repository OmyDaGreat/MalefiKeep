# Copilot Instructions for MalefiKeep

## Project Overview

MalefiKeep is a Google Keep-style notes app with multi-user workspace support, built with [Kobweb](https://kobweb.varabyte.com/) — a Kotlin full-stack framework (Compose for Web + Silk on the frontend, Ktor-backed API on the server). Notes are stored in PostgreSQL; authentication uses JWT.

## Build & Dev Commands

All Gradle commands run from the repo root. The app lives in the `:site` subproject.

```bash
# Start dev server at http://localhost:8080 (requires a running PostgreSQL — see Docker section)
./gradlew :site:kobwebStart

# Export for production (output: site/.kobweb/)
./gradlew :site:kobwebExport

# Docker — build and start everything (app + PostgreSQL)
docker compose up --build

# Docker — start pre-built
docker compose up
```

There are no tests in this project.

## Architecture

### Directory Structure

```
site/src/
├── commonMain/kotlin/xyz/malefic/malefikeep/models/
│   ├── Note.kt           # @Serializable — shared between JS and JVM
│   ├── Workspace.kt      # Workspace, WorkspaceMember, WorkspaceRole enum
│   └── ApiModels.kt      # All request/response DTOs
│
├── jsMain/kotlin/xyz/malefic/malefikeep/
│   ├── AppEntry.kt       # @App entry, global StyleSheet, shared CssStyle definitions
│   ├── api/
│   │   └── ApiClient.kt  # window.fetch wrappers: apiGet/apiPost/apiPut/apiDelete
│   ├── pages/
│   │   ├── Index.kt      # "/" — workspace list (redirects to /login if unauthed)
│   │   ├── Login.kt      # "/login"
│   │   ├── Register.kt   # "/register"
│   │   └── Workspace.kt  # "/workspace?id=<id>" — notes for one workspace
│   └── components/       # Header, CreateNote, NoteItem, NotesGrid,
│                         # WorkspaceList, WorkspaceItem, MemberManager, FormInput
│
└── jvmMain/kotlin/xyz/malefic/malefikeep/
    ├── db/
    │   ├── Tables.kt         # Exposed Table objects: Users, Workspaces, WorkspaceMembers, Notes
    │   └── DatabaseManager.kt# HikariCP pool, Exposed connect, schema migration on init
    ├── auth/
    │   ├── JwtUtils.kt       # java-jwt: generate/verify tokens (reads JWT_SECRET env)
    │   └── PasswordUtils.kt  # jbcrypt: hash/verify passwords (12 rounds)
    └── api/
        ├── ApiHelpers.kt     # requireAuth(), respondJson(), respondError(), apiJson
        ├── Init.kt           # @InitApi — calls DatabaseManager.init() on server start
        ├── auth/Login.kt     # POST /api/auth/login
        ├── auth/Register.kt  # POST /api/auth/register
        ├── workspaces/List.kt      # GET  /api/workspaces/list
        ├── workspaces/Create.kt    # POST /api/workspaces/create
        ├── workspaces/Delete.kt    # DELETE /api/workspaces/delete?id=
        ├── workspaces/members/List.kt   # GET /api/workspaces/members/list?workspaceId=
        ├── workspaces/members/Add.kt    # POST /api/workspaces/members/add
        ├── workspaces/members/Update.kt # PUT /api/workspaces/members/update
        ├── workspaces/members/Remove.kt # DELETE /api/workspaces/members/remove?workspaceId=&userId=
        ├── workspaces/notes/List.kt     # GET /api/workspaces/notes/list?workspaceId=
        ├── workspaces/notes/Create.kt   # POST /api/workspaces/notes/create
        ├── workspaces/notes/Update.kt   # PUT /api/workspaces/notes/update
        └── workspaces/notes/Delete.kt   # DELETE /api/workspaces/notes/delete?id=&workspaceId=
```

### Data Model

```
Users           id(UUID), username(unique), email(unique), passwordHash, createdAt
Workspaces      id(UUID), name, ownerId→Users, createdAt
WorkspaceMembers  workspaceId→Workspaces, userId→Users, role("READ_ONLY"|"READ_WRITE")
Notes           id(UUID), workspaceId→Workspaces, title, content, color, createdAt, updatedAt
```

### Auth Flow

1. Client POSTs credentials → server returns a signed JWT
2. JWT stored in `localStorage` under `auth-token` (plus `auth-user-id`, `auth-username`)
3. Every API call includes `Authorization: Bearer <token>` via `ApiClient.kt`
4. Server calls `requireAuth()` (in `ApiHelpers.kt`) to extract `userId` from the token

### Workspace Permission Model

- Workspace **owner** has full read/write/delete/manage rights
- Invited **members** have either `READ_ONLY` or `READ_WRITE`
- Read-only members can view notes but not create/edit/delete them
- Only the owner can add/remove/update members and delete the workspace

### Kobweb API Route Conventions

- Route path is derived from the file path under the `api` package (e.g., `api/workspaces/notes/Create.kt` → `/api/workspaces/notes/create`)
- Each route file has exactly one `@Api`-annotated `suspend fun ApiContext.functionName()` that handles all relevant HTTP methods via a `when(req.method)` check
- `req.params` = query parameters (`Map<String, String>`)
- `req.body.decodeToString()` = request body
- Database work runs in Exposed `transaction { }` blocks (synchronous)

### Environment Variables

| Variable       | Description                           | Default (dev only)        |
|----------------|---------------------------------------|---------------------------|
| `DATABASE_URL` | JDBC URL for PostgreSQL               | `jdbc:postgresql://localhost:5432/malefikeep` |
| `DB_USER`      | Database username                     | `postgres`                |
| `DB_PASSWORD`  | Database password                     | `postgres`                |
| `JWT_SECRET`   | HMAC-256 signing secret (change this!)| dev-secret-please-change  |

## Key Conventions

### Kotlin/Kobweb
- **No star imports** — enforced by `.editorconfig` (`ij_kotlin_name_count_to_use_star_import = 2147483647`)
- Composable function names follow the Kotlin naming convention exception: `ktlint_function_naming_ignore_when_annotated_with = Composable`
- Use `kotlin.code.style=official` (set in `gradle.properties`)
- Gradle configuration cache and build cache are both enabled

### Dependency Management
All versions are in `gradle/libs.versions.toml`. Reference via `libs.*` aliases — never hardcode version strings in `build.gradle.kts`.

### Commit Messages
Conventional Commits enforced by `kommit`. Valid types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`, `update`. Defined scopes: `index`, `github`, `gradle`, `kommit` (custom scopes allowed).

### CI / Deployment
The GitHub Actions workflow (`.github/workflows/export-and-deploy.yml`) deploys a **static** build to GitHub Pages (no server-side API). For the full-stack multi-user app, use Docker instead.

