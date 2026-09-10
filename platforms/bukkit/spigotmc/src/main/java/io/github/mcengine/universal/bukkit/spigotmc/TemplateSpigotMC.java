package io.github.mcengine.universal.bukkit.spigotmc;

import io.github.mcengine.universal.bukkit.core.AbstractTemplatePlugin;
import io.github.mcengine.universal.bukkit.core.scheduler.BukkitPlatformScheduler;
import io.github.mcengine.universal.bukkit.core.scheduler.PlatformScheduler;

/**
 * SpigotMC entry point.
 *
 * <p>Installs the classic Bukkit scheduler and inherits everything else from
 * {@link AbstractTemplatePlugin}. If this class ever grows past its scheduler
 * choice, the logic being added belongs in the core module instead — otherwise
 * three copies of it now exist.</p>
 */
public class TemplateSpigotMC extends AbstractTemplatePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected PlatformScheduler createScheduler() {
        return new BukkitPlatformScheduler(this);
    }
}
