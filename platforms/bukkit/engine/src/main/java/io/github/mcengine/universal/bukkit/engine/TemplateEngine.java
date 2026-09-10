package io.github.mcengine.universal.bukkit.engine;

import io.github.mcengine.universal.bukkit.core.AbstractTemplatePlugin;
import io.github.mcengine.universal.bukkit.core.scheduler.BukkitPlatformScheduler;
import io.github.mcengine.universal.bukkit.core.scheduler.PlatformScheduler;
import io.github.mcengine.universal.bukkit.foliamc.scheduler.FoliaPlatformScheduler;

/**
 * The universal entry point: one jar for SpigotMC, PaperMC, and Folia.
 *
 * <p>The running server is detected while the plugin enables and the matching
 * scheduler is installed, so the same file behaves correctly everywhere and
 * server owners have one download instead of three.</p>
 *
 * <p>Detection is done by probing for classes rather than by compiling against
 * all three APIs, which is not possible: Spigot, Paper, and Folia each provide
 * the same Gradle capability, so declaring them together is a dependency
 * conflict. This module therefore compiles against the Folia API alone — Folia
 * is a superset of the other two — and decides at runtime.</p>
 */
public class TemplateEngine extends AbstractTemplatePlugin {

    /**
     * The server platforms this jar can run on.
     */
    public enum ServerPlatform {

        /**
         * Folia, with its regionised multi-threaded schedulers.
         */
        FOLIA,

        /**
         * PaperMC and its downstream forks.
         */
        PAPER,

        /**
         * SpigotMC and its downstream forks. The fallback.
         */
        SPIGOT
    }

    /**
     * Detects which server this jar is running on.
     *
     * <p>Folia is probed first because its regionised server class exists
     * nowhere else, then Paper, whose config class is absent on plain Spigot.
     * Spigot is the fallback: it is the smallest API of the three, so guessing
     * it wrongly degrades rather than breaks.</p>
     *
     * @return The detected platform.
     */
    protected ServerPlatform detectPlatform() {
        if (classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
            return ServerPlatform.FOLIA;
        }
        if (classExists("com.destroystokyo.paper.PaperConfig")) {
            return ServerPlatform.PAPER;
        }
        return ServerPlatform.SPIGOT;
    }

    /**
     * Whether a class is present on the runtime classpath.
     *
     * @param className The fully qualified class name to probe.
     * @return True when the class can be loaded.
     */
    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PlatformScheduler createScheduler() {
        ServerPlatform platform = detectPlatform();
        return switch (platform) {
            case FOLIA -> {
                getLogger().info("Detected Folia. Installing the region-aware scheduler.");
                yield new FoliaPlatformScheduler(this);
            }
            case PAPER -> {
                getLogger().info("Detected Paper. Installing the Bukkit scheduler.");
                yield new BukkitPlatformScheduler(this);
            }
            case SPIGOT -> {
                getLogger().info("Detected Spigot. Installing the Bukkit scheduler.");
                yield new BukkitPlatformScheduler(this);
            }
        };
    }
}
