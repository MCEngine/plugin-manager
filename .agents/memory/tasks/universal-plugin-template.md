---
name: memory-tasks-universal-plugin-template
description: Task record for scaffolding the universal Minecraft plugin template — the confirmed eight-task plan, with one entry appended per task as it lands.
---

# Task: Universal plugin template

## Goal

Turn `MCEngine/universal-template` from an empty repository into a forkable **universal
Minecraft plugin template**: one repository carrying both a Bukkit server plugin (SpigotMC,
PaperMC, FoliaMC, plus a single universal engine jar) and standalone mods (Forge, Fabric,
NeoForge, each split into a client and a server jar that talk over a plugin message channel).

## Objective

A fork edits three values in `gradle.properties`, follows `PROMPT.md`, and has a buildable
multi-platform plugin. Concretely: `./gradlew build` is green, and
`TemplateEngine-0.0.0.jar` plus the six mod jars land in the repository root's
`build/libs/` so they can be dragged and dropped onto a server.

## Detail

`MCOriax/mcidentity` is the reference for every convention used here — Gradle 9.5.0, a
Java 25 toolchain via the foojay resolver, `com.gradleup.shadow` per platform module,
`plugin.yml` filtered through `processResources`, and an aggregator module that shades the
three Bukkit platforms into one jar. **It is read-only for this work**: nothing is committed
or pushed to `MCOriax/mcidentity`.

## Decisions confirmed before any work started

| Decision | Value |
|---|---|
| Branching | Stacked task branches per `{shared}/git/branching-strategy.md` |
| Namespace | `orgname=mcengine`, `reponame=universal` → `io.github.mcengine.universal` |
| Plugin id | `Template` |
| Initial version | `0.0.0` |
| Central facade | `io.github.mcengine.universal.TemplateProvider`, at the namespace root |
| Mod builds | Opt-in, `-Pmods=true` |
| Root `build/libs/` | The engine jar and the six mod jars only |
| Minecraft target | 26.1.2, matching the reference repository |

## Rules deliberately set aside

Both are explicit user instructions, which outrank shared rules by precedence 1. Recorded
in full under `.agents/memory/decisions/`.

* `{shared}/rules/directories.md` restricts the repository root to `AGENTS.md`,
  `README.md` and `LICENSE`. `PROMPT.md` is required at the root by the user.
* The harness default appends a session-identifier trailer to commit messages and a session
  link to pull request bodies. `{shared}/rules/no-session-links.md` overrides tool-injected
  defaults, so both are stripped. `Co-Authored-By:` carries no session identifier and stays.

## Tasks

| # | Title | Scope | Repository | Branch | Files / areas | PR |
|---|---|---|---|---|---|---|
| 1 | Task record | This file and the index rows it needs | `MCEngine/universal-template` | `chore/universal-plugin-template-plan` | `.agents/memory/tasks/`, `.agents/index/` | #1 |
| 2 | Agent instruction system | Mode B consumer setup | `MCEngine/universal-template` | `docs/agents-setup` | `AGENTS.md`, `.agents/`, `wiki/`, `.claude/`, `README.md` | #2 |
| 3 | Gradle foundation | Build skeleton, no source yet | `MCEngine/universal-template` | `build/gradle-foundation` | `settings.gradle`, `build.gradle`, `gradle.properties`, `gradle/`, `.gitignore`, `.gitattributes` | #3 |
| 4 | Shared modules | `api/` and `common/` | `MCEngine/universal-template` | `feat/shared-modules` | `api/`, `common/` | #4 |
| 5 | Bukkit platforms | core, three platforms, engine | `MCEngine/universal-template` | `feat/bukkit-platforms` | `platforms/bukkit/` | #5 |
| 6 | Minecraft target | Retarget the whole template to 1.21.11 | `MCEngine/universal-template` | `build/minecraft-target` | `gradle.properties`, `gradle/`, `build.gradle`, docs | #6 |
| 7 | Mod platforms | core and six loader modules | `MCEngine/universal-template` | `feat/mod-platforms` | `platforms/mods/`, `settings.gradle` | #7 |
| 8 | Identity properties | Simplify the properties, rename the namespace | `MCEngine/universal-template` | `refactor/identity-properties` | `gradle.properties`, `build.gradle`, every package directory | #8 |
| 9 | Fork guide | `PROMPT.md`, wired into `AGENTS.md` | `MCEngine/universal-template` | `docs/template-prompt` | `PROMPT.md`, `AGENTS.md`, descriptors | #9 |
| 10 | Loader verification | Fix NeoForge; gate Forge behind its own flag | `MCEngine/universal-template` | `fix/mod-loader-verification` | `platforms/mods/`, `settings.gradle`, docs | #10 |
| 11 | Dated logs | Date the change log instead of versioning it | `MCEngine/universal-template` | `docs/date-based-logs` | `wiki/logs/`, `.agents/index/logs-index.md`, `AGENTS.md`, `PROMPT.md` | #11 |
| 12 | Close out | Fill this table, mark the record done | `MCEngine/universal-template` | `chore/template-close-out` | this file, `.agents/memory/state/` | #12 |

