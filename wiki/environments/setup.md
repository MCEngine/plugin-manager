# Local Setup

[← Back to README](../../README.md)

## Requirements

| | |
|---|---|
| **JDK** | 21. Minecraft 1.21.11 requires it, so every module targets it. |
| **Gradle** | None needed — use the committed wrapper (`./gradlew`), which pins Gradle 9.5.0. |
| **Disk** | A few hundred MB for the Bukkit side. Several GB more if you enable the mod modules, which decompile Minecraft. |

You do not have to install JDK 21 yourself. `settings.gradle` applies the Foojay resolver,
so Gradle downloads a matching toolchain on first build if one is not already present, and
`gradle/gradle-daemon-jvm.properties` pins the Gradle daemon itself to 21 — which the mod
toolchains require, since Loom checks the daemon's Java version rather than the toolchain's.

## Build

```bash
./gradlew build                 # compile every module and run the tests
./gradlew clean                 # remove build output, including the root build/ directory
```

Never invoke a system-installed `gradle`; the wrapper is committed so that everyone builds
with the same version.

## Building the mods

The Forge, Fabric, and NeoForge modules are excluded from the build unless you ask for them:

```bash
./gradlew -Pmods=true build          # Fabric and NeoForge, client and server
```

They are opt-in because their toolchains download and decompile Minecraft the first time
they run, which takes a long time and a lot of disk. A change confined to the Bukkit side
does not need to pay that cost. Set `mods=true` in `gradle.properties` if you work on the
mods often enough that passing the flag becomes noise.

### Forge needs a second flag, and currently does not build

```bash
./gradlew -Pmods=true -Pforge=true build
```

The two Forge modules are written and complete, but their toolchain does not resolve. Legacy
Forge support in ModDevGradle drives NeoFormRuntime, and NeoFormRuntime's own downloader
fetches `net.minecraftforge:forge:<version>:universal-srg` **outside Gradle's dependency
resolution**. Declaring `maven.minecraftforge.net` in the module, or in the root
`allprojects` block, therefore never reaches it, and the download fails with
`Could not find … universal-srg in any repository` even though that artifact is published
and returns HTTP 200.

They sit behind their own flag so that `-Pmods=true build` stays green rather than shipping
a template whose documented command fails.

If you need Forge, the things worth trying, in order: check whether a newer ModDevGradle
release fixes the repository plumbing; try ForgeGradle instead of ModDevGradle's legacy
mode; or drop the two modules if your project does not target Forge — nothing else depends
on them.

## Where the jars go

The distributable jars are built straight into `build/libs/` at the repository root, so you
can drag them onto a server without hunting through module directories. Nothing else is
written there.

Intermediate jars — the individual SpigotMC, PaperMC, and FoliaMC jars that get shaded into
the universal engine jar — stay in their own module's `build/libs/`.

## Configuration

`gradle.properties` carries the GitHub coordinates (`git-org-name`, `git-repository-name`),
the version, and every Minecraft and loader version to target. The two names that appear in
code — the package segment and the plugin id — are at the top of the root `build.gradle`.

Do not edit these by hand for a fork. Run `PROMPT.md` at the repository root: it asks what
the project should be called, rewrites all of it including the package directories and class
names, and then deletes itself.

## Retargeting the Minecraft version

The template targets **1.21.11**, and not every version is a valid target. Mojang stopped
publishing obfuscation mappings with the 26.x line, and Yarn has no 26.x builds, so Fabric
Loom and ModDevGradle cannot set up for those versions at all — the Bukkit side would build
and the mods would not.

Before changing `minecraft-version`, confirm the version publishes mappings:

```bash
# the version's JSON must list client_mappings under "downloads"
curl -s https://piston-meta.mojang.com/mc/game/version_manifest_v2.json   | python3 -c "import sys,json;print([v['url'] for v in json.load(sys.stdin)['versions'] if v['id']=='1.21.11'][0])"
```

Then update the five platform coordinates and the loader versions alongside it, set
`java-version` to whatever that release's `javaVersion` says, and regenerate the daemon pin
with `./gradlew updateDaemonJvm --jvm-version=<that version>`.
