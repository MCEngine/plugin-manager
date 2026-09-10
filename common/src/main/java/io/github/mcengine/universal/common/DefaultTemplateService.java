package io.github.mcengine.universal.common;

import io.github.mcengine.universal.api.AbstractTemplateService;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The in-memory implementation the template ships with.
 *
 * <p>It is a working service, not a stub: it initializes, answers both actions,
 * stores per-player state, and shuts down. Replace its storage with a database
 * and the platform modules need no change at all, which is the property the
 * facade exists to protect.</p>
 */
public final class DefaultTemplateService extends AbstractTemplateService {

    /**
     * The greeting shown to a player who has not set one.
     */
    private static final String DEFAULT_GREETING = "Hello from the universal template";

    /**
     * Per-player greetings.
     *
     * <p>Concurrent because a Bukkit listener, an asynchronous scheduler task,
     * and — on Folia — a region thread can all reach it at once.</p>
     */
    private final Map<UUID, String> greetings = new ConcurrentHashMap<>();

    /**
     * Whether {@link #initialize()} has completed and {@link #shutdown()} has not.
     */
    private volatile boolean running;

    /**
     * Brings the service up. Nothing here can fail, so the future is already
     * complete; a storage-backed implementation would return a real one.
     *
     * @return A completed future.
     */
    @Override
    public CompletableFuture<Void> initialize() {
        running = true;
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Answers a liveness probe.
     *
     * @param request The originating request.
     * @return A future carrying the answer.
     */
    @Override
    protected CompletableFuture<TemplateResponse> ping(TemplateRequest request) {
        return CompletableFuture.completedFuture(running
            ? TemplateResponse.accepted("pong")
            : TemplateResponse.rejected("The service is not running"));
    }

    /**
     * Answers a greeting request, storing the payload as the player's greeting
     * when one was supplied.
     *
     * @param request The originating request.
     * @return A future carrying the answer.
     */
    @Override
    protected CompletableFuture<TemplateResponse> greet(TemplateRequest request) {
        if (!running) {
            return CompletableFuture.completedFuture(
                TemplateResponse.rejected("The service is not running"));
        }
        if (!request.payload().isEmpty()) {
            greetings.put(request.playerId(), request.payload());
        }
        return CompletableFuture.completedFuture(
            TemplateResponse.accepted(greetingFor(request.playerId()).orElse(DEFAULT_GREETING)));
    }

    /**
     * Reads a player's greeting from memory.
     *
     * @param playerId The player to look up.
     * @return The greeting, or empty when the player has none.
     */
    @Override
    public Optional<String> greetingFor(UUID playerId) {
        return Optional.ofNullable(greetings.get(playerId));
    }

    /**
     * Drops the stored state and marks the service stopped.
     */
    @Override
    public void shutdown() {
        running = false;
        greetings.clear();
    }
}