**The list grew from eight tasks to twelve, twice, and both times for a reason worth
keeping.** At task 6 the mod work uncovered that Minecraft 26.x publishes no obfuscation
mappings, forcing a retarget of the whole template — a different concern from the mod
modules, so it became its own task. Later the user changed where the template's identity
lives, added the interactive `PROMPT.md`, and asked for dated logs; and the NeoForged outage
clearing mid-session made a verification task possible that had not been planned. Each time
the plan was revised and put to the user before anything was written, per the workflow's
rule that a task invalidating a later one stops rather than silently reworking the list.

Task 1 branches from `master`; task `k` branches from task `k-1`. The `PR` column was filled
by task 12, per `{shared}/planning/task-workflow.md` §F — last in the stack, so writing the
numbers there rebased nothing.

Task 7 sits second-to-last on purpose: `PROMPT.md` can only list what a fork must rename
once the files exist, so the `AGENTS.md` trigger row pointing at it lands in the same commit
as the file itself and no intermediate branch carries a dangling link.

### Task 1 — chore/universal-plugin-template-plan

Wrote this record before any of the work it describes, so a reviewer can check the plan
against the diffs rather than infer the plan from them.

Also created `.agents/index/root-index.md` and `.agents/index/memory-index.md`. The index
system is task 2's deliverable, but an index has to exist for this file to be registered in
the same commit that adds it. Task 1 therefore creates the root router with its memory row
and the memory index; task 2 adds the remaining indexes and their rows to the root table.

Next task depends on: nothing beyond this record.

### Task 2 — docs/agents-setup

Adopted the shared instruction set as a **Mode B consumer**, declaring no overrides.

Created `AGENTS.md` (connector bootstrap verbatim, auto-activation contract, trigger table
mirrored row for row from `{shared}/rules/auto-activation.md`, reading order, routing
protocol, iron rule, discovery protocol, version and session-link rules); `.claude/CLAUDE.md`
as an import of `../AGENTS.md` and nothing else; the five remaining indexes under
`.agents/index/`; `.agents/rules/repository.md`; `.agents/wiki/context/repository-map.md`;
the memory state file and three decision records; `wiki/information/overview.md`; and
`wiki/logs/0/0/0/CHANGELOG.md`. Rewrote `README.md` as an overview, moving its detail into
the wiki page rather than deleting it. `LICENSE` was already MIT and was left alone.

**Only the mandatory core set was created.** The `agents-setup` procedure gates optional
local instruction files behind a user selection, and the discovery protocol gates instruction
files generally, so the candidates this project would earn — build and shading rules, a
platform entry-point SOP, the mod channel protocol — are held as findings to present at the
end of the work rather than written unasked. `AGENTS.md` therefore carries no repository-specific
trigger rows yet, and says so where the rows would go.

Next task depends on: nothing. The repository map and `repository.md` both state plainly that
no build exists yet, so task 3 updates them as it makes that false.

### Task 3 — build/gradle-foundation

Added the Gradle skeleton: `gradle.properties`, the root `build.gradle`, `settings.gradle`,
`.gitattributes`, and the committed wrapper pinning Gradle 9.5.0. Extended `.gitignore` with
IDE, OS, Java and mod-toolchain entries. Added `wiki/environments/setup.md` and its index row.

**Plan correction, made before anything was written.** The plan split the toolchain: Java 25
for the shared and Bukkit modules, Java 21 for the mods. Mojang's version manifest gives
Minecraft 26.1.2 a `javaVersion` of 25, so the split was unnecessary and would have broken
`platforms/mods/core` the moment it depended on `api`. One Java 25 toolchain now covers every
module and the `mod-java-version` property was never created.

