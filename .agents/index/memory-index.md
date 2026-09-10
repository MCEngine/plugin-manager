---
name: memory-index
description: Index of .agents/memory/ — task records, durable decisions, and current repository state.
---

# Memory Index

**Scope:** `.agents/memory/`
**Parent:** [root-index](root-index.md)

Any file added to or removed from `.agents/memory/` is reflected here in the same commit.
This index lists memory files only — never rules, never documentation.

Memory is written freely and automatically; it is the one tree exempt from the discovery
protocol's approval gate. See `{shared}/rules/memory-policy.md`.

## State

| File | Purpose |
|---|---|
| [`state/repository-state.md`](../memory/state/repository-state.md) | What exists in this repository right now, what does not yet, and the next obvious step. |

## Tasks

| File | Purpose |
|---|---|
| [`tasks/universal-plugin-template.md`](../memory/tasks/universal-plugin-template.md) | Scaffolding this repository into a universal plugin template: the confirmed task list and one entry per task. |

## Decisions

| File | Purpose |
|---|---|
| [`decisions/prompt-file-at-root.md`](../memory/decisions/prompt-file-at-root.md) | Why `PROMPT.md` sits at the repository root against the directory rule. |
| [`decisions/session-trailer-stripped.md`](../memory/decisions/session-trailer-stripped.md) | Why the harness session trailer is stripped from commits and pull requests. |
| [`decisions/template-identity-values.md`](../memory/decisions/template-identity-values.md) | The namespace, plugin id, version and Minecraft target chosen for the template, and why. |
| [`decisions/minecraft-target-version.md`](../memory/decisions/minecraft-target-version.md) | Why the target is 1.21.11 and not a 26.x release, and what to check before retargeting. |
