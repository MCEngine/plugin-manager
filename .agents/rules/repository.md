---
name: repository-rules
description: Rules specific to MCEngine/plugin-manager — what it is, what it consumes, its module boundaries, and the version carriers it gates.
---

# Repository-specific rules — plugin-manager

This repository is **`MCEngine/plugin-manager`**: MCPluginManager, a plugin that installs,
updates and removes other plugins on a Minecraft server by talking to a central catalogue and
control server. Read [`../wiki/context/repository-map.md`](../wiki/context/repository-map.md)
for what currently lives where before making changes.

## Mode and the shared set

This repository is a **Mode B consumer**. The shared instruction set — branching, commits,
pull requests, the task workflow, the creators, the placement and versioning rules — is
served by the **`lxagents-agents-base`** MCP connector and is never copied here. This
repository carries only what is its own: its indexes, this file, its two wiki trees, and its
memory.

## Rules

* **Nothing shared is copied here.** A file readable from `agents://` must not exist in
  `.agents/` unless it is a declared override with a row in
  [`../index/root-index.md`](../index/root-index.md). There are currently no overrides.
* **Nothing spells the identity out twice.** `gradle.properties` carries `git-org-name` and
  `git-repository-name`; the root `build.gradle` carries `namespaceSegment`, `pluginIdValue`
  and `commandAliasValue`, and exposes `namespace`, `pluginId`, `commandName` and
  `commandAlias` to every module. Anything you add that names the project reads one of those.
  See [`../memory/decisions/namespace-and-plugin-id.md`](../memory/decisions/namespace-and-plugin-id.md).
* **This repository is one of three.** `MCEngine/server-expressjs` is the central server and
  `MCEngine/client-reactjs` is the web panel. This plugin is an HTTP client of the server and
  owns none of its contract: read the contract documentation there rather than inferring a
  payload shape from a call site.
* **Never trust a downloaded jar.** Verify its checksum against what the server declared
  before it is written anywhere the server will load from, and refuse a file name containing
  a path separator. A plugin that installs code is only as safe as that check.
* **Never block the server thread on the network or on disk.** Every HTTP call and every file
  write goes through the scheduler abstraction in `platforms/bukkit/core/.../scheduler/`,
  which is what keeps Folia correct: `AsyncScheduler` for the work,
  `GlobalRegionScheduler` to fold the result back.
* **Bukkit cannot safely unload a plugin.** An update is staged into `plugins/update/` for the
  server to apply on restart, and a removal is marked and performed at shutdown. Do not
  reach for a classloader trick; there is no supported one.
* **Module boundaries.** `api` depends on nothing and holds only interfaces, records, enums,
  and abstract classes. `common` holds the implementation. Platform modules reach the
  implementation only through `io.github.mcengine.pluginmanager.MCPluginManagerProvider` and
  never import from `...pluginmanager.common`. See
  [`../../wiki/information/architecture.md`](../../wiki/information/architecture.md).
* **One package-info per shared module.** `api` and `common` each carry exactly one, at the
  module's root package. Do not add more, and do not create sub-packages that would want one.
* **Docs and indexes.** Keep both wiki trees current with any structural change, and update
  the index that owns the changed scope in the same commit. See
  `{shared}/creators/index-creator.md` and `{shared}/rules/change-propagation.md`.

## Build and test commands

| Command | Purpose |
|---|---|
| `./gradlew build` | Compile every module and run the test suite. |
| `./gradlew -Pmods=true build` | The same, including the four Fabric and NeoForge mod modules. |
| `./gradlew -Pmods=true -Pforge=true build` | Adds the two Forge modules, which do not currently build. |
| `./gradlew clean` | Remove build output, including the root `build/` directory. |

The Gradle wrapper is committed; never invoke a system-installed `gradle`. Full setup notes
are in [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md).

**A hung `-Pmods=true` build is a known defect with a known fix** — see
[`../memory/decisions/mods-build-parallelism.md`](../memory/decisions/mods-build-parallelism.md).
It is not Vineflower being slow, and the two look identical from the log alone.

**What "verify" means here.** The shared task workflow says to finish and verify each task
before starting the next. In this repository that means the Gradle build and the tests pass
for the modules you touched — `./gradlew build`, plus `-Pmods=true` when the change reaches
`platforms/mods/`.

## Version carriers in this repository

`{shared}/rules/versioning.md` gates every one of these; this table says where they are.

| Carrier | Where |
|---|---|
| `project-version` | `gradle.properties` |
| Gradle wrapper version | `gradle/wrapper/gradle-wrapper.properties` |
| Gradle daemon JVM | `gradle/gradle-daemon-jvm.properties` (generated by `updateDaemonJvm`) |
| Minecraft, loader and plugin dependency versions | `gradle.properties` |
| Log directories | `wiki/logs/{Major}/{Minor}/{Patch}/` |
| Git tags and release drafts | GitHub releases |
