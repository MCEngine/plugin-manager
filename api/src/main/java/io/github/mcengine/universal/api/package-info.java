/**
 * The shared contract every platform in this project implements or consumes.
 *
 * <p>This package is deliberately restricted to interfaces, records, enums, and
 * abstract classes, and it depends on nothing outside the JDK — not Bukkit, not
 * any mod loader, not the implementation in {@code common}. That restriction is
 * what lets the Bukkit plugin and the Forge, Fabric, and NeoForge mods agree on
 * one set of types instead of three that drift.</p>
 *
 * <p>Anything that can fail, block, or touch storage returns a
 * {@link java.util.concurrent.CompletableFuture}. Only lookups answered from
 * memory are synchronous, so a caller can tell the two apart from the signature
 * alone.</p>
 */
package io.github.mcengine.universal.api;
