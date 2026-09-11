---
name: memory-state-repository-state
description: What this repository contains right now, what it does not yet, and the next obvious step.
---

# Repository state

Overwritten in place, always current.

## As of the rename to MCPluginManager

`MCEngine/plugin-manager` is a **Mode B consumer** of the shared instruction set served by
the `lxagents-agents-base` connector. It declares no overrides.

**Exists:** `AGENTS.md`, `.claude/CLAUDE.md`, the six indexes under `.agents/index/`,
`.agents/rules/repository.md`, `.agents/wiki/context/repository-map.md`, this memory tree,
`wiki/information/overview.md`, `wiki/information/architecture.md`,
`wiki/environments/setup.md`, `wiki/logs/0/0/0/CHANGELOG.md`, `README.md`, `LICENSE`, the
Gradle build — `settings.gradle`, `build.gradle`, `gradle.properties`, `.gitattributes`,
`.gitignore`, and the committed wrapper pinning Gradle 9.5.0 — the `api/` and `common/`
modules, the five `platforms/bukkit/` modules, and `platforms/mods/`.

**Gone:** `PROMPT.md`. It was the one-time fork setup procedure; it has run and deleted
itself, along with its row in the `AGENTS.md` trigger table, the template's own task record,
and the two decisions that only described being a template.

**Identity:** `gradle.properties` carries `git-org-name=MCEngine` and
`git-repository-name=plugin-manager`; the group `io.github.mcengine` is derived from the
first. The package segment (`pluginmanager`), the plugin id (`MCPluginManager`) and the
command alias (`mcpm`) live at the top of the root `build.gradle`, because the first two also
appear in Java source and the third cannot be derived at all. The namespace is
`io.github.mcengine.pluginmanager`. The version is `0.0.0` — nothing has shipped.

**Verified:** `./gradlew clean build --warning-mode all` green with zero deprecations and 82
tests passing; `./gradlew -Pmods=true build` green, producing five jars in the root
`build/libs/` — `MCPluginManagerEngine`, `MCPluginManagerFabricClient`,
`MCPluginManagerFabricServer`, `MCPluginManagerNeoForgeClient` and
`MCPluginManagerNeoForgeServer`, each at `0.0.0`.

**Written but not building: the two Forge modules.** Behind `-Pforge=true`. NeoFormRuntime
fetches `net.minecraftforge:forge:<version>:universal-srg` outside Gradle's dependency
resolution, so declaring the Forge maven anywhere does not reach it. Not a build-script bug;
do not add more repositories.

## Stack

Gradle 9.5.0 multi-project with the configuration cache on. **One Java 21 toolchain for
every module, mods included**, and the Gradle daemon itself pinned to 21 in
`gradle/gradle-daemon-jvm.properties` because Loom checks the daemon's version rather than
the toolchain's. `com.gradleup.shadow` produces the Bukkit engine jar; fabric-loom and
ModDevGradle produce the mod jars. **Minecraft target 1.21.11** — not a 26.x release, which
publishes no obfuscation mappings and therefore supports no mod toolchain at all. See
[`../decisions/minecraft-target-version.md`](../decisions/minecraft-target-version.md).

**Parallel project execution is off under `-Pmods=true`.** NeoFormRuntime's shared decompile
lock deadlocks otherwise, and it looks exactly like Vineflower being slow. See
[`../decisions/mods-build-parallelism.md`](../decisions/mods-build-parallelism.md).

## What this is not yet

**The mod half does nothing with the manager.** `platforms/mods/` still carries the channel
and the payload codec the template left behind; the manager contract is Bukkit-side only.

There is no signature verification — the plugin checks that a download matches the checksum
the central server declared, which proves it was not altered in transit but not who published
it. That gap is recorded in `MCEngine/server-expressjs` at
`wiki/security/artifact-upload.md` under `Open`, and closing it needs both sides.

`register` prints the server key rather than writing it into `config.yml`, because saving
would drop every comment in the file.

## Next step

Nothing but the release task, which fills the plan's `PR` column and closes the record. The
full ordered plan is in [`../tasks/mcpluginmanager-platform.md`](../tasks/mcpluginmanager-platform.md).
