package io.github.mcengine.pluginmanager.api.manager;

/**
 * What the central server wants done to one plugin on this server.
 *
 * <p>Mirrors the {@code action} field of {@code GET /fleet/servers/:id/desired}
 * in {@code MCEngine/server-expressjs}. That service owns the vocabulary; this
 * enum follows it.</p>
 */
public enum PluginAction {

    /** The server does not have this plugin and should. */
    INSTALL,

    /** The server has an older version and should have a newer one. */
    UPDATE,

    /** The server has this plugin and should not. */
    DELETE;

    /**
     * Parses the wire form.
     *
     * @param wire the value from the payload
     * @return the matching action, or {@code null} when it is one this plugin
     *         does not understand — a newer server may send actions this
     *         version has never heard of, and skipping one is correct where
     *         failing the whole poll is not
     */
    public static PluginAction fromWire(String wire) {
        if (wire == null) {
            return null;
        }
        return switch (wire.toLowerCase(java.util.Locale.ROOT)) {
            case "install" -> INSTALL;
            case "update" -> UPDATE;
            case "delete" -> DELETE;
            default -> null;
        };
    }
}
