package io.github.mcengine.pluginmanager.api.manager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * What this plugin does with a central server.
 *
 * <p>An interface so the Bukkit side depends on the behaviour rather than on
 * {@code java.net.http}, and so a test can exercise the install and update flow
 * without a network.</p>
 */
public interface ManagerClient {

    /** The server this client talks to. */
    CentralServer server();

    /**
     * Registers this Minecraft server and returns the key to store.
     *
     * <p>Called once, by hand, when a server has no key yet. The key is returned
     * exactly once and is a credential.</p>
     *
     * @param name what the panel should call this server
     * @return the generated server key
     * @throws IOException if the request fails
     */
    String register(String name) throws IOException;

    /**
     * Reports the full inventory of installed plugins.
     *
     * @param serverId    this server's id
     * @param installed   every plugin currently on disk
     * @param platform    {@code spigot}, {@code paper} or {@code folia}
     * @param mcVersion   the running Minecraft version
     * @param agentVersion this plugin's own version
     * @throws IOException if the request fails
     */
    void report(
        String serverId,
        List<InstalledPlugin> installed,
        String platform,
        String mcVersion,
        String agentVersion
    ) throws IOException;

    /**
     * Asks what should change.
     *
     * @param serverId this server's id
     * @return the work to do, and when to ask again
     * @throws IOException if the request fails
     */
    DesiredState desired(String serverId) throws IOException;

    /**
     * Downloads a jar and verifies it against the checksum the server declared.
     *
     * <p>The file is written to {@code target} only after the checksum matches.
     * A mismatch leaves nothing behind — an unverified jar must never exist at a
     * path anything might later load from.</p>
     *
     * @param change the change carrying the URL, size and checksum
     * @param target where to write it
     * @throws IOException          if the download fails
     * @throws ChecksumMismatchException if the bytes do not match
     */
    void download(DesiredChange change, Path target) throws IOException;

    /**
     * Reports what happened to one plugin.
     *
     * @param serverId this server's id
     * @param pluginId the plugin acted on
     * @param state    {@code installed}, {@code pending_update}, {@code pending_delete} or {@code failed}
     * @param error    the reason, when the state is {@code failed}
     * @throws IOException if the request fails
     */
    void reportOutcome(String serverId, String pluginId, String state, String error)
        throws IOException;
}
