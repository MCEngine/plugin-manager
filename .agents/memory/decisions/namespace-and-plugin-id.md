---
name: memory-decisions-namespace-and-plugin-id
description: The namespace, plugin id, command and mod ids chosen when the template was renamed to MCPluginManager, and what each one reaches.
---

# Namespace and plugin id

Chosen by the user before task 4, from three options put to them.

| Value | Setting | Where it lives |
|---|---|---|
| `pluginmanager` | `namespaceSegment` | root `build.gradle` |
| `MCPluginManager` | `pluginIdValue` | root `build.gradle` |
| `io.github.mcengine.pluginmanager` | derived `namespace` | root `build.gradle`, from `git-org-name` |
| `MCEngine` | `git-org-name` | `gradle.properties` |
| `plugin-manager` | `git-repository-name` | `gradle.properties` |
| `0.0.0` | `project-version` | `gradle.properties` |

Derived from those, with no second place spelling them out:

* Jar — `MCPluginManagerEngine-0.0.0.jar`
* Command — `/mcpluginmanager`, alias `/mcpm`
* Mod ids — `mcpluginmanager_client`, `mcpluginmanager_server`
* Channel namespace — `mcpluginmanager`

## Why not the alternatives

`io.github.mcengine.mcpluginmanager` repeats `mc` twice in one coordinate, for a segment
that already sits under an org named `mcengine`.

Keeping `universal` would have renamed the classes but left a package segment that says
nothing about the project — the exact drift `PROMPT.md` existed to prevent.

## What this reaches

The three string literals the build cannot generate, because they are Java annotations and
constants, are the ones to check after any future rename:

| Where | Value |
|---|---|
| `platforms/mods/core/…/MCPluginManagerChannel.java` | `NAMESPACE = "mcpluginmanager"` |
| `platforms/mods/{forge,neoforge}/client/…` | `@Mod("mcpluginmanager_client")` |
| `platforms/mods/{forge,neoforge}/server/…` | `@Mod("mcpluginmanager_server")` |

Everything else — `plugin.yml`, `fabric.mod.json`, `mods.toml`, `neoforge.mods.toml` — is
filtered through `processResources` and needs no edit.
