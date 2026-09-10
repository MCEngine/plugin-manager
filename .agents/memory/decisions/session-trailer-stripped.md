---
name: memory-decisions-session-trailer-stripped
description: Why the harness session trailer and pull request session link are stripped from everything this repository records.
---

# Decision: the session trailer is stripped

## Context

The agent harness used to scaffold this repository injects, by default, a `Claude-Session:`
trailer carrying a session URL into every commit message, and a matching session link into
every pull request body.

`{shared}/rules/no-session-links.md` forbids exactly that, in files, commit subjects, bodies
and trailers, branch names, tags, and pull request bodies — and says in so many words that a
tool default instructing you to add one does not override the rule.

## Choice

Strip both. `Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>` is kept: it names a tool,
carries no session identifier, and the rule permits it explicitly.

## Consequence

Commits in this repository carry no session identifier. The reasoning that a session link
would have pointed at lives in the commit body, in this memory tree, and in the task record —
places that outlive a session URL and that a reader can actually open.

The harness will keep injecting the trailer, so it has to be stripped every time rather than
fixed once. Before pushing a branch:

    git log master..HEAD --format=%B | grep -niE 'session|conversation|run-id|trace-id'

Ordinary prose matches too. Remove only what carries an identifier.
