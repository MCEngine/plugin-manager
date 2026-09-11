package io.github.mcengine.pluginmanager.bukkit.core;

import io.github.mcengine.pluginmanager.MCPluginManagerProvider;
import io.github.mcengine.pluginmanager.bukkit.core.commands.MCPluginManagerCommand;
import io.github.mcengine.pluginmanager.bukkit.core.listeners.MCPluginManagerJoinListener;
import io.github.mcengine.pluginmanager.bukkit.core.manager.ManagerConfig;
import io.github.mcengine.pluginmanager.bukkit.core.manager.ManagerRuntime;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.PlatformScheduler;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.Schedulers;
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
public abstract class AbstractMCPluginManagerPlugin extends JavaPlugin {

    /**
     * The manager, built while the plugin enables and stopped while it disables.
     */
    private ManagerRuntime manager;

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

        MCPluginManagerProvider provider = MCPluginManagerProvider.create();

        // The service is brought up asynchronously because a real implementation
        // will reach storage here. Registration waits for it rather than racing
        // it, so no player can run the command against a service that is not up.
        provider.initialize().whenComplete((ignored, error) -> {
            if (error != null) {
                getLogger().severe("Failed to start the service: " + error.getMessage() + ". Disabling.");
                Schedulers.get().runGlobal(() -> getServer().getPluginManager().disablePlugin(this));
                return;
            }
            Schedulers.get().runGlobal(this::registerHandlers);
        });
    }

    /**
     * Registers the command and the listener, on the main thread.
     *
     * <p>Bukkit's command map and event registry are not safe to touch from an
     * arbitrary thread, so this never runs directly from the future's callback.
     * It goes through the platform scheduler rather than {@code Bukkit.getScheduler()},
     * which throws on Folia.</p>
     */
    private void registerHandlers() {
        // The command name is the plugin name lowercased, and plugin.yml declares
        // both from the same `pluginid` property. Reading it back this way means
        // renaming a fork does not leave a hardcoded string behind here.
        manager = new ManagerRuntime(this, ManagerConfig.from(getConfig(), getLogger()));

        MCPluginManagerCommand executor = new MCPluginManagerCommand(manager);
        var command = getCommand(getName().toLowerCase(Locale.ROOT));
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        getServer().getPluginManager().registerEvents(new MCPluginManagerJoinListener(), this);

        manager.start();
        getLogger().info("Enabled.");
    }

    /**
     * Shuts the service down and releases the scheduler, so a reload does not
     * leave one bound to the previous plugin instance.
     */
    @Override
    public void onDisable() {
        // Stopped first: shutdown is the only moment a loaded jar can be
        // deleted, and this is where pending removals are performed.
        if (manager != null) {
            manager.stop();
            manager = null;
        }

        if (MCPluginManagerProvider.isReady()) {
            MCPluginManagerProvider.instance.shutdown();
            MCPluginManagerProvider.instance = null;
        }
        Schedulers.clear();
        getLogger().info("Disabled.");
    }
}
