---
name: memory-decisions-prompt-file-at-root
description: Why PROMPT.md sits at the repository root, against the shared directory rule that restricts the root to three files.
---

# Decision: PROMPT.md at the repository root

## Context

`{shared}/rules/directories.md` states that only `AGENTS.md`, `README.md` and `LICENSE` may
be added at the repository root. The user required a `PROMPT.md` at the root: the guide a
fork reads to learn what must be renamed and where.

## Options

* **`.agents/prompts/PROMPT.md`** — obeys the directory rule, but the shared set explicitly
  reserves `prompts/` to itself, and a fork guide buried three levels down is not found by
  the person forking.
* **`wiki/guides/forking.md`** — obeys the rule and is human documentation, but `AGENTS.md`
  must point agents at it by name, and the user asked for a root file.
* **`PROMPT.md` at the root** — breaks the rule, is immediately visible to anyone who opens
  the repository, and is what the user asked for.

## Choice

`PROMPT.md` at the root. An explicit user instruction outranks a shared rule by precedence 1.

## Consequence

The rule is set aside for this one file, deliberately and on the record — not by oversight.
`.claude/CLAUDE.md` is unaffected by the rule: it is tool configuration, not an instruction
file, and it holds no content of its own beyond an import.

A future duplicate-instruction audit or setup run will flag `PROMPT.md` as a root file the
rule does not permit. This entry is the answer: it stays.
