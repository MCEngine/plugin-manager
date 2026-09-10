---
name: logs-index
description: Index of wiki/logs/ — every day this repository changed, newest first.
---

# Logs Index

**Scope:** `wiki/logs/`
**Parent:** [root-index](root-index.md)

Entries are dated, not versioned: `wiki/logs/{yyyy}/{mm}/{dd}/CHANGELOG.md`, with `mm` and
`dd` zero-padded so the directories sort correctly. This repository's version is fixed at
`0.0.0` — see [`../rules/repository.md`](../rules/repository.md) — so a version directory
would encode a claim it can never make, while a date encodes the only ordering it has.

**A date directory is not a version claim, so creating one is not gated.** Record the day's
work as you land it. That is the opposite of the rule for a version directory, which
`{shared}/rules/versioning.md` gates on explicit user approval.

Two changes on the same day share one file: append to it rather than inventing a suffix.

Listed **newest first**. Any file added to or removed from `wiki/logs/` is reflected here in
the same commit.

## Entries

| Date | Summary | Files |
|---|---|---|
| [`2026/09/04`](../../wiki/logs/2026/09/04/CHANGELOG.md) | Built the repository into a working universal plugin template: instruction system, Gradle build, shared contract, Bukkit plugin, and the mod side. | `CHANGELOG.md` |
