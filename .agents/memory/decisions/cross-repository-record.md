---
name: memory-decisions-cross-repository-record
description: Why one task record in plugin-manager covers work in three repositories, and how the other two point at it.
---

# One task record, three repositories

`{shared}/planning/task-workflow.md` §B reserves task 1 for the record and says a change
spanning repositories is always more than one work task. It does not say where the record
goes when the work spans three.

**The record lives in `plugin-manager`**, at
`.agents/memory/tasks/mcpluginmanager-platform.md`.

## Why

At the time task 1 is written it is the only repository with a `.agents/memory/` tree —
`server-expressjs` and `client-reactjs` get theirs in tasks 2 and 3, which the record itself
plans. A record cannot be written into a tree that the record has not yet planned into
existence.

Three parallel records would also drift. The plan is one plan; the tasks are ordered against
each other across repositories, and a reviewer checking whether task 11 landed before task 17
needs one table, not three.

## How the other two find it

`server-expressjs` and `client-reactjs` each carry
`.agents/memory/tasks/mcpluginmanager-platform.md` holding **only** their own task entries
and a line naming `MCEngine/plugin-manager` as the repository holding the plan table. They
never copy the table.

This is the same principle as the shared instruction set: the fact lives in one place and
everywhere else links to it.
