# Architecture

[← Back to README](../../README.md)

How this project is laid out, and the dependency rules that keep the layout honest. For what
the project *is*, see [Project Overview](overview.md); for building it, see
[Local Setup](../environments/setup.md).

## Modules

| Module | Package | Contains |
|---|---|---|
| `api` | `io.github.mcengine.universal.api` | The shared contract: interfaces, records, enums, and one abstract dispatcher. No dependencies at all. |
| `common` | `io.github.mcengine.universal` and `.common` | The implementation, plus the single public facade `TemplateProvider`. |

| `platforms/bukkit/core` | `...bukkit.core` | `AbstractTemplatePlugin`, the scheduler abstraction, the command, and the listener. |
| `platforms/bukkit/spigotmc` | `...bukkit.spigotmc` | `TemplateSpigotMC` — the entry point, and nothing else. |
| `platforms/bukkit/papermc` | `...bukkit.papermc` | `TemplatePaperMC`. |
| `platforms/bukkit/foliamc` | `...bukkit.foliamc` | `TemplateFoliaMC` and `FoliaPlatformScheduler`. |
| `platforms/bukkit/engine` | `...bukkit.engine` | `TemplateEngine` — the universal jar that bundles all three. |

| `platforms/mods/core` | `...mod.core` | `TemplateChannel` and `TemplatePayloadCodec`. No Minecraft dependency. |
| `platforms/mods/{forge,fabric,neoforge}/client` | `...mod.<loader>.client` | Sends actions; renders answers. |
| `platforms/mods/{forge,fabric,neoforge}/server` | `...mod.<loader>.server` | Decodes, runs the service, replies. |

## The Bukkit side

### One bootstrap, three entry points

Enabling, config, command and listener registration, and starting the service are identical
on all three servers, so they live once in `AbstractTemplatePlugin`. Each platform module
supplies only its scheduler:

```java
public class TemplateSpigotMC extends AbstractTemplatePlugin {
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

`TemplateEngine` detects the server at enable time by probing for classes —
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
| `TemplateEngine-{version}.jar` | `build/libs/` at the repository root | Yes |
| `TemplateSpigotMC-{version}.jar` etc. | The module's own `build/libs/` | No — bundled into the engine |
| `universal-template-api/common/bukkit-core-{version}.jar` | The module's own `build/libs/` | No |

### Renaming stays a one-file edit

A fork's rename is one guided pass, not a scattered edit. `gradle.properties` carries only
the GitHub owner and repository name, from which the Maven group is derived. The two values
that name the project in code — the package segment (`universal`) and the plugin id
(`Template`) — sit at the top of the root `build.gradle`, next to each other, and every
module reads them through `namespace`, `pluginId`, and `commandName`.

So the plugin descriptor's `main:`, the jar base names, the mod ids, and the `/template`
command are all generated. What a fork edits by hand is the package directories and the
`Template*` class names, which no build system can rewrite for it — and `PROMPT.md` at the
repository root walks through exactly that, then deletes itself.

## The shared contract

`api` depends on nothing: not Bukkit, not a mod loader, not `common`. That is the whole point
of it. A Bukkit listener, a Fabric client, and a NeoForge server all compile against the same
`TemplateAction`, `TemplateRequest`, and `TemplateResponse`, so the protocol between them is
checked by the compiler rather than described in a document that drifts.

Three shapes carry the contract:

- **`TemplateAction`** — the wire vocabulary. Adding a constant changes the protocol.
- **`TemplateRequest` / `TemplateResponse`** — immutable records. `TemplateRequest`
  normalizes a missing payload to the empty string in its compact constructor, so no handler
  has to null-check it.
- **`TemplateService`** — the behaviour, with `AbstractTemplateService` supplying the
  dispatch every implementation would otherwise write itself.

`AbstractTemplateService.handle` switches over the enum with **no `default` branch**. Adding
an action therefore breaks the build until a handler exists, rather than reaching production
and failing on whichever platform received the new action first.

## Asynchronous by signature

Anything that can block, fail, or reach storage returns a `CompletableFuture`. Only
`greetingFor`, which answers from memory, is synchronous. A caller can tell the two apart
from the signature alone, which matters on a Minecraft server where blocking the main thread
is a visible stall for every player online.

## One way in

Every platform module reaches the implementation through
`io.github.mcengine.universal.TemplateProvider`, and nothing else.

The facade deliberately sits at the **root of the namespace**, one package above the
`common` implementation classes it wraps, so someone opening the source tree meets the
supported entry point before they meet anything they should not depend on. It holds its
service privately and returns it from no method, so there is no supported way to reach
around it.

The rule that follows: **a platform module never imports from
`io.github.mcengine.universal.common`.** If a platform needs something the facade
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

`platforms/mods/core` holds `TemplateChannel` (the two channel identifiers) and
`TemplatePayloadCodec` (the byte layout), and depends only on `api` and the JDK. Both halves
of every loader encode and decode through it, so client and server cannot disagree about the
format — the compiler and the codec's round-trip tests enforce it.

The action is written **by name, not by ordinal**: reordering `TemplateAction` would
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