`settings.gradle` deliberately includes no modules yet: each task adds its own `include`
lines so every commit in the history has a settings file matching the directories on disk.

Verified: `./gradlew --version` reports Gradle 9.5.0; `./gradlew properties` resolves
`group: io.github.mcengine` and `version: 0.0.0`, confirming the group is derived from
`orgname` rather than stored; `./gradlew clean` succeeds and the configuration cache stores
an entry. `./gradlew build` has no `build` task to run yet — the root applies no Java plugin
and there are no subprojects — which is expected until task 4.

Next task depends on: the identity properties in `gradle.properties`, which task 4 reads to
place its packages.

### Task 4 — feat/shared-modules

Added the two shared modules and wired them into `settings.gradle`.

`api` holds the contract and depends on nothing: `TemplateAction` (the wire vocabulary),
`TemplateRequest` and `TemplateResponse` (records; the request normalizes a missing payload
to the empty string so no handler null-checks it), `TemplateService`, and
`AbstractTemplateService`. The dispatch switch has no `default` branch deliberately —
adding an action breaks the build until every platform handles it, rather than failing at
runtime on whichever platform saw it first.

`common` holds `DefaultTemplateService` and, at the **root of the namespace**,
`TemplateProvider` — the single entry point. It holds its service privately and returns it
from no method, so there is no supported way around it. Exactly one `package-info.java` per
module, at each module's root package, as specified.

Added `wiki/information/architecture.md` now that there is real architecture to describe,
and its index row.

Verified: `./gradlew build` succeeds; 11 tests pass across `TemplateProviderTest` and
`TemplateRequestTest` with no failures; `javap` reports bytecode major version 69, which
confirms Gradle provisioned and used JDK 25 through the foojay resolver rather than falling
back to the container's JDK 21. Jars are named `universal-template-api-0.0.0.jar` and
`universal-template-common-0.0.0.jar`.

Not done, deliberately: no `maven-publish` configuration. The reference repository publishes
its shared modules to GitHub Packages, but that was not part of the requested structure, so
it is raised as a discovery finding instead of added unasked.

Next task depends on: `TemplateProvider` and the contract types, which every Bukkit module
compiles against.

### Task 5 — feat/bukkit-platforms

Added the whole Bukkit side: `platforms/bukkit/{core,spigotmc,papermc,foliamc,engine}`.

`core` carries `AbstractTemplatePlugin`, the `PlatformScheduler`/`PlatformTask`/`Schedulers`
abstraction with the Bukkit implementation, the `/template` command, the join listener, and
the single `config.yml`. Each platform module adds core's resources directory as an extra
resource source rather than copying `config.yml` four times.

Each platform entry point is a scheduler choice and nothing else, and a structure test in
each module asserts it declares exactly one method — so platform-specific logic cannot
quietly accumulate in three places. `TemplateEngine` probes for classes at enable time
(`RegionizedServer` → Folia, `PaperConfig` → Paper, else Spigot) and compiles against the
Folia API alone, because Spigot, Paper and Folia all provide the same Gradle capability and
cannot be declared together.

Nothing in the plugin descriptors is hardcoded: `processResources` builds `main:` from
`namespace` and `pluginid`, and the command name from `pluginid` lowercased.
`AbstractTemplatePlugin` reads it back with `getCommand(getName().toLowerCase())`, so
renaming a fork leaves no stale string behind.

**Two build problems found and fixed, both worth recording.**

1. A `plugins {}` block accepts only constant expressions, so
   `id 'com.gradleup.shadow' version providers.gradleProperty(...)` fails to evaluate.
   Plugin versions now live in a `pluginManagement { plugins { } }` block in
   `settings.gradle`, read from `gradle.properties`, and modules declare a bare id. A line
   break between `version` and its argument also breaks Groovy parsing — it must stay on
   one line.
2. `filteringCharset 'UTF-8'`, copied from the reference repository's style, is deprecated
   space-assignment syntax that Gradle 10 removes. Changed to `filteringCharset = 'UTF-8'`
   in all four modules. `./gradlew clean build --warning-mode all` now reports zero
   deprecations, and that is the standard to hold — a template that ships a warning hands
   it to every fork.

