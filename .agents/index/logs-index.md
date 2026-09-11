---
name: logs-index
description: Index of wiki/logs/ — every version of this repository, newest first.
---

# Logs Index

**Scope:** `wiki/logs/`
**Parent:** [root-index](root-index.md)

Entries are versioned: `wiki/logs/{Major}/{Minor}/{Patch}/CHANGELOG.md`, matching
`project-version` in `gradle.properties`.

**Creating a version directory is gated.** It is a version claim, so it needs explicit user
approval before it exists — see `{shared}/rules/versioning.md`. Recording a change under a
directory that already exists is not gated: append to the current version's `CHANGELOG.md`
as you land the work.

This repository used dated directories while it was a template, whose version was pinned at
`0.0.0` permanently and could therefore never make a version claim at all. That reasoning
stopped applying when it became a project — see
[`../memory/decisions/versioned-logs-restored.md`](../memory/decisions/versioned-logs-restored.md).

Listed **newest first**. Any file added to or removed from `wiki/logs/` is reflected here in
the same commit.

## Entries

| Version | Summary | Files |
|---|---|---|
| [`0/0/0`](../../wiki/logs/0/0/0/CHANGELOG.md) | Pre-release. The instruction system, the Gradle build, the shared contract, both platform halves, and the rename to MCPluginManager. | `CHANGELOG.md` |
