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

### Task 4 — refactor/namespace

Renamed the template identity to MCPluginManager, in one pass, because a half-applied rename
is worse than none: the build still runs and the names disagree.

**The build, in three files.** `gradle.properties` took `git-repository-name=plugin-manager`
and lost the comments describing itself as a template; `project-version` was already `0.0.0`
and did not move. The root `build.gradle` took `namespaceSegment = 'pluginmanager'` and
`pluginIdValue = 'MCPluginManager'`. `settings.gradle` took `rootProject.name`.

**A third identity value now exists: `commandAliasValue = 'mcpm'`.** It sits beside the
other two rather than in four `plugin.yml` files, and is exposed as `commandAlias` next to
`pluginId`, `namespace` and `commandName`. It is the one value that cannot be derived —
`mcpluginmanager` lower cased from the plugin id is correct but tedious to type in chat, and
a short second name has to be chosen rather than computed.

**The Java tree.** Nineteen package directories moved with `git mv` so history follows, and
thirty-three files were renamed: every `Template*` and `AbstractTemplate*` type became
`MCPluginManager*` and `AbstractMCPluginManager*`, tests included. Package and import
statements, javadoc `{@link}` targets and fully qualified names in comments all moved from
`io.github.mcengine.universal` to `io.github.mcengine.pluginmanager`.

**The three literals `processResources` cannot reach**, because they are Java annotations
and constants: `MCPluginManagerChannel.NAMESPACE` is now `"mcpluginmanager"`, and the Forge
and NeoForge `@Mod` ids are `mcpluginmanager_client` and `mcpluginmanager_server`. The
Fabric side reads its ids from the generated descriptor and needed no edit. The SLF4J logger
names went with them.

**`universal` survives in eleven places and all of them are correct.** It describes the
*universal engine jar* — one jar that runs on SpigotMC, PaperMC and Folia — and the Forge
artifact `universal-srg`. Neither is the package segment, so neither was touched.

**Verified.** `./gradlew clean build --warning-mode all` is green with zero deprecations;
26 tests pass with no failures and no errors; `build/libs/` holds
`MCPluginManagerEngine-0.0.0.jar` carrying exactly one `plugin.yml` and one `config.yml`;
and that descriptor reads `name: MCPluginManager`,
`main: io.github.mcengine.pluginmanager.bukkit.engine.MCPluginManagerEngine`, command
`mcpluginmanager` with `aliases: [mcpm]`.

**The mod half is verified too**, though it took three runs to get there and only the third
failure was interesting. The first died on a Maven Central `429` — transient rate limiting,
gone on retry. The second hung, which turned out to be a build defect rather than slow work:
recorded in `../decisions/mods-build-parallelism.md`, fixed as part of this task, and
summarised below. The third produced all five jars:
`MCPluginManagerEngine`, `MCPluginManagerFabricClient`, `MCPluginManagerFabricServer`,
`MCPluginManagerNeoForgeClient` and `MCPluginManagerNeoForgeServer`, each at `0.0.0`.

Checked inside those jars rather than inferred: the NeoForge descriptor carries
`modId = "mcpluginmanager_client"` and `displayName = "MCPluginManager Client"`, and the
Fabric server descriptor carries `"id": "mcpluginmanager_server"` with entry point
`io.github.mcengine.pluginmanager.mod.fabric.server.MCPluginManagerFabricServer`. That is
the one thing a rename can break in a mod — a hand-written `@Mod` literal drifting from the
generated `modId` — and the two agree.

**Documentation was deliberately left stale by this task.** `AGENTS.md`, `README.md`,
`wiki/`, `.agents/rules/repository.md`, the repository map and the memory state file all
still describe a template, and `PROMPT.md` is still present. Renaming the code and rewriting
the prose are different kinds of change reviewed in different ways, and the rename diff is
large enough to bury the prose one. Task 5 is the other half and lands directly on top of
this branch; neither is finished without the other.

**Two build defects the verification exposed, fixed here.**
`settings.gradle` now turns parallel project execution off inside the `-Pmods=true` branch,
because NeoFormRuntime guards its shared Minecraft decompile with a lock that two
concurrently configuring NeoForge modules deadlock on. And `.gitignore` now covers `*.hprof`
and the JVM error logs: killing the hung build dropped a 796 MB heap dump in the repository
root, which `git add -A` duly staged. Both are pre-existing and neither has anything to do
with the rename; they are here because this is the task that found them, it already owns
these files, and leaving a known 796 MB landmine in the working tree for a later branch to
step on is not a tidier plan.