Verified: `./gradlew build` green; 20 tests pass across five test classes with no failures;
`build/libs/TemplateEngine-0.0.0.jar` exists at the repository root and contains exactly one
`plugin.yml` and one `config.yml`, the descriptor's `main` correctly expanded to
`io.github.mcengine.universal.bukkit.engine.TemplateEngine`, and every one of the
five `bukkit.*` packages plus `api` and `common`. The three platform jars are named
`TemplateSpigotMC/PaperMC/FoliaMC-0.0.0.jar` and stay in their own module directories, as
intended.

Next task depends on: the contract types in `api`, which the mod modules share with the
Bukkit side over the message channel.


### Task 6 — build/minecraft-target

Retargeted the whole template from Minecraft 26.1.2 to **1.21.11**, on Java 21.

**Why, since this reverses a decision made in task 3.** The mod toolchains cannot build for
26.x at all: Mojang stopped publishing obfuscation mappings with that line — every 26.x
release's version JSON has only `client` and `server` under `downloads`, no
`client_mappings` — and Yarn has no 26.x builds either. Fabric Loom fails at configuration
with "Failed to find official mojang mappings for 26.1.2", and Forge and NeoForge need the
same maps. 1.21.11 is the most recent release that has them, and every platform this
template targets ships for it. Full reasoning and the evidence table are in
[`../decisions/minecraft-target-version.md`](../decisions/minecraft-target-version.md).

Choosing 1.21.11 for both halves rather than only for the mods was the user's call: a
1.21.11 client mod cannot talk to a 26.1.2 server, so a split target would have built seven
jars that could not work together — the opposite of what a universal template is for.

Changed `gradle.properties` (Minecraft version, the three Bukkit coordinates, the three
loader coordinates, a new `fabric-yarn-version`, `java-version` 25 to 21,
`plugin-api-version` 1.20 to 1.21) and regenerated `gradle/gradle-daemon-jvm.properties` at
21. **No Java source changed** — nothing in the source names a version, which is the point
of driving everything from properties.

A second finding worth keeping: **Loom checks the Gradle daemon's Java version, not the
toolchain's.** Its first failure was "Minecraft requires Java 25 but Gradle is using 21"
even with the toolchain set correctly. `gradle/gradle-daemon-jvm.properties`, generated by
`./gradlew updateDaemonJvm --jvm-version=21`, pins it portably with per-OS download URLs, so
a contributor on any JDK gets the right daemon.

Verified: `./gradlew clean build --warning-mode all` green with zero deprecations; 20 tests
pass; `javap` reports bytecode major version 65, confirming Java 21; the engine jar still
carries exactly one `plugin.yml` and one `config.yml`, now with `api-version: '1.21'`.

Next task depends on: the loader coordinates in `gradle.properties`, which the six mod
modules read.

### Task 7 — feat/mod-platforms

Added the mod side: `platforms/mods/core` plus the six loader modules under
`platforms/mods/{forge,fabric,neoforge}/{client,server}`.

`mods/core` holds `TemplateChannel` (two channel ids, request and response) and
`TemplatePayloadCodec` (the byte layout), depending on nothing but `api` and the JDK, so it
stays in the default build and the wire format keeps compiling even with the loaders off.
The action is encoded by name rather than ordinal, so reordering the enum cannot silently
change the meaning of packets already in flight.

The client half of every loader decides nothing: it sends an action and renders the answer,
and the server takes the player's identity from the connection rather than from the payload.
That is a trust boundary, not just a packaging choice.

**Verified:** `./gradlew build` green with 26 tests and zero deprecations;
`./gradlew -Pmods=true :platforms:mods:fabric:client:build :platforms:mods:fabric:server:build`
produces `TemplateFabricClient-0.0.0.jar` and `TemplateFabricServer-0.0.0.jar` in the root
`build/libs/`, each carrying the bundled `api` and `mods/core` classes and a fully expanded
`fabric.mod.json`.

**Not verified: the four Forge and NeoForge modules.** Both loaders build through
ModDevGradle, which resolves `net.neoforged:minecraft-dependencies` from
`maven.neoforged.net/mojang-meta`. That host returned **502 Bad Gateway for every version
tried**, including ones unrelated to this project, for the whole session; the `releases`
repository on the same host answers normally, and the agent proxy reported no relay
failures, so this is an upstream outage rather than a configuration fault. Their code and
build wiring are written and committed, but no compiler has seen them. **Treat them as
unverified until `./gradlew -Pmods=true build` has been run against a working
`mojang-meta`.**

