package io.github.mcengine.universal.bukkit.foliamc.scheduler;

import io.github.mcengine.universal.bukkit.core.scheduler.PlatformScheduler;
import io.github.mcengine.universal.bukkit.core.scheduler.PlatformTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * The region-aware scheduler Folia requires.
 *
 * <p>Folia splits the world across threads, so work touching an entity must run
 * on the thread that currently owns it — and that entity may be gone by the time
 * the work is due, which is what the {@code retired} callback is for.</p>
 *
 * <p>Folia's async scheduler takes real durations, so unlike the Bukkit
 * implementation this one passes the millisecond values straight through with no
 * tick conversion.</p>
 */
public final class FoliaPlatformScheduler implements PlatformScheduler {

    /**
     * The plugin every scheduled task is registered against.
     */
    private final Plugin plugin;

    /**
     * @param plugin The owning plugin.
     */
    public FoliaPlatformScheduler(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    /**
     * Runs the task on the thread that currently owns the entity.
     *
     * @param entity The entity whose thread should run the task.
     * @param task The work to run.
     * @param retired Invoked instead when the entity no longer exists.
     */
    @Override
    public void runForEntity(Entity entity, Runnable task, Runnable retired) {
        entity.getScheduler().run(plugin, scheduledTask -> task.run(), retired);
    }

    /**
     * Runs the task on Folia's async scheduler.
     *
     * @param task The work to run.
     */
    @Override
    public void runAsync(Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    /**
     * Runs the task repeatedly on Folia's async scheduler.
     *
     * @param task The work to run.
     * @param initialDelayMillis Delay before the first run, in milliseconds.
     * @param periodMillis Delay between runs, in milliseconds.
     * @return A handle that cancels the task.
     */
    @Override
    public PlatformTask runAsyncAtFixedRate(Runnable task, long initialDelayMillis, long periodMillis) {
        ScheduledTask handle = plugin.getServer().getAsyncScheduler().runAtFixedRate(
            plugin, scheduledTask -> task.run(), initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
        return handle::cancel;
    }
}
