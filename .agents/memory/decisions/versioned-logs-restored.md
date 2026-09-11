---
name: memory-decisions-versioned-logs-restored
description: Why wiki/logs/ went back to version directories when the template became a project, and what that changes about the gate.
---

# Version directories restored for the logs

The template dated its logs — `wiki/logs/{yyyy}/{mm}/{dd}/` — because its version was pinned
at `0.0.0` permanently and a version directory would have encoded a claim it could never
make. Creating a date directory asserts only *when* something happened, so it was ungated.

This repository is no longer a template. The user chose version directories, so
`wiki/logs/{Major}/{Minor}/{Patch}/CHANGELOG.md` is the structure again and the template's
`wiki/logs/2026/09/04/` was deleted with it.

## What changes

**Creating a log directory is gated again.** A `{Major}/{Minor}/{Patch}/` directory is a
version claim, and `{shared}/rules/versioning.md` gates every version claim on explicit user
approval. That is the opposite of the rule this repository carried as a template, and
`.agents/index/logs-index.md` was rewritten to say so rather than left describing dated
entries.

## The current version is still 0.0.0

The user set all three repositories to `0.0.0`, so the only log directory is
`wiki/logs/0/0/0/`. Nothing has been released. A version directory here is a claim about
which version the entry belongs to, not a claim that it shipped.
