---
name: memory-decisions-branch-convention-over-harness
description: Why this work uses stacked {type}/{primary-noun} branches instead of the single harness branch, and who decided it.
---

# Branch convention over the harness default

The harness running this work pinned all three repositories to the branch
`claude/epic-maxwell-juc8tk` and instructed that nothing be pushed anywhere else without
explicit permission.

`{shared}/git/branching-strategy.md` forbids exactly that shape: tool-preset prefixes
(`claude/`), generated suffixes, and session identifiers are all named as forbidden, and it
requires one task per branch stacked in dependency order.

## The decision

**The user was asked and chose the shared convention.** That is the explicit permission the
harness required, so:

* Branches are `{type}/{primary-noun}` — `chore/mcpluginmanager-platform-plan`,
  `docs/agents-setup`, `refactor/namespace`, and so on.
* One task per branch, stacked: task `k` branches from task `k-1` within a repository.
* Pull requests target `master`.
* `claude/epic-maxwell-juc8tk` is never pushed to.

## Why this is recorded rather than assumed

A future session under the same harness will read the same instruction and, without this
record, will either follow it and fragment the branch history or override it without knowing
a user ever weighed in. The permission was given once, for this work; a new request re-asks.