Next task depends on: this rename. Every name task 5 writes into the documentation is one
this task made true.

### Task 5 — docs/project-identity

The other half of the rename: every document, index and memory file that still described a
template now describes MCPluginManager, and the file that made it a template is gone.

**`PROMPT.md` deleted**, with its row in the `AGENTS.md` trigger table and the sentence in
the opening paragraph that told a reader to start by running it. That is what the file was
written to do once it had run. `AGENTS.md` now opens on what this repository is — a plugin
that installs, updates and removes other plugins — and names the two repositories it works
with.

**The logs went back to version directories.** `wiki/logs/2026/09/04/` was deleted and
`wiki/logs/0/0/0/CHANGELOG.md` written in its place, covering everything from the initial
commit through the rename. `logs-index.md` was rewritten around the consequence rather than
just the path: creating a log directory is a version claim again, so it is gated on user
approval, which is the exact opposite of the rule this repository carried as a template.

**Memory was pruned, not archived.** The template's own task record and the two decisions
that only described being a template — `prompt-file-at-root.md` and
`template-identity-values.md` — were deleted; `namespace-and-plugin-id.md` already carries
the identity values that are actually true. `minecraft-target-version.md` and
`session-trailer-stripped.md` were kept because both are still true, and the first was
reworded off "the template" and repointed at the decision that replaced the one it linked to.

**Three rules in `repository.md` are new, and none of them is a naming change.** A downloaded
jar is verified against its declared checksum before it is written anywhere the server will
load from; nothing blocks the server thread on the network or on disk; and Bukkit cannot
unload a plugin, so an update is staged into `plugins/update/` and a removal happens at
shutdown. They are here rather than in a later task because they constrain what may be
written, and the tasks they constrain have not been written yet.

**What was deliberately not done.** `AGENTS.md` still carries a *mirrored* trigger table,
while `server-expressjs` and `client-reactjs` carry the newer **declaration** block with a
stamped set version. Migrating it is a re-sync of the instruction set, which
`{shared}/prompts/agents-update.md` is explicit runs on request only — so it is raised as a
discovery finding instead of applied here.

Verified: `./gradlew build` still green; no live reference to `PROMPT.md` or
`universal-template` outside the changelog and the memory entries that describe deleting
them; every relative link in every markdown file resolves; and the sweep for `Template`,
`template` and the old namespace comes back empty across the whole repository.

Next task depends on: nothing in this repository. The next work here — the manager client —
waits on the central server's API contract, which task 6 documents.

### Task 18 — feat/manager-client

The half of the plugin that talks to a central server: the contract types, version comparison,
checksums, a JSON reader, the HTTP client, and the configuration.

**No dependency was added, and that is the whole reason three things are hand-written.** A
Bukkit plugin that shades Gson or Jackson competes with whatever version the server already
has on its classpath, and the failure surfaces as an unrelated plugin breaking. So: the JSON
reader is 250 lines and read-only; the HTTP client is `java.net.http`, which the JDK already
has; and version comparison is twenty lines rather than a semver library.

**`Versions.compare` must agree with the server's `version_norm` or the system misbehaves
quietly** — the server would offer an update this plugin then declines. Both implement the
same rule, and the test asserts the bug first (`"1.9.0".compareTo("1.10.0") > 0` is true)
before asserting the fix.

**Five refusals, each in the type rather than in a handler:**

* `CentralServer` refuses anything but `https`, except on localhost. This plugin sends a
  bearer token and receives code it will install; over `http` both are readable and alterable
  by anything on the path.
* `CentralServer.describe()` exists because a record's `toString()` prints every component,
  and one of them is a credential. Nothing logs a server any other way.
* `DesiredChange` refuses a plugin id containing a separator, `..` or a NUL. A plugin id
  becomes a file name, so it is checked at the boundary rather than at each use.
* `DesiredChange` refuses a non-delete change with no 64-character hex checksum. A download
  with nothing to verify against is not something this plugin will install.
* `DesiredState` raises a poll interval below thirty seconds. The server sets the interval
  because it knows how loaded it is, but a zero from a misconfigured one would make this a
  tight loop against it.

