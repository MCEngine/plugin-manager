package io.github.mcengine.pluginmanager.api;

import java.util.Objects;

/**
 * The server's answer to a {@link MCPluginManagerRequest}.
 *
 * @param accepted Whether the server acted on the request.
 * @param message Human-readable detail: the result when accepted, the reason when not.
 */
public record MCPluginManagerResponse(boolean accepted, String message) {

    /**
     * Rejects a response with no message, because a rejection a player cannot
     * read is indistinguishable from the plugin doing nothing.
     */
    public MCPluginManagerResponse {
        Objects.requireNonNull(message, "message cannot be null");
    }

    /**
     * Builds an accepted response.
     *
     * @param message The result to show the player.
     * @return The response.
     */
    public static MCPluginManagerResponse accepted(String message) {
        return new MCPluginManagerResponse(true, message);
    }

    /**
     * Builds a rejected response.
     *
     * @param reason Why the request was refused.
     * @return The response.
     */
    public static MCPluginManagerResponse rejected(String reason) {
        return new MCPluginManagerResponse(false, reason);
    }
}
