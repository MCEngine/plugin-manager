package io.github.mcengine.universal.bukkit.papermc;

import io.github.mcengine.universal.bukkit.core.AbstractTemplatePlugin;
import io.github.mcengine.universal.bukkit.core.scheduler.BukkitPlatformScheduler;
import io.github.mcengine.universal.bukkit.core.scheduler.PlatformScheduler;

/**
 * PaperMC entry point.
 *
 * <p>Paper still ticks the world on one thread, so it uses the same scheduler as
 * SpigotMC. The module exists separately because it compiles against the Paper
 * API, which is where any Paper-only feature would go.</p>
 */
public class TemplatePaperMC extends AbstractTemplatePlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    protected PlatformScheduler createScheduler() {
        return new BukkitPlatformScheduler(this);
    }
}
