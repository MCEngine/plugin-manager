# Project Overview

[← Back to README](../../README.md)

## Project identity

| | |
|---|---|
| **Platform** | [github.com](https://github.com) |
| **Organization** | [MCEngine](https://github.com/MCEngine) |
| **Repository** | [plugin-manager](https://github.com/MCEngine/plugin-manager) |
| **Plugin id** | `MCPluginManager` |
| **Namespace** | `io.github.mcengine.pluginmanager` |
| **Command** | `/mcpluginmanager`, alias `/mcpm` |

## What this is

**MCPluginManager** installs, updates and removes plugins on a Minecraft server, driven from
a central server rather than by hand.

An operator running twenty plugins across four servers has no good way to answer "which of
these is out of date, and where". An author publishing a new jar has no good way to get it
onto those servers other than telling people to download it. This plugin is the end of that
loop that runs on the server: it reports what is installed, asks what should be installed,
and applies the difference.

## The three repositories

| Repository | Role |
|---|---|
| `MCEngine/plugin-manager` | This one. The plugin, and the mod half. |
| `MCEngine/server-expressjs` | The central server: accounts, the artifact catalogue, tokens, the fleet control plane. |
| `MCEngine/client-reactjs` | The web panel a person publishes and administers from. |

This repository owns none of the server's HTTP contract. It is a client of it.

## Why both halves live here

"Universal" means one repository covers both halves of a modern Minecraft project, which are
usually split across two:

- a **server plugin** for the Bukkit family — SpigotMC, PaperMC, and Folia — including a
  single universal jar that detects the running server and adapts to it, so you ship one
  file instead of three;
- **standalone mods** for Forge, Fabric, and NeoForge, each split into a client jar and a
  server jar that work together, with the client sending actions to the server over a plugin
  message channel.

Both halves sit on top of one shared contract, so behaviour is defined once and the platform
modules only supply what is genuinely platform-specific.

A plugin and its companion mod normally drift apart: they live in separate repositories, on
separate release cadences, and the message protocol between them is written down twice.
Keeping them together means the shared contract is a compile-time dependency for both sides
rather than a document, so a change that breaks the protocol fails the build instead of
failing in production.

## Three constraints that shape the design

**Bukkit cannot safely unload a plugin.** There is no supported `unload`, and classloader
tricks leak. So an update is downloaded, checksummed, and written into `plugins/update/`,
which the server itself applies on the next restart; a removal is marked and performed at
shutdown, because a loaded jar cannot be deleted on Windows while the server runs.

**Folia has no single main thread.** It divides the world into independently ticking regions,
so work is scheduled through `AsyncScheduler` for the network and disk, and folded back
through `GlobalRegionScheduler`. The scheduler abstraction in `platforms/bukkit/core` is
where that lives, and it is the reason one jar can serve all three server flavours.

**A plugin that installs code is a supply chain.** Every download is verified against the
checksum the central server declared before it is written anywhere the server will load
from, and a file name carrying a path separator is refused outright.

## Current state

Pre-release at `0.0.0`. The instruction system, the Gradle build, the shared contract and
both platform halves exist and are green. The manager logic — the client that reaches a
central server, the version comparison, the download and apply steps — is being added task by
task; until it lands, the repository map at `.agents/wiki/context/repository-map.md` is the
accurate statement of what is actually present.

## Working with agents

This repository consumes a shared agent instruction set over an MCP connector rather than
carrying its own copy. See [`AGENTS.md`](../../AGENTS.md).
