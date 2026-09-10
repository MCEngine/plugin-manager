package io.github.mcengine.universal.bukkit.core.scheduler;

import org.bukkit.entity.Entity;

/**
 * The scheduling operations shared code needs, in a shape every Bukkit
 * derivative can satisfy.
 *
 * <p>This abstraction exists because the shared code compiles against the plain
 * {@code spigot-api} and therefore cannot name Paper's {@code AsyncScheduler} or
 * Folia's region and entity schedulers directly. Each platform module supplies
 * an implementation; shared code only ever sees this interface.</p>
 *
 * <p>Folia is the reason the signatures look the way they do. On Folia an entity
 * can be moved to another region thread, or removed entirely, between scheduling
 * work and running it — so {@link #runForEntity} takes a {@code retired} callback
 * that single-threaded platforms simply never invoke. Designing for the strictest
 * platform means the others cost nothing.</p>
 */
public interface PlatformScheduler {

    /**
     * Runs a task on the thread that owns an entity.
     *
     * @param entity The entity whose thread should run the task.
     * @param task The work to run.
     * @param retired Invoked instead when the entity no longer exists. On
     *                single-threaded platforms this never happens.
     */
    void runForEntity(Entity entity, Runnable task, Runnable retired);

    /**
     * Runs a task off the main thread.
     *
     * @param task The work to run.
     */
    void runAsync(Runnable task);

    /**
     * Runs a task off the main thread, repeatedly.
     *
     * @param task The work to run.
     * @param initialDelayMillis Delay before the first run, in milliseconds.
     * @param periodMillis Delay between runs, in milliseconds.
     * @return A handle that cancels the task.
     */
    PlatformTask runAsyncAtFixedRate(Runnable task, long initialDelayMillis, long periodMillis);
}