Two build details worth keeping: a `from(configurations.bundled...)` in a `jar` task needs an
explicit `dependsOn configurations.bundled`, or Gradle fails with an implicit-dependency
error; and only the server halves bundle `common`, because only they use `TemplateProvider`.

Next task depends on: nothing. Task 8 renames the namespace across everything added here.

### Task 8 — refactor/identity-properties

Reworked where the template's identity lives, at the user's direction, and renamed the
namespace.

`gradle.properties` now carries only `git-org-name=MCEngine` and
`git-repository-name=universal-template`; `orgname`, `reponame` and `pluginid` are gone. The
Maven group is derived from the owner, lowercased. The two values that also appear in Java
source — the package segment `universal` and the plugin id `Template` — moved to the top of
the root `build.gradle`.

**Why that split, since it reverses task 3.** Three of the old properties were also spelled
out in the source tree: package directories, `package` and `import` statements, and the
`Template*` class names. Gradle cannot rewrite any of that, so a fork had to do a
source-wide rename *and* keep a properties file in step with it — two places with nothing
checking they agree. Putting the two source-facing values in one build file, next to each
other, makes the rename a single guided pass, which is what `PROMPT.md` will perform.

Renamed the package `io.github.mcengine.universaltemplate` to
`io.github.mcengine.universal` across 19 source directories and 47 files, with `git mv` so
the history follows. The repository name and the package segment are deliberately different
things now.

Also recorded that **the version is fixed at `0.0.0` permanently** — a template has nothing
to release — and rewrote the identity decision file, the repository rules, the repository
map, and the three wiki pages that described the old model.

Verified: `./gradlew clean build --warning-mode all` green with zero deprecations and 26
tests; `./gradlew properties` reports `group: io.github.mcengine` and `version: 0.0.0`; the
engine jar's descriptor resolves `main:` to
`io.github.mcengine.universal.bukkit.engine.TemplateEngine`; both Fabric jars rebuild and
contain only `io/github/mcengine/universal/`, with the entrypoint in `fabric.mod.json`
matching. No stale package survived anywhere outside `build/`.

Next task depends on: this layout. `PROMPT.md` rewrites `namespaceSegment`, `pluginIdValue`,
the package directories and the class names in one pass.

### Task 9 — docs/template-prompt

Added `PROMPT.md` at the repository root and wired it into `AGENTS.md`.

It is an **interactive** setup procedure, not a checklist to read. It asks, in one message,
for the plugin id, the starting version, the GitHub organization and repository, the license
type with holder and year, the group and namespace (offering the derived defaults), what the
project actually is for `README.md` and `wiki/`, and whether the fork wants custom rules or
trigger rows in `AGENTS.md`. Only then does it rename anything, because a half-applied
rename still builds and is worse than none.

It then states exactly what changes and where: two build values, the 19 package directories,
the 28 `Template*` types, and the three string literals the build cannot reach — the channel
`NAMESPACE` constant and the two `@Mod` ids, which are Java annotations. It ends by deleting
itself and its own `AGENTS.md` row, in the same commit as the setup work.

**Shrank the manual surface first.** `author` and `issueTrackerURL` were literal `MCEngine`
and a literal GitHub URL in ten descriptor files. Both now derive from `git-org-name` and
`git-repository-name` through `processResources`, so no `plugin.yml`, `fabric.mod.json` or
`.toml` needs editing by a fork at all. That is ten files off the checklist, and ten fewer
places for a fork's name to be half-changed.

`PROMPT.md` also records that this template's "version is fixed at 0.0.0" rule is a
template-only rule and must be removed from a fork, which does release.

Verified: `./gradlew clean build --warning-mode all` green, zero deprecations, 26 tests; the
engine jar's `plugin.yml` shows `author: MCEngine` resolved from the property; no `MCEngine`
literal remains in any `.yml`, `.json` or `.toml`. The root now holds `AGENTS.md`,
`PROMPT.md`, `README.md` and `LICENSE` — `PROMPT.md` being the declared exception to the
shared directory rule, recorded in `.agents/memory/decisions/prompt-file-at-root.md`.

Next task depends on: nothing. Task 10 closes the record.

### Task 10 — fix/mod-loader-verification

`maven.neoforged.net/mojang-meta` came back mid-session, so the Forge and NeoForge modules
task 7 committed unverified could finally be compiled. They did not compile, and the fixes
are worth recording because they are all Minecraft 1.21.11 API changes rather than mistakes
in the build.

