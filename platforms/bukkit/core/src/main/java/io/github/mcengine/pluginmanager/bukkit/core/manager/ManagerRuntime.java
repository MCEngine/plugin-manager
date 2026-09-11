package io.github.mcengine.pluginmanager.bukkit.core.manager;

import io.github.mcengine.pluginmanager.api.manager.CentralServer;
import io.github.mcengine.pluginmanager.api.manager.DesiredState;
import io.github.mcengine.pluginmanager.common.manager.HttpManagerClient;
import io.github.mcengine.pluginmanager.common.manager.ManagerOutcome;
import io.github.mcengine.pluginmanager.common.manager.ManagerService;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.PlatformTask;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Holds the manager while the plugin is enabled, and drives the poll loop.
 *
 * <p>Every network call and every file write runs off the server thread, through
 * {@link Schedulers}. That is what keeps Folia correct: it has no main thread to
 * block, and blocking a region thread stalls a slice of the world rather than
 * the whole server, which is harder to notice and no better.</p>
 */
public final class ManagerRuntime {

    private final Plugin plugin;
    private final ManagerConfig config;
    private final List<ManagerService> services = new ArrayList<>();
    private final AtomicReference<PlatformTask> poller = new AtomicReference<>();

    public ManagerRuntime(Plugin plugin, ManagerConfig config) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = Objects.requireNonNull(config, "config");

        Path plugins = plugin.getDataFolder().getParentFile().toPath();
        for (CentralServer server : config.servers()) {
            services.add(new ManagerService(
                new HttpManagerClient(server), plugins, config.serverId()));
        }
    }

    public ManagerConfig config() {
        return config;
    }

    public List<ManagerService> services() {
        return List.copyOf(services);
    }

    /** Starts the poll loop, unless this server has nothing to talk to. */
    public void start() {
        Logger log = plugin.getLogger();

        if (config.servers().isEmpty()) {
            log.info("No central server is configured, so nothing will be managed. "
                + "Add one to config.yml.");
            return;
        }
        if (config.serverId().isBlank()) {
            log.info("This server is not registered yet. Run /"
                + plugin.getName().toLowerCase(java.util.Locale.ROOT) + " register <name>.");
            return;
        }

        long period = config.pollSeconds() * 1000L;
        // The first run is delayed rather than immediate: a server that has just
        // started is doing several other things, and one poll can wait.
        PlatformTask task = Schedulers.get().runAsyncAtFixedRate(this::pollOnce, 10_000L, period);
        poller.set(task);
        log.info("Managing plugins against " + config.servers().size()
            + " central server(s), every " + config.pollSeconds() + "s.");
    }

    /** Stops the poll loop and performs any deletions that were pending. */
    public void stop() {
        PlatformTask task = poller.getAndSet(null);
        if (task != null) {
            task.cancel();
        }

        // Shutdown is the only moment a loaded jar can be deleted -- on Windows
        // it cannot be while the server runs.
        for (ManagerService service : services) {
            try {
                List<String> deleted = service.staging().applyPendingDeletions();
                if (!deleted.isEmpty()) {
                    plugin.getLogger().info("Removed " + String.join(", ", deleted) + ".");
                }
            } catch (IOException failure) {
                plugin.getLogger().warning(
                    "Could not remove a pending plugin: " + failure.getMessage());
            }
        }
    }

    /**
     * One pass: report, ask, and apply when configured to.
     *
     * <p>Already off the server thread when the poll loop calls it.</p>
     */
    public void pollOnce() {
        Logger log = plugin.getLogger();
        for (ManagerService service : services) {
            try {
                if (config.reportInventory()) {
                    service.report(platform(), minecraftVersion(), plugin.getDescription().getVersion());
                }

                DesiredState state = service.check();
                if (state.isEmpty()) {
                    continue;
                }

                if (!config.autoApply()) {
                    log.info(state.changes().size()
                        + " change(s) are pending. Run the apply subcommand to perform them.");
                    continue;
                }

                for (ManagerOutcome outcome : service.apply(state)) {
                    if (outcome.staged()) {
                        log.info(outcome.message());
                    } else {
                        log.warning(outcome.message());
                    }
                }
            } catch (IOException failure) {
                // One unreachable catalogue must not stop the others.
                log.warning("Could not reach a central server: " + failure.getMessage());
            }
        }
    }

    /**
     * Which Bukkit derivative is running.
     *
     * <p>Probed by class rather than by parsing a version string, which is the
     * same test the universal engine jar uses to pick a scheduler.</p>
     */
    public static String platform() {
        if (hasClass("io.papermc.paper.threadedregions.RegionizedServer")) {
            return "folia";
        }
        if (hasClass("com.destroystokyo.paper.PaperConfig")
            || hasClass("io.papermc.paper.configuration.Configuration")) {
            return "paper";
        }
        return "spigot";
    }

    private static String minecraftVersion() {
        String version = Bukkit.getBukkitVersion();
        int dash = version.indexOf('-');
        return dash < 0 ? version : version.substring(0, dash);
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException absent) {
            return false;
        }
    }
}
