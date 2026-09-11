# Architecture

[← Back to README](../../README.md)

How this project is laid out, and the dependency rules that keep the layout honest. For what
the project *is*, see [Project Overview](overview.md); for building it, see
[Local Setup](../environments/setup.md).

## Modules

| Module | Package | Contains |
|---|---|---|
| `api` | `io.github.mcengine.pluginmanager.api` | The shared contract: interfaces, records, enums, and one abstract dispatcher. No dependencies at all. |
| `common` | `io.github.mcengine.pluginmanager` and `.common` | The implementation, plus the single public facade `MCPluginManagerProvider`. |

| `platforms/bukkit/core` | `...bukkit.core` | `AbstractMCPluginManagerPlugin`, the scheduler abstraction, the command, and the listener. |
| `platforms/bukkit/spigotmc` | `...bukkit.spigotmc` | `MCPluginManagerSpigotMC` — the entry point, and nothing else. |
| `platforms/bukkit/papermc` | `...bukkit.papermc` | `MCPluginManagerPaperMC`. |
| `platforms/bukkit/foliamc` | `...bukkit.foliamc` | `MCPluginManagerFoliaMC` and `FoliaPlatformScheduler`. |
| `platforms/bukkit/engine` | `...bukkit.engine` | `MCPluginManagerEngine` — the universal jar that bundles all three. |

| `platforms/mods/core` | `...mod.core` | `MCPluginManagerChannel` and `MCPluginManagerPayloadCodec`. No Minecraft dependency. |
| `platforms/mods/{forge,fabric,neoforge}/client` | `...mod.<loader>.client` | Sends actions; renders answers. |
| `platforms/mods/{forge,fabric,neoforge}/server` | `...mod.<loader>.server` | Decodes, runs the service, replies. |

## The Bukkit side

### One bootstrap, three entry points

Enabling, config, command and listener registration, and starting the service are identical
on all three servers, so they live once in `AbstractMCPluginManagerPlugin`. Each platform module
supplies only its scheduler:

```java
public class MCPluginManagerSpigotMC extends AbstractMCPluginManagerPlugin {
    @Override
    protected PlatformScheduler createScheduler() {
        return new BukkitPlatformScheduler(this);
    }
}
```

That is the entire class. A test in each module asserts it declares exactly one method, so
platform-specific logic cannot quietly accumulate in three places.

### The scheduler abstraction

Shared code compiles against the plain `spigot-api` and so cannot name Paper's
`AsyncScheduler` or Folia's region schedulers. `PlatformScheduler` is the seam.

Its signatures are shaped by Folia, the strictest platform: Folia spreads the world across
threads, so an entity may move to another thread — or stop existing — between scheduling
work and running it. `runForEntity` therefore takes a `retired` callback that Spigot and
Paper never invoke. Designing for the strictest case costs the others nothing.

### The universal engine jar

`MCPluginManagerEngine` detects the server at enable time by probing for classes —
`io.papermc.paper.threadedregions.RegionizedServer` means Folia,
`com.destroystokyo.paper.PaperConfig` means Paper, otherwise Spigot — and installs the
matching scheduler.

It probes rather than compiling against all three APIs because it cannot do the latter:
Spigot, Paper, and Folia all provide the same Gradle capability, so declaring them together
is a dependency conflict Gradle rejects. The module compiles against the Folia API alone,
which is a superset of the other two.

The three platform modules declare `api`, `common`, and `core` as `compileOnly` and disable
their thin jar. Only the engine shades them, so the universal jar holds exactly one copy of
every class. The engine's shadow configuration also strips the `plugin.yml` out of each
bundled platform jar, keeping only its own, so the shipped jar has a single descriptor.

### Jar outputs

| Jar | Built to | Distributed |
|---|---|---|
| `MCPluginManagerEngine-{version}.jar` | `build/libs/` at the repository root | Yes |
| `MCPluginManagerSpigotMC-{version}.jar` etc. | The module's own `build/libs/` | No — bundled into the engine |
| `plugin-manager-api/common/bukkit-core-{version}.jar` | The module's own `build/libs/` | No |

### Renaming stays a one-file edit

The project's identity is spelled out once, not scattered. `gradle.properties` carries only
the GitHub owner and repository name, from which the Maven group is derived. The three values
that name the project in code — the package segment (`pluginmanager`), the plugin id
(`MCPluginManager`) and the command alias (`mcpm`) — sit at the top of the root
`build.gradle`, next to each other, and every module reads them through `namespace`,
`pluginId`, `commandName` and `commandAlias`.

