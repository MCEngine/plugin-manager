package io.github.mcengine.pluginmanager.api;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Dispatches {@link MCPluginManagerAction} so implementations do not each write the
 * same switch.
 *
 * <p>{@link #handle(MCPluginManagerRequest)} is final and exhaustive over the enum. The
 * switch has no {@code default} branch on purpose: adding a constant to
 * {@link MCPluginManagerAction} then fails to compile here until a handler exists for
 * it, which is the behaviour you want from a protocol shared by four platforms.
 * A {@code default} would turn that compile error into a runtime surprise on
 * whichever platform received the new action first.</p>
 */
public abstract class AbstractMCPluginManagerService implements MCPluginManagerService {

    /**
     * Routes a request to the handler for its action.
     *
     * @param request The action to perform.
     * @return A future carrying the server's answer.
     */
    @Override
    public final CompletableFuture<MCPluginManagerResponse> handle(MCPluginManagerRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        return switch (request.action()) {
            case PING -> ping(request);
            case GREET -> greet(request);
        };
    }

    /**
     * Answers a {@link MCPluginManagerAction#PING}.
     *
     * @param request The originating request.
     * @return A future carrying the answer.
     */
    protected abstract CompletableFuture<MCPluginManagerResponse> ping(MCPluginManagerRequest request);

    /**
     * Answers a {@link MCPluginManagerAction#GREET}.
     *
     * @param request The originating request.
     * @return A future carrying the answer.
     */
    protected abstract CompletableFuture<MCPluginManagerResponse> greet(MCPluginManagerRequest request);
}
