---
name: agents-entry-point
description: Universal entry point for agents working in the universal-template repository — shared set resolution, reading order, and the auto-activation trigger table.
---

# AGENTS.md

This repository is **`MCEngine/universal-template`**: a forkable template for a universal
Minecraft plugin. One Gradle multi-project build carries both halves of such a project — a
Bukkit server plugin (`platforms/bukkit/`, covering SpigotMC, PaperMC and FoliaMC plus a
single universal engine jar) and standalone mods (`platforms/mods/`, covering Forge, Fabric
and NeoForge, each split into a client and a server jar) — over a shared `api/` contract and
a `common/` implementation. Forks rename it and build on it; it is not a plugin in its own
right — **start by running [`PROMPT.md`](PROMPT.md)**, which asks what the project should be
and rewrites the repository to match.

## Shared Instruction Set

The conventions this repository follows — branching, commits, pull requests, task
workflow, the creators — live in the shared instruction set served by the
**`lxagents-agents-base`** MCP server. This repository carries only what is its
own. **Resolve the shared set before doing any work:**

1. If the `lxagents-agents-base` connector is available in this session, that is
   the shared set. Refer to it as `{shared}`; its files are addressed as
   `agents://{folder}/{file}.md`.
2. Read `agents://manifest.json` once. It lists every shared file with its `name`,
   path and description — one read instead of twenty, and it is what the routing
   tables below are checked against.
3. Read `agents://index/root-index.md` and route from there. Do not bulk-read the
   set.
4. If the connector is not available, say so plainly and continue with this
   repository's local instruction set only. **Do not reconstruct the missing rules
   from memory, and do not clone or copy them into this repository.**

**One call instead of six.** Where the connector exposes tools,
`agents_auto_activation` returns steps 2 and 3 together with the four files that load
on every request. It does not cover the local reads — this file, the local root index,
and the local memory index are still read from disk.

Never commit shared content into this repository. A file that can be read from
`agents://` must not exist here as a copy — see
`{shared}/rules/duplicate-instruction-audit.md`.

**Local overrides shared.** A file in `.agents/` whose `name` matches a shared
file's `name` replaces that shared file entirely for this repository. The current
overrides are listed in
[`.agents/index/root-index.md`](.agents/index/root-index.md).

## Auto-Activation

The instruction set is **always active** — the local `.agents/` set and the shared set
together. It applies to every task in this repository whether or not the user mentions
it, links to it, or asks for it. Treat these files as standing orders, not as optional
reference material.

At the start of every session, before doing any work:

1. Read `AGENTS.md` (this file).
2. Resolve the shared set per the bootstrap above.
3. Read [`.agents/index/root-index.md`](.agents/index/root-index.md).
4. Read [`.agents/index/memory-index.md`](.agents/index/memory-index.md) and load only
   the memory rows whose scope matches the current request, so you continue prior work
   instead of restarting it.
5. Load the four mandatory standard files, whatever the request looks like.
6. Match the request against the trigger table below and load the instruction files it
   names, local first, shared second.

Four files load on **every** request rather than on a trigger — the task workflow, the
branching strategy, the commit conventions, and the discovery protocol — along with the
three permission gates that ride with them: approve the plan before any file is
written, ask before opening a pull request, ask before merging. See
`{shared}/rules/shared-instructions.md` §H.

Steps 2, 5 and 6 are one call to `agents_auto_activation` where the connector exposes
tools. Steps 1, 3 and 4 read files in this repository and are still read from disk.

If a rule conflicts with a habit, a default, or a template you would otherwise follow,
the rule wins. If it conflicts with an explicit instruction from the user in this
session, the user wins — and you say out loud which rule you are setting aside.

## Auto-activation trigger table

Mirrored row for row from `{shared}/rules/auto-activation.md`, which is the source of
truth, with this repository's own rows appended below.

| When you are about to… | Load and obey |
|---|---|
| Take in any new request of more than one step | `{shared}/planning/task-workflow.md` |
| Create a branch | `{shared}/git/branching-strategy.md` |
| Write a commit message | `{shared}/git/commit-conventions.md` |
| Open or update a pull request | `{shared}/git/pull-request-template.md` |
| Write **any** commit, tag, PR, comment, or file that will be committed or posted | `{shared}/rules/no-session-links.md` |
| Wonder whether something is local or shared, or need to override a shared rule | `{shared}/rules/shared-instructions.md` |
| Decide where a new file goes | `{shared}/rules/directories.md` |
| Resolve, connect, or fail to reach the shared set | `{shared}/rules/mcp-connector.md` |
| Add, move, rename, or delete any file in a set or in `wiki/` | `{shared}/creators/index-creator.md` |
| Write a rule or instruction | `{shared}/creators/instruction-creator.md` |
| Write documentation, an SOP, or a domain guideline | `{shared}/creators/information-creator.md` |
| Write or change a security file — a policy, a threat model, or a security SOP | `{shared}/creators/security-creator.md` |
| Change code or structure that a document describes | `{shared}/rules/change-propagation.md` |
| Record progress, a decision, or session state | `{shared}/creators/memory-creator.md` |
| Touch anything that carries a version number | `{shared}/rules/versioning.md` |
| Record a release | `{shared}/creators/changelog-creator.md` |
| Store, read, or construct a model identifier — any `model_name` column | `{shared}/rules/model-naming-convention.md` |
| Report finished work back to the user | `{shared}/rules/work-summary.md` |
| Need project facts, commands, or orientation | [`.agents/wiki/context/repository-map.md`](.agents/wiki/context/repository-map.md) |
| Do anything at all in this project | [`.agents/rules/repository.md`](.agents/rules/repository.md) |

