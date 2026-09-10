# Changelog — 2026-09-04

Built this repository from an empty fork into a working universal plugin template: the agent
instruction system, the Gradle multi-project build, the shared contract, the Bukkit server
plugin, and the mod side.

## Added

- `AGENTS.md`, `.claude/CLAUDE.md`, and the `.agents/` tree — six indexes, the repository
  rules hub, the agent repository map, and the memory tree. The repository consumes the
  shared instruction set over the `lxagents-agents-base` connector and declares no
  overrides.
- The Gradle skeleton: `settings.gradle` with plugin versions in `pluginManagement`, a root
  `build.gradle` applying one Java 21 toolchain, `gradle.properties`, a committed Gradle
  9.5.0 wrapper, and `gradle/gradle-daemon-jvm.properties` pinning the daemon.
- `api/` — the shared contract: `TemplateAction`, `TemplateRequest`, `TemplateResponse`,
  `TemplateService`, `AbstractTemplateService`. No dependencies at all.
- `common/` — `DefaultTemplateService` and, at the root of the namespace, `TemplateProvider`:
  the single entry point every platform and every third-party consumer goes through.
- `platforms/bukkit/` — `core` with the shared bootstrap and the scheduler abstraction,
  entry points for SpigotMC, PaperMC and Folia, and the `engine` module that shades all
  three into one universal jar with a single `plugin.yml`.
- `platforms/mods/` — `core` with the channel identifiers and the shared payload codec, plus
  client and server modules for Fabric, NeoForge and Forge.
- `PROMPT.md` — the one-time fork setup procedure. It asks what the project should be,
  rewrites the build, the Java tree and the documentation, and then deletes itself.
- `wiki/` — project overview, architecture, and local setup.

## Changed

- Retargeted from Minecraft 26.1.2 to **1.21.11**. Mojang stopped publishing obfuscation
  mappings with the 26.x line, so no mod toolchain can be set up for it; 1.21.11 is the most
  recent release that has them, and both halves target it so the client mod and the server
  plugin can actually talk to each other.
- Reduced `gradle.properties` to `git-org-name` and `git-repository-name`, deriving the
  Maven group from the first. The two names that also appear in Java source — the package
  segment and the plugin id — moved to the root `build.gradle`, so a rename is one guided
  pass rather than a properties edit plus an unguided source sweep.
- Renamed the namespace to `io.github.mcengine.universal`.
- Switched the log structure from `wiki/logs/{Major}/{Minor}/{Patch}/` to
  `wiki/logs/{yyyy}/{mm}/{dd}/`. This repository's version is fixed at `0.0.0`, so a version
  directory encoded a claim it could never make.

## Known limitations

- **The Forge modules do not build.** They sit behind `-Pforge=true`, and
  `./gradlew -Pmods=true build` is green without them. ModDevGradle's legacy Forge mode
  drives NeoFormRuntime, whose own downloader fetches the `universal-srg` artifact outside
  Gradle's dependency resolution, so declaring the Forge maven does not reach it. The
  diagnosis and what to try are in `wiki/environments/setup.md`.
