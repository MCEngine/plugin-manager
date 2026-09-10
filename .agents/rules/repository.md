---
name: repository-rules
description: Rules specific to the universal-template repository — what it is, what it consumes, and the version carriers it gates.
---

# Repository-specific rules — universal-template

This repository is **`MCEngine/universal-template`**, a forkable template for a universal
Minecraft plugin. Read [`../wiki/context/repository-map.md`](../wiki/context/repository-map.md)
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
* **This is a template, not a plugin.** Nothing spells out the organization, the package or
  the plugin id twice. `gradle.properties` carries `git-org-name` and `git-repository-name`;
  the root `build.gradle` carries `namespaceSegment` and `pluginIdValue` and exposes
  `namespace`, `pluginId` and `commandName` to every module. Anything you add that names the
  project reads one of those. See
  [`../memory/decisions/template-identity-values.md`](../memory/decisions/template-identity-values.md).
* **The version never changes.** `project-version` stays `0.0.0` permanently — a template has
  nothing to release. A fork sets its own version through `PROMPT.md`.
* **Logs are dated, not versioned.** `wiki/logs/{yyyy}/{mm}/{dd}/CHANGELOG.md`, with `mm` and
  `dd` zero-padded. A version directory would encode a claim this repository can never make.
  **Creating a date directory is not gated** — it asserts only when something happened, not
  what was released — so record the day's work as you land it, and append when a day already
  has an entry. A *fork* releases, and `PROMPT.md` switches it back to version directories.
* **Versions.** Never edit `project-version` on your own initiative — see
  `{shared}/rules/versioning.md`. In this repository the answer is always no: the version is
  fixed at `0.0.0`. The shared rule also gates creating a `{Major}/{Minor}/{Patch}/` log
  directory, because that is itself a version claim; this repository has no such directories,
  which is exactly why its logs are dated instead.
* **Module boundaries.** `api` depends on nothing and holds only interfaces, records, enums,
  and abstract classes. `common` holds the implementation. Platform modules reach the
  implementation only through `io.github.mcengine.universal.TemplateProvider` and
  never import from `...universal.common`. See
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
| `./gradlew -Pmods=true build` | The same, including the six mod modules. |
| `./gradlew clean` | Remove build output, including the root `build/` directory. |

The Gradle wrapper is committed; never invoke a system-installed `gradle`. Full setup notes
are in [`../../wiki/environments/setup.md`](../../wiki/environments/setup.md).

**What "verify" means here.** The shared task workflow says to finish and verify each task
before starting the next. In this repository that means the Gradle build and the tests pass
for the modules you touched — `./gradlew build`, plus `-Pmods=true` when the change reaches
`platforms/mods/`.

## Current state

The instruction, knowledge and memory systems exist, the Gradle skeleton is in place, and
the shared modules plus the entire Bukkit side build and test green — including the
universal `TemplateEngine-0.0.0.jar` in the root `build/libs/`. The mod modules and
`PROMPT.md` do not exist yet — they are the remaining tasks in
[`../memory/tasks/universal-plugin-template.md`](../memory/tasks/universal-plugin-template.md).
This section, and the repository map, are updated by each task that changes what is true.

## Version carriers in this repository

`{shared}/rules/versioning.md` gates every one of these; this table says where they are.

| Carrier | Where |
|---|---|
| `project-version` | `gradle.properties` |
| Gradle wrapper version | `gradle/wrapper/gradle-wrapper.properties` |
| Gradle daemon JVM | `gradle/gradle-daemon-jvm.properties` (generated by `updateDaemonJvm`) |
| Minecraft, loader and plugin dependency versions | `gradle.properties` |
| Log directories | `wiki/logs/{yyyy}/{mm}/{dd}/` — dated, and therefore not a version carrier here |
| Git tags and release drafts | GitHub releases |
