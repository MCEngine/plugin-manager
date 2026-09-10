package io.github.mcengine.pluginmanager.bukkit.core.manager;

import io.github.mcengine.pluginmanager.api.manager.CentralServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * The plugin's configuration, read once and validated.
 *
 * <p>A malformed entry is skipped with a logged reason rather than failing
 * startup: a server operator with three catalogues configured and one typo
 * should lose that one catalogue, not the plugin.</p>
 *
 * @param serverId       this server's id from the central server, or empty when unregistered
 * @param serverKey      the credential written by {@code /mcpm register}
 * @param servers        every central server, in priority order
 * @param pollSeconds    the interval used before the first successful poll
 * @param autoApply      whether a poll applies what it finds
 * @param reportInventory whether the installed inventory is reported
 */
public record ManagerConfig(
    String serverId,
    String serverKey,
    List<CentralServer> servers,
    int pollSeconds,
    boolean autoApply,
    boolean reportInventory
) {

    private static final int MINIMUM_POLL_SECONDS = 30;

    public ManagerConfig {
        servers = List.copyOf(servers);
    }

    /** True when this server has been registered and has somewhere to talk to. */
    public boolean isUsable() {
        return !serverId.isBlank() && !serverKey.isBlank() && !servers.isEmpty();
    }

    /**
     * Reads the configuration.
     *
     * @param config the loaded {@code config.yml}
     * @param logger where to report a skipped entry
     * @return the configuration, with invalid central servers omitted
     */
    public static ManagerConfig from(FileConfiguration config, Logger logger) {
        List<CentralServer> servers = new ArrayList<>();
        List<?> raw = config.getList("servers", List.of());

        int index = 0;
        for (Object entry : raw) {
            index++;
            CentralServer parsed = readServer(entry, index, logger);
            if (parsed != null) {
                servers.add(parsed);
            }
        }
        servers.sort(Comparator.comparingInt(CentralServer::priority));

        int poll = Math.max(MINIMUM_POLL_SECONDS, config.getInt("manager.poll-seconds", 300));

        return new ManagerConfig(
            config.getString("server.id", "").trim(),
            config.getString("server.key", "").trim(),
            servers,
            poll,
            config.getBoolean("manager.auto-apply", true),
            config.getBoolean("manager.report-inventory", true));
    }

    /** A configuration value as a string, treating a missing one as empty. */
    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static CentralServer readServer(Object entry, int index, Logger logger) {
        String name;
        String url;
        String token;
        int priority;

        // Bukkit hands back a Map for a list of maps and a ConfigurationSection
        // for a nested section, depending on how the file was written. Both
        // shapes are read rather than one being declared the only correct one.
        if (entry instanceof ConfigurationSection section) {
            name = section.getString("name", "");
            url = section.getString("url", "");
            token = section.getString("token", "");
            priority = section.getInt("priority", index);
        } else if (entry instanceof Map<?, ?> map) {
            name = text(map.get("name"));
            url = text(map.get("url"));
            token = text(map.get("token"));
            Object rawPriority = map.get("priority");
            priority = rawPriority instanceof Number number ? number.intValue() : index;
        } else {
            logger.warning("Ignoring central server " + index + ": it is not a mapping.");
            return null;
        }

        if (token.isBlank()) {
            // The commonest case by far: a fresh config with the placeholder
            // still in it. Said plainly rather than as a validation error.
            logger.warning("Central server '" + name + "' has no token yet, so it is not in use. "
                + "Create one in the web panel with the fleet:read, fleet:write and artifact:read scopes.");
            return null;
        }

        try {
            return new CentralServer(name, new URI(url), token, priority);
        } catch (URISyntaxException | IllegalArgumentException invalid) {
            logger.warning("Ignoring central server '" + name + "': " + invalid.getMessage());
            return null;
        }
    }
}
