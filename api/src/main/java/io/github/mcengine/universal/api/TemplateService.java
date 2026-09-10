package io.github.mcengine.universal.api;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The behaviour this project offers, independent of where it runs.
 *
 * <p>Every platform module reaches this interface through the single facade in
 * {@code common}; none of them implement it directly. That is what keeps a
 * scheduler, a listener, or a mod packet handler from growing its own copy of
 * the logic.</p>
 *
 * <p>Asynchronous methods return {@link CompletableFuture} because a real
 * implementation will reach storage or a network, and blocking the server's main
 * thread to wait for either is how a plugin becomes a lag report. The one
 * synchronous method answers from memory and is safe on a hot path.</p>
 */
public interface TemplateService {

    /**
     * Brings the service up: opens whatever it needs and makes itself usable.
     *
     * @return A future completing when the service is ready, and completing
     *         exceptionally when it is not, so the caller can retry rather than
     *         run half-initialized.
     */
    CompletableFuture<Void> initialize();

    /**
     * Performs one requested action.
     *
     * @param request The action to perform.
     * @return A future carrying the server's answer.
     */
    CompletableFuture<TemplateResponse> handle(TemplateRequest request);

    /**
     * Reads a player's greeting from memory.
     *
     * <p>Synchronous by design: it touches no storage, so it is safe to call
     * from a listener or a scheduler tick.</p>
     *
     * @param playerId The player to look up.
     * @return The greeting, or empty when the player has none.
     */
    Optional<String> greetingFor(UUID playerId);

    /**
     * Releases whatever {@link #initialize()} acquired. Safe to call when the
     * service never came up.
     */
    void shutdown();
}
