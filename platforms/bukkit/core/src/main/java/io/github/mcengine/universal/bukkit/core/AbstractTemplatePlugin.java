package io.github.mcengine.universal.bukkit.core;

import io.github.mcengine.universal.TemplateProvider;
import io.github.mcengine.universal.bukkit.core.commands.TemplateCommand;
import io.github.mcengine.universal.bukkit.core.listeners.TemplateJoinListener;
import io.github.mcengine.universal.bukkit.core.scheduler.PlatformScheduler;
import io.github.mcengine.universal.bukkit.core.scheduler.Schedulers;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * The shared bootstrap behind every Bukkit entry point.
 *
 * <p>Enabling, disabling, config loading, command and listener registration, and
 * bringing the service up are identical on SpigotMC, PaperMC, and Folia. Each
 * platform module supplies only its scheduler, through {@link #createScheduler()},
 * and extends this class with a concrete entry point of a few lines.</p>
 *
 * <p>That is the whole reason the platform modules are as small as they are: the
 * moment platform-specific logic starts appearing in the subclasses, three copies
 * of it exist and two of them are about to drift.</p>
 */
public abstract class AbstractTemplatePlugin extends JavaPlugin {

    /**
     * Supplies the scheduler installed while the plugin enables.
     *
     * @return The scheduler implementation for this platform.
     */
    protected abstract PlatformScheduler createScheduler();

    /**
     * Boots the plugin: installs the scheduler, reads config, brings the service
     * up, and registers the command and listener.
     */
    @Override
    public void onEnable() {
        Schedulers.set(createScheduler());
        saveDefaultConfig();

        if (!getConfig().getBoolean("enable", true)) {
            getLogger().warning("Disabled in config.yml. Set enable: true to turn the plugin on.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        TemplateProvider provider = TemplateProvider.create();

        // The service is brought up asynchronously because a real implementation
        // will reach storage here. Registration waits for it rather than racing
        // it, so no player can run the command against a service that is not up.
        provider.initialize().whenComplete((ignored, error) -> {
            if (error != null) {
                getLogger().severe("Failed to start the service: " + error.getMessage() + ". Disabling.");
                getServer().getScheduler().runTask(this, () ->
                    getServer().getPluginManager().disablePlugin(this));
                return;
            }
            getServer().getScheduler().runTask(this, this::registerHandlers);
        });
    }

    /**
     * Registers the command and the listener, on the main thread.
     *
     * <p>Bukkit's command map and event registry are not safe to touch from an
     * arbitrary thread, so this never runs directly from the future's callback.</p>
     */
    private void registerHandlers() {
        // The command name is the plugin name lowercased, and plugin.yml declares
        // both from the same `pluginid` property. Reading it back this way means
        // renaming a fork does not leave a hardcoded string behind here.
        getCommand(getName().toLowerCase(Locale.ROOT)).setExecutor(new TemplateCommand());
        getServer().getPluginManager().registerEvents(new TemplateJoinListener(), this);
        getLogger().info("Enabled.");
    }

    /**
     * Shuts the service down and releases the scheduler, so a reload does not
     * leave one bound to the previous plugin instance.
     */
    @Override
    public void onDisable() {
        if (TemplateProvider.isReady()) {
            TemplateProvider.instance.shutdown();
            TemplateProvider.instance = null;
        }
        Schedulers.clear();
        getLogger().info("Disabled.");
    }
}
