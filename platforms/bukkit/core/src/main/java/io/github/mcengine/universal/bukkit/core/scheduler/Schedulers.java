package io.github.mcengine.universal.bukkit.core.scheduler;

import org.bukkit.entity.Entity;

import java.util.Objects;

/**
 * Holds the scheduler the running platform installed, so shared code can
 * schedule work without being handed a scheduler at every call site.
 *
 * <p>Set exactly once, while the plugin enables, before anything can schedule.</p>
 */
public final class Schedulers {

    /**
     * The installed scheduler.
     *
     * <p>Volatile because it is written on the main thread during enable and read
     * from asynchronous tasks and, on Folia, from region threads.</p>
     */
    private static volatile PlatformScheduler scheduler;

    /**
     * Not instantiable.
     */
    private Schedulers() {
    }

    /**
     * Installs the platform's scheduler.
     *
     * @param platformScheduler The scheduler to install.
     */
    public static void set(PlatformScheduler platformScheduler) {
        scheduler = Objects.requireNonNull(platformScheduler, "platformScheduler cannot be null");
    }

    /**
     * The installed scheduler.
     *
     * @return The scheduler.
     * @throws IllegalStateException When called before the plugin enabled, which
     *         is a programming error rather than a runtime condition.
     */
    public static PlatformScheduler get() {
        PlatformScheduler current = scheduler;
        if (current == null) {
            throw new IllegalStateException(
                "No scheduler installed. Schedulers.set(...) runs during onEnable.");
        }
        return current;
    }

    /**
     * Clears the installed scheduler. Called while the plugin disables so a
     * reload does not keep a scheduler bound to the previous plugin instance.
     */
    public static void clear() {
        scheduler = null;
    }

    /**
     * Convenience for {@link PlatformScheduler#runForEntity}.
     *
     * @param entity The entity whose thread should run the task.
     * @param task The work to run.
     * @param retired Invoked instead when the entity no longer exists.
     */
    public static void runForEntity(Entity entity, Runnable task, Runnable retired) {
        get().runForEntity(entity, task, retired);
    }

    /**
     * Convenience for {@link PlatformScheduler#runAsync}.
     *
     * @param task The work to run.
     */
    public static void runAsync(Runnable task) {
        get().runAsync(task);
    }
}