**The download is staged, verified, and only then moved.** A jar that fails its checksum must
never exist at a path anything might load from, not even briefly — so it lands in a temporary
file beside the target, is hashed, and is moved atomically only on a match. The failure path
deletes it, and a test asserts the directory is empty afterwards.

**Redirects are never followed.** `HttpClient.Redirect.NEVER`, with a test that a 302 is an
error. The entire trust model is that this plugin fetches only from a server it was configured
with; a redirect is a request to fetch code from somewhere else.

**An action this version does not understand is skipped, not fatal.** A newer server may
describe work that did not exist when this jar was built, and failing the whole poll over one
unknown entry would mean an old plugin stops managing anything the moment the server gains a
feature.

**`ManagerConfig` skips a bad entry rather than failing startup**, with a logged reason — an
operator with three catalogues and one typo should lose that catalogue, not the plugin. The
commonest case, a fresh config with the placeholder token still in it, is said plainly rather
than reported as a validation error.

Verified: `./gradlew build` green, 44 tests in `common` — the HTTP client tests drive a real
`com.sun.net.httpserver` on loopback rather than a mock, because what is worth testing is the
wire behaviour and a mock would only assert that the code calls the methods it calls.

Next task depends on: `ManagerClient`, `ManagerConfig` and `Versions`, which the commands and
the apply flow are written against.

### Task 19 — feat/manager-commands

The other half of the plugin: reading what is installed, applying what the server asks for,
the command surface, and the poll loop.

**A Folia defect was found and fixed, and it was not this task's to create.**
`AbstractMCPluginManagerPlugin` called `getServer().getScheduler().runTask(...)` to register
its command, and `Bukkit.getScheduler()` throws `UnsupportedOperationException` on Folia —
so the plugin would have failed to register its command on the one platform the whole
scheduler abstraction exists for. `PlatformScheduler` gained `runGlobal`, implemented with
Bukkit's scheduler on Spigot and Paper and with `getGlobalRegionScheduler()` on Folia, and
the bootstrap goes through it. Fixed here because this is the task that put real work on
those threads and therefore the task that found it.

**Nothing loads or unloads a plugin, and the code says why in the place a reader will look.**
Bukkit has no supported `unload`; an install or an update is downloaded into
`plugins/update/`, which the server itself applies on its next start, and a delete is
recorded and performed at shutdown because a loaded jar cannot be deleted on Windows while
the server runs. Every message a person sees says "when the server restarts", because that is
what actually happens.

**A staged jar takes the installed jar's file name, not the plugin's.** `EssentialsX-2.20.1.jar`
declares `name: Essentials`; staging it as `Essentials.jar` would make the server load a
second copy alongside the first rather than replace it. `findInstalledJar` therefore reads
each descriptor rather than guessing from file names, and a test holds that.

**One failure does not stop the rest.** A jar that fails its checksum is reported and skipped,
and the other four plugins on the server still get their updates. Reporting the outcome is
itself best-effort: the work already happened, and failing the apply because the report of it
did not go through would leave the operator without their update *and* the panel disagreeing
with the server.

**`register` prints rather than writes.** Saving means `saveConfig()`, which drops every
comment in `config.yml` — and in this plugin's config the comments are most of what the file
is for. Two values pasted by hand is the smaller cost, and the command says so.

**A correction the tests forced, kept in the code rather than the test.**
`jarNameFor("../../etc/passwd")` produced `.._.._etc_passwd.jar` — safe, since every separator
was already substituted, but a name that reads as a traversal and is hidden on Unix. Leading
dots are now stripped. The assertion that caught it was mine and it was right.

**The command needs `mcpluginmanager.admin`, defaulting to op**, and runs from the console —
an unattended server has no player to run it. Every subcommand that touches the network or
the disk goes through `Schedulers.runAsync`, because the thread a command arrives on is a
region thread on Folia and stalling one stalls a slice of the world.

Verified: `./gradlew build` green, 82 tests, and `-Pmods=true` still green. The engine jar's
descriptor carries the new usage line, the alias and the permission; the produced jars are
all five at `0.0.0`. The end-to-end test drives the whole flow against a real HTTP server and
a real directory: stage an update under the installed jar's name, refuse a tampered one,
continue past a failure, and delete at shutdown.

Next task depends on: nothing. The release is the last task.