So the plugin descriptor's `main:`, the jar base names, the mod ids, and the
`/mcpluginmanager` command are all generated. The only things a rename touches by hand are
the package directories and the `MCPluginManager*` class names, which no build system can
rewrite for it, plus three string literals that are Java annotations and constants: the
channel namespace in `MCPluginManagerChannel`, and the Forge and NeoForge `@Mod` ids.

## The shared contract

`api` depends on nothing: not Bukkit, not a mod loader, not `common`. That is the whole point
of it. A Bukkit listener, a Fabric client, and a NeoForge server all compile against the same
`MCPluginManagerAction`, `MCPluginManagerRequest`, and `MCPluginManagerResponse`, so the protocol between them is
checked by the compiler rather than described in a document that drifts.

Three shapes carry the contract:

- **`MCPluginManagerAction`** — the wire vocabulary. Adding a constant changes the protocol.
- **`MCPluginManagerRequest` / `MCPluginManagerResponse`** — immutable records. `MCPluginManagerRequest`
  normalizes a missing payload to the empty string in its compact constructor, so no handler
  has to null-check it.
- **`MCPluginManagerService`** — the behaviour, with `AbstractMCPluginManagerService` supplying the
  dispatch every implementation would otherwise write itself.

`AbstractMCPluginManagerService.handle` switches over the enum with **no `default` branch**. Adding
an action therefore breaks the build until a handler exists, rather than reaching production
and failing on whichever platform received the new action first.

## Asynchronous by signature

Anything that can block, fail, or reach storage returns a `CompletableFuture`. Only
`greetingFor`, which answers from memory, is synchronous. A caller can tell the two apart
from the signature alone, which matters on a Minecraft server where blocking the main thread
is a visible stall for every player online.

## One way in

Every platform module reaches the implementation through
`io.github.mcengine.pluginmanager.MCPluginManagerProvider`, and nothing else.

The facade deliberately sits at the **root of the namespace**, one package above the
`common` implementation classes it wraps, so someone opening the source tree meets the
supported entry point before they meet anything they should not depend on. It holds its
service privately and returns it from no method, so there is no supported way to reach
around it.

The rule that follows: **a platform module never imports from
`io.github.mcengine.pluginmanager.common`.** If a platform needs something the facade
does not expose, the fix is a method on the facade, not an import.

## Dependency rules

```
api        <-  nothing
common     <-  api
platforms  <-  api, common   (never each other)
```

`common` declares `api` with Gradle's `api` configuration rather than `implementation`, so a
platform module that depends on `common` sees the contract types on its own compile
classpath without redeclaring them.

## The mod side

### Six jars, not one

Each loader builds its own jar, and each loader's jar is split into a client half and a
server half. Merging loaders is not possible — they load classes differently and their APIs
do not overlap — and merging the two halves would ship client rendering code to servers and
server state to clients.

The split is also a trust boundary. **The client decides nothing.** It sends an action and
renders whatever comes back; the server owns the state. A modified client can send anything
it likes and still cannot grant itself a result the server did not give it. The server takes
the player's identity from the connection, never from the payload, for the same reason.

### One wire format, shared

`platforms/mods/core` holds `MCPluginManagerChannel` (the two channel identifiers) and
`MCPluginManagerPayloadCodec` (the byte layout), and depends only on `api` and the JDK. Both halves
of every loader encode and decode through it, so client and server cannot disagree about the
format — the compiler and the codec's round-trip tests enforce it.

The action is written **by name, not by ordinal**: reordering `MCPluginManagerAction` would
otherwise silently change the meaning of every packet already in flight.

What is duplicated per loader is only the payload wrapper, because `CustomPayload` is a
Minecraft type and each loader sees it under different mappings — Fabric under Yarn, Forge
and NeoForge under Mojang mappings via ModDevGradle. The wrapper is a few lines around a
`byte[]`; the part that matters is shared.

### Opt-in builds

`./gradlew build` does not build the loader modules. `settings.gradle` includes them only
under `-Pmods=true`, because Loom and ModDevGradle download and decompile Minecraft on first
run. `platforms/mods/core` is always included: it needs no Minecraft, so the wire format
keeps compiling even when the loaders are switched off.

Forge sits behind a second flag, `-Pforge=true`, and does not currently build — see
[Local Setup](../environments/setup.md) for the diagnosis. `-Pmods=true build` produces the
Fabric and NeoForge jars and is green.
