package io.github.mcengine.pluginmanager.bukkit.foliamc;

import io.github.mcengine.pluginmanager.bukkit.core.AbstractMCPluginManagerPlugin;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.PlatformScheduler;
import io.github.mcengine.pluginmanager.bukkit.foliamc.scheduler.FoliaPlatformScheduler;

/**
 * Folia entry point.
 *
 * <p>Installs the region-aware scheduler and inherits everything else from
 * {@link AbstractMCPluginManagerPlugin}.</p>
 */
public class MCPluginManagerFoliaMC extends AbstractMCPluginManagerPlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected PlatformScheduler createScheduler() {
        return new FoliaPlatformScheduler(this);
    }
}
