# Project Overview

[← Back to README](../../README.md)

## Project identity

| | |
|---|---|
| **Platform** | [github.com](https://github.com) |
| **Organization** | [MCEngine](https://github.com/MCEngine) |
| **Repository** | [universal-template](https://github.com/MCEngine/universal-template) |

## What this is

A **template for a universal Minecraft plugin** — a starting point you fork, rename, and
build on. It is not a plugin you install.

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

## Why one repository

A plugin and its companion mod normally drift apart: they live in separate repositories, on
separate release cadences, and the message protocol between them is written down twice.
Keeping them together means the shared contract is a compile-time dependency for both sides
rather than a document, so a change that breaks the protocol fails the build instead of
failing in production.

## What a fork changes

Run `PROMPT.md` at the repository root. It asks what the project is — plugin id, version,
organization and repository, license, group and namespace, and what the README and wiki
should say — applies the answers across the build, the sources and the documentation, and
then deletes itself, because a setup prompt that stays behind is just clutter in someone
else's repository.

## Current state

The agent instruction system, the documentation trees, and the memory tree exist. The Gradle
build and the source modules are being added task by task; until they land, the repository
map at `.agents/wiki/context/repository-map.md` is the accurate statement of what is
actually present.

## Working with agents

This repository consumes a shared agent instruction set over an MCP connector rather than
carrying its own copy. See [`AGENTS.md`](../../AGENTS.md).
