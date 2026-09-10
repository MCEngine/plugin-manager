package io.github.mcengine.universal.mod.core;

/**
 * The plugin message channel the mod client and the mod server talk over.
 *
 * <p>Both halves read the identifier from here rather than writing the string
 * themselves, because a channel name that differs by one character produces no
 * error at all — the messages are simply never delivered, which is among the
 * least pleasant things to debug on a live server.</p>
 */
public final class TemplateChannel {

    /**
     * The namespace segment of the channel identifier. Matches the mod id.
     */
    public static final String NAMESPACE = "template";

    /**
     * Path of the client-to-server channel, carrying an encoded request.
     */
    public static final String REQUEST_PATH = "request";

    /**
     * Path of the server-to-client channel, carrying an encoded response.
     */
    public static final String RESPONSE_PATH = "response";

    /**
     * The client-to-server channel identifier, in Minecraft's
     * {@code namespace:path} form.
     */
    public static final String REQUEST_ID = NAMESPACE + ":" + REQUEST_PATH;

    /**
     * The server-to-client channel identifier.
     */
    public static final String RESPONSE_ID = NAMESPACE + ":" + RESPONSE_PATH;

    /**
     * Not instantiable.
     */
    private TemplateChannel() {
    }
}
