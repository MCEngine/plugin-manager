---
name: agents-index
description: Index of .agents/ — the sole authority for this repository's own instruction set.
---

# Agents Index

**Scope:** `.agents/`
**Parent:** [root-index](root-index.md)

This index is the sole authority that indexes and manages `.agents/`. Nothing outside
`.agents/` may dictate or write files inside this tree. Any file added to or removed from
`.agents/` is reflected here in the same commit.

This index lists **local** instructions only. Branching, commits, pull requests, the task
workflow, the creators, and the placement and versioning rules are served by the
`lxagents-agents-base` connector and are addressed as `agents://{folder}/{file}.md`; route
into them through `agents://index/root-index.md`. They are deliberately absent below — a
local copy would override the shared file by `name` and then silently go stale.

## Rules

| File | Purpose |
|---|---|
| [`rules/repository.md`](../rules/repository.md) | Rules specific to this repository: module boundaries, build commands, jar outputs, what a fork must rename. |

## Other folders

None. `.agents/index/`, `.agents/wiki/` and `.agents/memory/` are reserved structural
folders and are routed by their own indexes, listed in the root index. This repository
carries no other instruction folder yet; each one added gets a `##` section here in the same
commit.
