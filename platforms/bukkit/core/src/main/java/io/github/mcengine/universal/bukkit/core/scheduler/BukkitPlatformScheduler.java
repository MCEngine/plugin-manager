package io.github.mcengine.universal.bukkit.core.scheduler;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

/**
 * The classic Bukkit scheduler, used by SpigotMC and PaperMC.
 *
 * <p>These servers tick the whole world on one thread, so "the thread that owns
 * an entity" is always the main thread and the {@code retired} callback is never
 * needed.</p>
 */
public final class BukkitPlatformScheduler implements PlatformScheduler {

    /**
     * Bukkit counts delays in ticks, and a tick is 50 milliseconds.
     */
    private static final long MILLIS_PER_TICK = 50L;

    /**
     * The plugin every scheduled task is registered against.
     */
    private final Plugin plugin;

    /**
     * @param plugin The owning plugin.
     */
    public BukkitPlatformScheduler(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    /**
     * Runs the task on the main thread, which owns every entity here.
     *
     * @param entity The entity whose thread should run the task.
     * @param task The work to run.
     * @param retired Never invoked on a single-threaded server.
     */
    @Override
    public void runForEntity(Entity entity, Runnable task, Runnable retired) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    /**
     * Runs the task on Bukkit's async pool.
     *
     * @param task The work to run.
     */
    @Override
    public void runAsync(Runnable task) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Converts the millisecond delays to ticks, never rounding down to zero,
     * which Bukkit rejects.
     *
     * @param task The work to run.
     * @param initialDelayMillis Delay before the first run, in milliseconds.
     * @param periodMillis Delay between runs, in milliseconds.
     * @return A handle that cancels the task.
     */
    @Override
    public PlatformTask runAsyncAtFixedRate(Runnable task, long initialDelayMillis, long periodMillis) {
        long delayTicks = Math.max(1L, initialDelayMillis / MILLIS_PER_TICK);
        long periodTicks = Math.max(1L, periodMillis / MILLIS_PER_TICK);
        BukkitTask handle = plugin.getServer().getScheduler()
            .runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        return handle::cancel;
    }
}
