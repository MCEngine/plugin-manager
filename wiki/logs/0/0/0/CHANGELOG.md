# Changelog — 0.0.0

**2026-09-11** — MCPluginManager: the universal template renamed into a plugin that polls a
central server, compares versions, verifies checksums, and stages installs, updates and
deletions through `plugins/update/`.

Pre-release. This version covers the repository from its initial commit up to the first
release. Nothing has shipped; `0.0.0` has not moved and the first version that ships is the
one that asks.

## Added

- `AGENTS.md`, `.claude/CLAUDE.md`, and the `.agents/` tree — six indexes, the repository
  rules hub, the agent repository map, and the memory tree. The repository consumes the
  shared instruction set over the `lxagents-agents-base` connector and declares no
  overrides.
- The Gradle skeleton: `settings.gradle` with plugin versions in `pluginManagement`, a root
  `build.gradle` applying one Java 21 toolchain, `gradle.properties`, a committed Gradle
  9.5.0 wrapper, and `gradle/gradle-daemon-jvm.properties` pinning the daemon.
- `api/` — the shared contract: `MCPluginManagerAction`, `MCPluginManagerRequest`,
  `MCPluginManagerResponse`, `MCPluginManagerService`, `AbstractMCPluginManagerService`. No
  dependencies at all.
- `common/` — `DefaultMCPluginManagerService` and, at the root of the namespace,
  `MCPluginManagerProvider`: the single entry point every platform and every third-party
  consumer goes through.
- `platforms/bukkit/` — `core` with the shared bootstrap and the scheduler abstraction,
  entry points for SpigotMC, PaperMC and Folia, and the `engine` module that shades all
  three into one universal jar with a single `plugin.yml`.
- `platforms/mods/` — `core` with the channel identifiers and the shared payload codec, plus
  client and server modules for Fabric, NeoForge and Forge.
- `wiki/` — project overview, architecture, and local setup.
- The manager itself: a client for the central server, version comparison that orders
  `1.10.0` above `1.9.0`, checksum verification, a read-only JSON parser and a plugin
  inventory reader — none of which adds a dependency a Bukkit server could conflict with.
- `/mcpluginmanager` (alias `/mcpm`) with `status`, `servers`, `check`, `apply`, `report`,
  `register` and `pending`, behind `mcpluginmanager.admin` and usable from the console.
- A poll loop that reports what is installed, asks what should change, and stages it.

## Changed

- **Renamed from the `universal-template` identity to MCPluginManager.** The namespace moved
  to `io.github.mcengine.pluginmanager`, every `Template*` type became `MCPluginManager*`,
  the command became `/mcpluginmanager` with the alias `/mcpm`, and the mod ids became
  `mcpluginmanager_client` and `mcpluginmanager_server`. The engine jar is
  `MCPluginManagerEngine-0.0.0.jar`.
- Switched the log structure back from dated directories to
  `wiki/logs/{Major}/{Minor}/{Patch}/`, so recording a change is a version claim again and
  is gated on approval.
- Turned parallel project execution off inside the `-Pmods=true` branch of
  `settings.gradle`. NeoFormRuntime guards its shared Minecraft decompile with a lock that
  two concurrently configuring NeoForge modules deadlock on.

## Removed

- `PROMPT.md`, the one-time fork setup procedure, along with its row in the `AGENTS.md`
  trigger table. It has run; a setup prompt left behind is clutter a later reader mistakes
  for instructions.
- The template's own task record and the two decisions that only described being a template.
## Fixed

- **The plugin could not register its command on Folia.** The bootstrap called
  `Bukkit.getScheduler()`, which throws `UnsupportedOperationException` there.
  `PlatformScheduler` gained `runGlobal`, backed by the global region scheduler on Folia and
  by Bukkit's scheduler elsewhere, and nothing shared reaches for Bukkit's directly any more.