**NeoForge: fixed and verified.** Three renames, found by reading the symbols out of the
built Minecraft jar with `javap` rather than guessing:

* `net.minecraft.resources.ResourceLocation` is now `net.minecraft.resources.Identifier` —
  Mojang adopted the Yarn-style name. `fromNamespaceAndPath` is unchanged.
* `Player.sendSystemMessage(Component)` is now `displayClientMessage(Component, boolean)`.
* `PacketDistributor` no longer carries `sendToServer`; the client direction moved to
  `net.neoforged.neoforge.client.network.ClientPacketDistributor`.

Both NeoForge jars now build into the root `build/libs/`.

**Forge: still not building, and it is not a build-script fault.** ModDevGradle's legacy
Forge mode drives NeoFormRuntime, whose own `ArtifactManager` downloads
`net.minecraftforge:forge:1.21.11-61.2.1:universal-srg` **outside Gradle's dependency
resolution**. That artifact is published and returns HTTP 200, but declaring
`maven.minecraftforge.net` in the module, in the root `allprojects` block, and clearing the
NeoFormRuntime artifact cache all left the error unchanged. Adding more repositories is not
the fix and should not be attempted again.

Moved Forge behind its own `-Pforge=true` flag so `./gradlew -Pmods=true build` stays green
rather than shipping a template whose own documented command fails. What to try next is
written down in `wiki/environments/setup.md`: a newer ModDevGradle, ForgeGradle instead of
its legacy mode, or deleting the two modules if the fork does not target Forge — nothing
else depends on them.

Verified: `./gradlew -Pmods=true build` green; 26 tests; five jars in the root `build/libs/`
— the engine plus the Fabric and NeoForge client and server jars.

Next task depends on: nothing.

### Task 11 — docs/date-based-logs

Switched the change log from `wiki/logs/{Major}/{Minor}/{Patch}/` to
`wiki/logs/{yyyy}/{mm}/{dd}/CHANGELOG.md`, at the user's suggestion, and rewrote the entry to
cover the whole of this work rather than only the instruction-system scaffold.

The reasoning is worth keeping: this repository's version is fixed at `0.0.0`, so a version
directory encoded a claim it can never make, while a date encodes the only ordering a
never-versioned repository has. The depth is unchanged at three segments, so the index keeps
its shape.

Two refinements on top of the suggestion:

* `mm` and `dd` are **zero-padded**, so the directories sort correctly as strings. `2026/9/4`
  would sort after `2026/12/1`.
* **A date directory is not a version claim, so creating one is not gated.**
  `{shared}/rules/versioning.md` gates version directories precisely because they assert a
  release; a date asserts only when something happened. Recorded explicitly in
  `logs-index.md` and the repository rules, because an agent that reads the shared rule and
  stops to ask permission before writing a log entry has misread it.

Also updated the version rule in `AGENTS.md`, the version-carrier table, the repository map,
and `PROMPT.md` — where a fork is told to switch **back** to version directories, since a
fork does release, and to drop the ungated note along with them.

Verified: `./gradlew -Pmods=true clean build --warning-mode all` green with zero
deprecations; no `INDEX.md` anywhere; no index outside `.agents/index/`; every `.agents/`
file carries valid frontmatter; every relative link in every markdown file resolves.

Next task depends on: nothing. The close-out fills the pull request column.

### Task 12 — chore/template-close-out

Rewrote the task table to the twelve tasks the work actually took, recorded why it grew from
eight, filled the pull request column, and closed the record.

**Final state.** `./gradlew build` and `./gradlew -Pmods=true build` are both green: 26 tests
across six test classes, zero deprecation warnings under `--warning-mode all`, and five jars
in the repository root's `build/libs/` — `TemplateEngine-0.0.0.jar` plus the Fabric and
NeoForge client and server jars. The instruction system passes every structural check: no
`INDEX.md`, no index outside `.agents/index/`, valid frontmatter throughout, every relative
link resolving, and the `AGENTS.md` trigger table matching the shared set row for row.

**What is not done, and why.** The two Forge modules are written but do not build; the cause
is in NeoFormRuntime rather than in this repository, the diagnosis and next steps are in
`wiki/environments/setup.md`, and they sit behind `-Pforge=true` so nothing else is affected.
No CI and no `maven-publish`; both were unrequested scope and are raised as findings instead.

**Status: done.**
