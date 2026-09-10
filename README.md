# plugin-manager

**MCPluginManager** — a Minecraft plugin that installs, updates and removes other plugins on
your server by talking to a central catalogue and control server.

## Key features

- Runs on SpigotMC, PaperMC and Folia from **one jar** that detects the server at startup
  and installs the right scheduler.
- Talks to **several** central servers, each with its own token, so a server can draw from
  more than one catalogue.
- Compares installed versions against the catalogue and stages updates into `plugins/update/`
  for the server to apply on restart — the only safe way, because Bukkit cannot unload a
  plugin.
- Verifies every download against the checksum the server declared before writing it.
- Ships a mod half too: Forge, Fabric and NeoForge, each split into a client jar and a server
  jar that talk over a plugin message channel.

## The three repositories

| Repository | Role |
|---|---|
| [`MCEngine/plugin-manager`](https://github.com/MCEngine/plugin-manager) | This one. The Minecraft plugin. |
| [`MCEngine/server-expressjs`](https://github.com/MCEngine/server-expressjs) | The central server: accounts, the artifact catalogue, tokens, the fleet control plane. |
| [`MCEngine/client-reactjs`](https://github.com/MCEngine/client-reactjs) | The web panel for publishing and administration. |

## Quick start

```bash
./gradlew build                 # compile every module and run the tests
./gradlew -Pmods=true build     # the same, including the Fabric and NeoForge mods
```

Jars land in `build/libs/` at the repository root. You need no local Gradle and no local
JDK — the wrapper is committed and the Java 21 toolchain is downloaded on first build.

## Status

Pre-release at `0.0.0`. The build, the shared contract and both platform halves are in place
and green; the plugin manager logic itself is being added task by task. See
[`.agents/wiki/context/repository-map.md`](.agents/wiki/context/repository-map.md) for
exactly what the repository does and does not contain right now.

## Documentation

The full map is [`.agents/index/project-wiki-index.md`](.agents/index/project-wiki-index.md).

Start here:

- [Project Overview](wiki/information/overview.md) — what MCPluginManager is, why both halves
  share one repository, and how it reaches a central server.
- [Local Setup](wiki/environments/setup.md) — requirements, build commands, and where the
  jars go.

## Working with agents

See [`AGENTS.md`](AGENTS.md). This repository consumes a shared agent instruction set served
over the `lxagents-agents-base` MCP connector; it carries no copy of that set.

## License

See [`LICENSE`](LICENSE).
