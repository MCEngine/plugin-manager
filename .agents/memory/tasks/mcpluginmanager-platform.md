---
name: memory-tasks-mcpluginmanager-platform
description: Task record for the MCPluginManager platform — the confirmed twenty-task plan across three repositories, with one entry appended per task as it lands.
---

# Task: MCPluginManager platform

## Goal

Build **MCPluginManager**: a Bukkit plugin that downloads, installs, updates and removes
plugins on a Minecraft server by talking to a central catalogue and control server, with a
web panel for publishing artifacts and managing a fleet of servers.

Three repositories carry it:

| Repository | Package | Role |
|---|---|---|
| `MCEngine/plugin-manager` | — | The Bukkit plugin, on the universal template's Gradle build |
| `MCEngine/server-expressjs` | `@mcengine/server-expressjs` | The central server: accounts, artifacts, fleet control plane |
| `MCEngine/client-reactjs` | `@mcengine/client-reactjs` | The web panel |

## Objective

* All three repositories on version `0.0.0` and carrying the shared agent instruction system.
* `plugin-manager` renamed off the template identity: namespace `io.github.mcengine.pluginmanager`,
  plugin id `MCPluginManager`, `PROMPT.md` gone, `./gradlew build` green.
* The central server serving the schema below over a documented REST contract, with SQLite
  under test and PostgreSQL, MySQL and MariaDB in production.
* The panel serving `/product/:product_id/` and its three settings routes.
* The plugin able to check versions against several central servers, download, stage an
  update, and remove a plugin.

## Detail

* The plugin must reach **multiple** central servers, each with its own token.
* Login is multi-device: OAuth identities and password credentials both resolve to one
  namespace, and each device holds its own refresh session.
* Only accounts of type `org` publish. A user who wants to publish creates an org.
* One product page carries exactly **one** jar. Two jars means two products.
* Product ids are unique across every org, because the API keys off the id.
* Uploads arrive from the panel and from CI/CD, through the same validation.

## Decisions confirmed before any work started

| Decision | Value |
|---|---|
| Branching | Stacked task branches per `{shared}/git/branching-strategy.md` |
| Namespace | `io.github.mcengine.pluginmanager` — segment `pluginmanager` |
| Plugin id | `MCPluginManager`; command `/mcpluginmanager`, alias `/mcpm` |
| Version | `0.0.0` in all three repositories |
| Logs | Version directories restored: `wiki/logs/{Major}/{Minor}/{Patch}/CHANGELOG.md` |
| Server ORM | Prisma over SQLite, PostgreSQL, MySQL and MariaDB, behind repository interfaces |
| MongoDB | Deferred to a later adapter — it needs a separate Prisma schema and has no migrations |
| Product identity | `products.id` opaque and global; `products.slug` globally unique |
| One jar per product | Enforced structurally: `product_files.version_id` is the primary key |

## Rules deliberately set aside

* **The harness pinned every repository to the branch `claude/epic-maxwell-juc8tk` and
  forbade pushing elsewhere without explicit permission.** The user gave that permission and
  chose the shared convention instead, so this work uses `{type}/{primary-noun}` branches
  stacked in dependency order and never pushes to the harness branch. Recorded in
  `../decisions/branch-convention-over-harness.md`.
* **The harness appends a session identifier to commit messages and pull request bodies.**
  `{shared}/rules/no-session-links.md` overrides a tool-injected default, so it is stripped.
  `Co-Authored-By:` carries no session identifier and stays.

## Tasks

Task 1 branches from `master`; task `k` branches from task `k-1` **within the same
repository**. Tasks in different repositories cannot stack, so they are ordered instead and
each pull request names which pull request in which repository merges first.