### This repository's own rows

| When you are about to… | Load and obey |
|---|---|
| Set this fork up for the first time, or rename it | [`PROMPT.md`](PROMPT.md) |

[`PROMPT.md`](PROMPT.md) is the one-time setup procedure: it asks what the project should be
called and what it does, rewrites the build, the Java tree and the documentation, and then
deletes itself along with this row. **If `PROMPT.md` is absent, setup has already happened**
— this row goes with it.

Beyond that this repository carries the mandatory core set only. Local instruction files are
added under `.agents/{folder}/{file}.md` through the discovery protocol below, and each one
gets a row here in the same commit that creates it.

## Reading order (mandatory)

1. Read `AGENTS.md`.
2. Resolve the shared set, as above.
3. Read [`.agents/index/root-index.md`](.agents/index/root-index.md) — and nothing else at
   this stage.
4. From its routing table, pick the ONE index whose scope matches the task, and read that
   index.
5. If that index delegates to a child index, follow the one branch that matches.
6. Only then open the specific file(s) you need.

## Routing protocol (context discipline)

Route by reading index tables, not by reading files.

* Do NOT load every index.
* Do NOT bulk-scan `.agents/**` or the shared set to build a registry — `agents://manifest.json`
  already is one.
* Do NOT read an instruction body until that instruction has been selected.

Each index row's purpose text is what you route on; the file body is what you load after
choosing. The standing exception is `memory-index.md`, read every session because
continuity depends on it.

## Iron rule (separation of concerns)

* `AGENTS.md` and `README.md` are overviews and must never carry detailed rules or
  documentation.
* [`.agents/index/root-index.md`](.agents/index/root-index.md) is a **router only**. It
  lists other indexes. It must never contain rules, documentation, prose, or direct links
  to leaf content.
* Each index owns exactly one scope and writes outside it never.
* **Local carries only what is local.** A convention true for more than one repository
  belongs in the shared set — propose it there, do not copy it here.
* `wiki/` is for humans, `.agents/wiki/` is for agents, and neither duplicates the other.
* **One subject per file.** A cross-cutting rule gets its own file and is linked, not
  pasted into a file about something else.
* An index never teaches. The moment it explains something, that content belongs in a real
  file.

## Placement

* Local instructions go to `.agents/{folder}/{file}.md`.
* Human documentation goes to `wiki/{folder}/{file-name}.md`.
* Agent knowledge goes to `.agents/wiki/{type}/{file-name}.md`.
* Memory goes to `.agents/memory/{type}/{file-name}.md`, and indexes to
  `.agents/index/{scope}-index.md`.
* Anything universal belongs to the shared set, not here.

No `INDEX.md`, anywhere, ever. The full authority is `{shared}/rules/directories.md`.

## Discovery Protocol

Source of truth: `{shared}/rules/discovery-protocol.md`.

While working, if you notice an instruction worth adding — a new rule, or new
content for an existing instruction file — do NOT create or edit it yourself.
Collect the findings, and when the task is done present them to the user:

* one finding per message block, each in its own code block;
* state the target set — `local` (this repository) or `shared` (the organization's
  instruction set served by the `lxagents-agents-base` connector);
* include the proposed file path, `name`, `description`, and the full proposed
  body;
* explain in one line why it is worth adding.

Then let the user select which findings to apply. Create only the selected ones.
Never batch-apply, never apply silently. A `shared` finding is never written from a
consuming repository — it is reported so it can be raised against the shared set.

**Scope of this gate:** it covers instruction files in either set. Documentation
pages under `wiki/` and `.agents/wiki/` may be written when the facts are real and
verified. Memory under `.agents/memory/` is written freely and automatically — see
`memory-policy.md`.

## Version rule

Never change the project version without explicit user approval — see
`{shared}/rules/versioning.md`. In this repository the answer is effectively always no:
`project-version` in `gradle.properties` is fixed at `0.0.0`, because a template has nothing
to release.

For the same reason this repository's logs are **dated, not versioned** —
`wiki/logs/{yyyy}/{mm}/{dd}/CHANGELOG.md`. A date directory is not a version claim, so
creating one is not gated; record the day's work as you land it. The carriers and the full
rule are in [`.agents/rules/repository.md`](.agents/rules/repository.md).

## No session links

Never write a link or identifier pointing at an assistant or tool session into a file,
commit message, commit trailer, branch name, tag, pull request, or comment. If your tooling
appends one by default, strip it before committing or posting — see
`{shared}/rules/no-session-links.md`.
