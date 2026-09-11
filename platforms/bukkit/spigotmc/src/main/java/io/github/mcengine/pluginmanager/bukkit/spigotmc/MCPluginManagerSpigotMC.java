package io.github.mcengine.pluginmanager.bukkit.spigotmc;

import io.github.mcengine.pluginmanager.bukkit.core.AbstractMCPluginManagerPlugin;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.BukkitPlatformScheduler;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.PlatformScheduler;

/**
 * SpigotMC entry point.
 *
 * <p>Installs the classic Bukkit scheduler and inherits everything else from
 * {@link AbstractMCPluginManagerPlugin}. If this class ever grows past its scheduler
 * choice, the logic being added belongs in the core module instead — otherwise
 * three copies of it now exist.</p>
 */
public class MCPluginManagerSpigotMC extends AbstractMCPluginManagerPlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected PlatformScheduler createScheduler() {
        return new BukkitPlatformScheduler(this);
    }
}