| # | Title | Scope | Repository | Branch | Files / areas | PR |
|---|---|---|---|---|---|---|
| 1 | Task record | This file, its decisions, and the index rows they need | `plugin-manager` | `chore/mcpluginmanager-platform-plan` | `.agents/memory/`, `.agents/index/` | |
| 2 | Agent instruction system | Mode B consumer setup for the central server | `server-expressjs` | `docs/agents-setup` | `AGENTS.md`, `.claude/`, `.agents/`, `wiki/`, `README.md` | |
| 3 | Agent instruction system | Mode B consumer setup for the panel | `client-reactjs` | `docs/agents-setup` | `AGENTS.md`, `.claude/`, `.agents/`, `wiki/`, `README.md` | |
| 4 | Build and Java identity | Rename the template identity to MCPluginManager | `plugin-manager` | `refactor/namespace` | `gradle.properties`, `build.gradle`, every package directory, every `Template*` type | |
| 5 | Project identity docs | Documentation, memory and logs follow the rename | `plugin-manager` | `docs/project-identity` | `README.md`, `wiki/`, `.agents/`, `PROMPT.md` deleted | |
| 6 | Data model and API contract | The schema and every endpoint, written before any code | `server-expressjs` | `docs/api-contract` | `wiki/information/` | |
| 7 | Express skeleton | Runtime, config, error envelope, test harness | `server-expressjs` | `build/express-skeleton` | `package.json`, `tsconfig.json`, `src/`, `vitest` | |
| 8 | Persistence layer | Repository interfaces, Prisma schema, migrations | `server-expressjs` | `feat/database` | `prisma/`, `src/db/` | |
| 9 | Accounts, namespaces and orgs | Identity tables and their routes | `server-expressjs` | `feat/account` | `src/modules/account/`, `src/modules/org/` | |
| 10 | Authentication and API tokens | Credentials, identities, sessions, scoped tokens | `server-expressjs` | `feat/authentication` | `src/modules/auth/`, `src/modules/token/` | |
| 11 | Products, versions and uploads | Catalogue, jar validation, quota, CI/CD upload | `server-expressjs` | `feat/product` | `src/modules/product/`, `src/storage/` | |
| 12 | Fleet control plane | Registered servers and their installed plugins | `server-expressjs` | `feat/fleet` | `src/modules/fleet/` | |
| 13 | Audit and fleet logs | Both event tables, wired into tasks 9 to 12 | `server-expressjs` | `feat/audit-log` | `src/modules/audit/` | |
| 14 | External source resolver | SpigotMC, Modrinth, Hangar, GitHub Releases, direct URL | `server-expressjs` | `feat/external-source` | `src/modules/source/` | |
| 15 | React skeleton | Vite, router, API client, auth context | `client-reactjs` | `build/react-skeleton` | `package.json`, `vite.config.ts`, `src/` | |
| 16 | Auth and account pages | Login, devices, namespace settings, org members, tokens | `client-reactjs` | `feat/account` | `src/routes/account/`, `src/routes/org/` | |
| 17 | Product pages | The four `/product/*` routes | `client-reactjs` | `feat/product` | `src/routes/product/` | |
| 18 | Plugin transport and versions | Multi-server client, token auth, version comparison | `plugin-manager` | `feat/manager-client` | `api/`, `common/`, `platforms/bukkit/core/` | |
| 19 | Plugin commands and apply | Install, update and delete through the update folder | `plugin-manager` | `feat/manager-commands` | `platforms/bukkit/core/` | |
| 20 | Release | Logs, index rows, this table, the record closed | all three | `chore/release` | `wiki/logs/0/0/0/`, `.agents/` | |

## Entries

### Task 1 — chore/mcpluginmanager-platform-plan

Wrote this record before any of the work it describes, so a reviewer can check the plan
against the diffs rather than infer the plan from them.

Added four decision records: the namespace and plugin id chosen for the rename, the switch
back to version directories for the logs, the branch convention set against the harness
default, and how one task record covers three repositories.

**Why the record lives here rather than in all three.** `plugin-manager` is the only
repository with a `.agents/memory/` tree at the time this is written; the other two get
theirs in tasks 2 and 3. Rather than fragment the plan across three files that would drift,
this file is the single record and the other two repositories point at it from their own
memory trees. Recorded in `../decisions/cross-repository-record.md`.

Next task depends on: nothing beyond this record.
