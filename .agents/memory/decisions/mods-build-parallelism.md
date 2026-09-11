---
name: memory-decisions-mods-build-parallelism
description: Why -Pmods=true deadlocks under Gradle's parallel execution, and how the build was changed so it stops.
---

# The mod build deadlocks under parallel execution

## What happens

`./gradlew -Pmods=true build` hangs indefinitely with the last log line:

```
> Task :platforms:mods:neoforge:server:createMinecraftArtifacts
*** Started working on decompile
Waiting for lock on decompile_<hash>...........
```

It is not slow work. The decompiling JVM's accumulated CPU time stops advancing while the
process stays resident, which is what distinguishes a deadlock from Vineflower simply taking
a long time on Minecraft — and it does take a long time, which is why the two look alike.

## Why

`gradle.properties` sets `org.gradle.parallel=true`. Under it, `:platforms:mods:neoforge:client`
and `:platforms:mods:neoforge:server` configure at the same time, and both call
NeoFormRuntime's `createMinecraftArtifacts`. NFRT caches its decompile output in a
**shared** directory under `~/.gradle/caches/neoformruntime/` and guards each step with a
lock file. Two Gradle workers in one build reach the same lock, and the wait is not resolved.

Killing the build leaves the `*.lock` files behind, so the next run waits on a lock no
process holds. **After a deadlock, delete `~/.gradle/caches/neoformruntime/*.lock` before
retrying** or the second run hangs immediately for a different reason than the first.

## The fix

`settings.gradle` turns parallel project execution off **inside the `-Pmods=true` branch
only**, so the flag never has to be remembered. The Bukkit side is six modules that
parallelize cleanly and keep doing so; the mod side is the only place NFRT is involved, and
it is the only place that gives the parallelism up.

`./gradlew -Pmods=true build --no-parallel` is the equivalent by hand, and is what was used
to verify the diagnosis before the settings change landed.

## What is not the cause

Not the MCPluginManager rename — the Fabric client and server modules remapped successfully
in the same run, before the NeoForge modules reached their lock. Not the Maven Central `429`
seen on an earlier attempt either; that was transient rate limiting and succeeded on retry.
