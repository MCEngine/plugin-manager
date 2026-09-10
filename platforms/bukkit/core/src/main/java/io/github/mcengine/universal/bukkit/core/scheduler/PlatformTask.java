package io.github.mcengine.universal.bukkit.core.scheduler;

/**
 * A handle to a repeating task, so the caller can stop it without knowing which
 * scheduler created it.
 */
@FunctionalInterface
public interface PlatformTask {

    /**
     * Cancels the task. Safe to call more than once.
     */
    void cancel();
}
