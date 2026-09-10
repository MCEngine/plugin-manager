package io.github.mcengine.pluginmanager.api;

/**
 * The actions a client may ask the server to perform.
 *
 * <p>This enum is the wire vocabulary shared by the Bukkit plugin and the mods.
 * A mod client names one of these constants when it sends a message, and the
 * server dispatches on it. Adding a constant therefore changes the protocol:
 * add it here first, then handle it in
 * {@link AbstractMCPluginManagerService}, which will not compile until you do.</p>
 */
public enum MCPluginManagerAction {

    /**
     * A liveness probe. The server answers without touching any state, so a
     * client can confirm the plugin is present and responding before it sends
     * anything that matters.
     */
    PING,

    /**
     * Asks the server for the greeting belonging to the requesting player.
     */
    GREET
}
