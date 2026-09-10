# PROMPT.md — set this fork up, then delete this file

You are an agent working in a fresh fork of `MCEngine/universal-template`. This file is the
one-time setup procedure. **It runs once, and the last step deletes it**, because a setup
prompt left behind in someone's repository is clutter that later readers mistake for
instructions.

If this file is missing, setup already happened. Nothing here needs doing again.

---

## 0. Before you start

Read `AGENTS.md` first, and resolve the shared instruction set through the
`lxagents-agents-base` connector as it describes. This procedure is subject to the same
rules as any other work in this repository: one branch per task, the commit conventions, and
**the plan gate — present the plan and wait for approval before writing any file**.

Do the whole rename on a branch named `chore/template-setup`.

---

## 1. Ask, in one message

Ask all of the following at once. Do not guess, and do not start renaming before the answers
are in — a half-applied rename is worse than none, because the build still runs and the
names disagree.

| # | Ask for | Notes |
|---|---|---|
| 1 | **Plugin ID** | The name in class and jar names, e.g. `MyPlugin` → `MyPluginEngine-1.0.0.jar`. Upper camel case, letters and digits only. Its lower case form becomes the in-game command and the mod ids. |
| 2 | **Starting version** | e.g. `0.1.0` or `1.0.0`. This template sits at `0.0.0`; a fork picks its own. |
| 3 | **GitHub organization and repository** | e.g. `AcmeMC` and `acme-identity`. Used verbatim for URLs, and the organization lower cased becomes the Maven group. |
| 4 | **License** | Type (MIT, Apache-2.0, GPL-3.0, BSD-3-Clause, proprietary, none), plus the copyright holder and year. Do not choose one for them — a LICENSE file is a legal statement. |
| 5 | **Group and namespace** | Offer the derived defaults — group `io.github.<org lowercased>`, namespace `<group>.<segment>` — and let them override either. The namespace segment must be a valid Java identifier: lower case, no hyphens. It does **not** have to match the repository name. |
| 6 | **What the project is** | A paragraph or two: what it does, who it is for, notable features, anything a newcomer needs. This becomes `README.md` and the `wiki/` pages. Without it you will write filler; say so and ask again rather than inventing. |
| 7 | **Custom logic in `AGENTS.md`?** | Whether they want repository-specific rules or trigger rows added — for their domain, their review conventions, their deployment. If yes, ask what; if no, leave the mirrored table alone. |

If they decline question 6, say plainly that you will leave the documentation describing the
template rather than their project, and that it will need writing later.

---

## 2. What to change, and where

### 2.1 The build — two files

**`gradle.properties`**

| Key | Set to |
|---|---|
| `git-org-name` | Their organization, exact case. The Maven group derives from this, lower cased. |
| `git-repository-name` | Their repository, exact case. Used for the issue-tracker URL. |
| `project-version` | Their starting version. |

The Minecraft and loader versions are already consistent with each other. Change them only
if asked, and read *Retargeting the Minecraft version* in
[`wiki/environments/setup.md`](wiki/environments/setup.md) first — not every release can be
targeted.

**Root `build.gradle`**, near the top:

```groovy
def namespaceSegment = 'universal'
def pluginIdValue = 'Template'
```

Set both. Everything else in the build reads `namespace`, `pluginId`, `commandName`,
`author` and `issueTrackerUrl` from these, so **no other build file needs editing**.

### 2.2 The Java tree

This is the part no build system can do for you.

1. **Package directories.** Every module has
   `src/{main,test}/java/io/github/mcengine/universal/…`. Rename both the `mcengine` and the
   `universal` segments to match the new group and namespace. Use `git mv` so history
   follows. There are 19 such directories:

   ```
   api/src/main/java/…                       common/src/{main,test}/java/…
   platforms/bukkit/core/src/main/java/…      platforms/bukkit/engine/src/main/java/…
   platforms/bukkit/{spigotmc,papermc,foliamc}/src/{main,test}/java/…
   platforms/mods/core/src/{main,test}/java/…
   platforms/mods/{fabric,forge,neoforge}/{client,server}/src/main/java/…
   ```

2. **`package` and `import` statements.** Replace the old namespace everywhere, including
   javadoc `{@link}` references and fully qualified names inside comments.

3. **Class names.** Rename every `Template*` and `AbstractTemplate*` type to the new plugin
   id, and their files with them:

   ```
   TemplateProvider          TemplateService          AbstractTemplateService
   TemplateAction            TemplateRequest          TemplateResponse
   DefaultTemplateService    AbstractTemplatePlugin   TemplateCommand
   TemplateJoinListener      TemplateSpigotMC         TemplatePaperMC
   TemplateFoliaMC           TemplateEngine           TemplateChannel
   TemplatePayloadCodec      TemplatePayloads         TemplateFabricClient
   TemplateFabricServer      TemplateForgeClient      TemplateForgeServer
   TemplateNeoForgeClient    TemplateNeoForgeServer
   ```

   And the tests: `TemplateProviderTest`, `TemplateRequestTest`,
   `TemplatePayloadCodecTest`, and `Template{SpigotMC,PaperMC,FoliaMC}StructureTest`.

