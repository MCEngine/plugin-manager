---
name: memory-decisions-template-identity-values
description: What names this project carries, where each one lives, and why the renameable ones are not gradle.properties entries.
---

# Decision: where the template's identity lives

## Context

A fork has to rename this project. The first design put every renameable value in
`gradle.properties` — `orgname`, `reponame`, `pluginid` — and derived the Maven group, the
Java namespace, the jar base names and the command name from those three.

That was wrong in a way worth writing down. Three of those values also appear in the Java
source: the package directories (`io/github/mcengine/universal/…`), the `package` and
`import` statements in every file, and the `Template*` class names. Gradle cannot rewrite
any of that. So a fork still had to do a source-wide rename by hand, *and* keep it in step
with the properties file — two places, no check that they agree.

## Choice

Split by what the build can actually derive.

| Value | Where | Why |
|---|---|---|
| `git-org-name` = `MCEngine` | `gradle.properties` | The GitHub owner. The Maven group is derived from it, lowercased: `io.github.mcengine`. |
| `git-repository-name` = `universal-template` | `gradle.properties` | The GitHub repository. Used for the repository URL, not for the package. |
| `namespaceSegment` = `universal` | top of the root `build.gradle` | Also appears in every package statement and directory path. |
| `pluginIdValue` = `Template` | top of the root `build.gradle` | Also appears in every entry-point class name. |
| `project-version` = `0.0.0` | `gradle.properties` | Permanent — see below. |

Modules read `namespace`, `pluginId` and `commandName` from `allprojects.ext`, so the plugin
descriptor's `main:`, the jar base names, the mod ids and the `/template` command are all
generated. Nothing spells a package out twice.

The two source-facing values sit at the top of one build file, next to each other, so the
rename is one guided pass rather than a properties edit plus an unguided source sweep.
`PROMPT.md` performs that pass.

## Consequence

* The namespace is `io.github.mcengine.universal` — not `…universaltemplate`. The repository
  name and the package segment are deliberately different things.
* **The version stays `0.0.0` forever.** This is a template; there is nothing to release, so
  no changelog entry ever bumps it. A fork chooses its own starting version through
  `PROMPT.md`.
* Adding a new renameable identifier means deciding, first, whether the build can derive it.
  If it also lives in Java source, it belongs beside the other two in `build.gradle`, not in
  `gradle.properties`.
* The Minecraft target is a separate decision — see
  [`minecraft-target-version.md`](minecraft-target-version.md).
