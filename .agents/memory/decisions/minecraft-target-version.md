---
name: memory-decisions-minecraft-target-version
description: Why the template targets Minecraft 1.21.11 and not a 26.x release — mappings, and what to check before retargeting.
---

# Decision: the template targets Minecraft 1.21.11

## Context

The template originally targeted **26.1.2**, matching the reference repository
`MCOriax/mcidentity`. The Bukkit side built fine there — Spigot, Paper, and Folia publish
API jars for it and none of them need obfuscation mappings.

The mod side did not. Fabric Loom failed at configuration with *"Failed to find official
mojang mappings for 26.1.2"*, and Forge and NeoForge need the same maps through
ModDevGradle.

## What was actually wrong

**Mojang stopped publishing obfuscation mappings with the 26.x line.** Checking each recent
release's JSON from `piston-meta.mojang.com`:

| Release | `downloads.client_mappings` | Date |
|---|---|---|
| 26.2 | absent | 2026-06-16 |
| 26.1.2 | absent | 2026-04-09 |
| 26.1.1 | absent | 2026-04-01 |
| 26.1 | absent | 2026-03-24 |
| **1.21.11** | **present** | 2025-12-09 |
| 1.21.10 and earlier | present | — |

Yarn has no 26.x builds either, so there is no second source. Without mappings **no mod
toolchain can set up at all** for 26.x. This is not a version-pinning problem that a newer
Loom would fix.

## Options

* **Keep 26.1.2, ship no mods.** The Bukkit half works; the template delivers half of what
  it promises.
* **Bukkit on 26.1.2, mods on 1.21.11.** Everything builds, but a 1.21.11 client mod cannot
  talk to a 26.1.2 server — which is precisely the thing the shared contract and the message
  channel exist to make work.
* **Everything on 1.21.11.** One game version across the template.

## Choice

Everything on 1.21.11, on Java 21. Both halves target the same game, so the client mod and
the server plugin can actually talk to each other, which is the template's whole premise.

Every platform ships for it: spigot-api `1.21.11-R0.2-SNAPSHOT`, paper-api and folia-api
`1.21.11-R0.1-SNAPSHOT`, Forge `1.21.11-61.2.1`, NeoForge `21.11.45`, Fabric API
`0.141.6+1.21.11`, Yarn `1.21.11+build.6`.

## Consequence

* `java-version` dropped from 25 to 21, which is what 1.21.11's `javaVersion` says. The
  earlier "one Java 25 toolchain" reasoning still holds — it is still one toolchain, just a
  different one.
* Fabric uses Yarn mappings rather than `loom.officialMojangMappings()`. The Mojang-mappings
  choice was forced by 26.x having no Yarn build; at 1.21.11 both exist.
* **Before retargeting again, check that the version publishes mappings.** A release without
  `client_mappings` will build the Bukkit side and silently fail the mod side.
* This supersedes the Minecraft target row in
  [`template-identity-values.md`](template-identity-values.md).