4. **Three string literals that the build cannot reach**, because they are Java annotations
   and constants:

   | Where | Currently | Set to |
   |---|---|---|
   | `platforms/mods/core/…/TemplateChannel.java` | `NAMESPACE = "template"` | the plugin id, lower cased |
   | `platforms/mods/{forge,neoforge}/client/…` | `@Mod("template_client")` | `<pluginid lowercased>_client` |
   | `platforms/mods/{forge,neoforge}/server/…` | `@Mod("template_server")` | `<pluginid lowercased>_server` |

   These must match what the build generates, or the mods will not load. The Fabric side
   reads its ids from the generated descriptor and needs no edit.

**Nothing else in the descriptors needs touching.** `plugin.yml`, `fabric.mod.json`,
`mods.toml` and `neoforge.mods.toml` are all filtered through `processResources` — the mod
ids, entry-point class names, author, issue tracker and versions are generated.

### 2.3 Documentation

* **`LICENSE`** — replace with the full, exact text of the chosen license, holder and year
  filled in. If they declined, write `# License` and nothing else. Never invent one.
* **`README.md`** — rewrite from answer 6. Keep it an overview: what the project is, quick
  start, links into `wiki/`, the license line. No detailed docs here.
* **`wiki/information/overview.md`** — what the project is, from answer 6.
* **`wiki/information/architecture.md`** — keep the structural explanation, which is still
  true, and replace the template-specific framing with theirs.
* **`wiki/environments/setup.md`** — update names and commands.
* **`wiki/logs/`** — the template logs by date, `{yyyy}/{mm}/{dd}/`, because its version is
  fixed. **A fork releases, so switch it back to versions.** Delete the dated directories and
  create `{Major}/{Minor}/{Patch}/CHANGELOG.md` for their starting version, describing the
  project's initial state. Rewrite `.agents/index/logs-index.md` to match — including its
  note that creating a directory is ungated, which stops being true once the directory is a
  version claim again.
* **`AGENTS.md`** — rewrite the opening paragraph to describe their project. Leave the
  mirrored trigger table alone unless answer 7 asked for rows; a mirrored row is never
  repointed at a local file, and a local override is declared in the root index's override
  table instead.
* **`.agents/rules/repository.md`**, **`.agents/wiki/context/repository-map.md`** — update
  names, and drop the template-only rules (the ones about this file, and about the version
  being fixed).
* **`.agents/memory/`** — clear the template's task record and decisions; they describe
  building the template, not their project. Keep `state/repository-state.md`, rewritten to
  describe the fork. Update `.agents/index/memory-index.md` to match.

### 2.4 The version rule changes

This template pins `project-version` at `0.0.0` permanently, and
`.agents/rules/repository.md` says so. **That rule does not apply to a fork** — a real
project releases. Remove it, and leave the ordinary shared rule that a version is never
bumped without the user asking.

---

## 3. Verify before you finish

```bash
./gradlew clean build --warning-mode all     # green, zero deprecations, all tests pass
./gradlew -Pmods=true build                  # the Fabric and NeoForge jars; slow on first run
```

The two Forge modules sit behind an extra `-Pforge=true` and do not currently build; see
*Forge needs a second flag* in [`wiki/environments/setup.md`](wiki/environments/setup.md).
If the fork does not target Forge, deleting `platforms/mods/forge/` is clean — nothing else
depends on it.

Then check:

* `grep -ri template .` finds nothing outside genuine prose — no stale class name, package
  segment, mod id, or channel namespace.
* `build/libs/` holds `<PluginId>Engine-<version>.jar` and the six mod jars.
* `unzip -p build/libs/<PluginId>Engine-<version>.jar plugin.yml` shows the new `main:`,
  `name:`, `author:` and command.
* `grep -r "INDEX.md"` finds nothing, every `.agents/` file still has valid frontmatter, and
  every index row points at a file that exists.

---

## 4. Delete this file

Once everything above is done and the build is green:

1. Delete `PROMPT.md`.
2. Remove its row from the trigger table in `AGENTS.md`.
3. Commit the deletion **with the setup work**, not as a follow-up — the setup and the
   removal of its instructions are one change.

This file has no second use. A fork that keeps it around will one day have an agent read it
and try to rename an already-renamed project.
