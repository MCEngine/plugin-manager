# universal-template

A forkable template for a **universal Minecraft plugin**: one repository carrying both a
Bukkit server plugin and standalone mods, over a single shared contract.

## Key features

- One Gradle multi-project build for both halves of a Minecraft project.
- Bukkit side: SpigotMC, PaperMC, and Folia, plus a universal engine jar that detects the
  running server at startup and installs the right scheduler — one file instead of three.
- Mod side: Forge, Fabric, and NeoForge, each producing a client jar and a server jar that
  talk over a plugin message channel.
- A shared `api/` contract and a single `common/` facade class, so a developer integrating
  against this plugin reads one file.
- Renaming is one guided pass: `PROMPT.md` asks what your project is, rewrites the build,
  the sources and the docs, and then deletes itself.

## Forking this template

Run [`PROMPT.md`](PROMPT.md). It asks what your project is — plugin id, version,
organization and repository, license, group and namespace, and what the docs should say —
rewrites the build, the Java tree and the documentation to match, and then deletes itself.

## Quick start

```bash
./gradlew build                 # compile every module and run the tests
./gradlew -Pmods=true build     # the same, including the Forge, Fabric and NeoForge mods
```

Jars land in `build/libs/` at the repository root. You need no local Gradle and no local
JDK 25 — the wrapper is committed and the toolchain is downloaded on first build.

The source modules are still being added task by task; see
`.agents/wiki/context/repository-map.md` for exactly what the repository does and does not
contain right now.

## Documentation

The full map is [`.agents/index/project-wiki-index.md`](.agents/index/project-wiki-index.md).

Start here:

- [Project Overview](wiki/information/overview.md) — what this template is, why both halves
  share one repository, and what a fork changes.
- [Local Setup](wiki/environments/setup.md) — requirements, build commands, and where the
  jars go.

## Working with agents

See [`AGENTS.md`](AGENTS.md). This repository consumes a shared agent instruction set served
over the `lxagents-agents-base` MCP connector; it carries no copy of that set.

## License

See [`LICENSE`](LICENSE).
