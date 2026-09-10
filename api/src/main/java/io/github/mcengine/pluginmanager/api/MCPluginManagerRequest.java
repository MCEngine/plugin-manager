package io.github.mcengine.pluginmanager.api;

import java.util.Objects;
import java.util.UUID;

/**
 * One action a client is asking the server to perform.
 *
 * @param playerId The player the request is made on behalf of.
 * @param action The action being requested.
 * @param payload Free-form argument for the action; never null, empty when unused.
 */
public record MCPluginManagerRequest(UUID playerId, MCPluginManagerAction action, String payload) {

    /**
     * Rejects a request that names no player or no action, and normalizes a
     * missing payload to the empty string.
     *
     * <p>Normalizing here rather than at each call site means no handler has to
     * null-check the payload, which is the kind of check that gets forgotten in
     * exactly one branch.</p>
     */
    public MCPluginManagerRequest {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(action, "action cannot be null");
        payload = payload == null ? "" : payload;
    }

    /**
     * Creates a request that carries no payload.
     *
     * @param playerId The player the request is made on behalf of.
     * @param action The action being requested.
     * @return The request.
     */
    public static MCPluginManagerRequest of(UUID playerId, MCPluginManagerAction action) {
        return new MCPluginManagerRequest(playerId, action, "");
    }
}
