package io.github.mcengine.universal.api;

import java.util.Objects;

/**
 * The server's answer to a {@link TemplateRequest}.
 *
 * @param accepted Whether the server acted on the request.
 * @param message Human-readable detail: the result when accepted, the reason when not.
 */
public record TemplateResponse(boolean accepted, String message) {

    /**
     * Rejects a response with no message, because a rejection a player cannot
     * read is indistinguishable from the plugin doing nothing.
     */
    public TemplateResponse {
        Objects.requireNonNull(message, "message cannot be null");
    }

    /**
     * Builds an accepted response.
     *
     * @param message The result to show the player.
     * @return The response.
     */
    public static TemplateResponse accepted(String message) {
        return new TemplateResponse(true, message);
    }

    /**
     * Builds a rejected response.
     *
     * @param reason Why the request was refused.
     * @return The response.
     */
    public static TemplateResponse rejected(String reason) {
        return new TemplateResponse(false, reason);
    }
}
