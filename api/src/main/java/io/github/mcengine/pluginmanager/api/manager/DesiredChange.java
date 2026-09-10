package io.github.mcengine.pluginmanager.api.manager;

import java.net.URI;
import java.util.Objects;

/**
 * One thing to do, as the central server described it.
 *
 * <p>Everything needed to act is here, which is deliberate: the plugin makes one
 * request per poll and never a second one to find out where a jar is or what it
 * should hash to.</p>
 *
 * @param action       install, update or delete
 * @param pluginId     the {@code name:} from that jar's {@code plugin.yml}
 * @param productId    the catalogue product, or {@code null} for a delete
 * @param fromVersion  what is installed now, or {@code null} for an install
 * @param toVersion    what should be installed, or {@code null} for a delete
 * @param downloadUrl  where to fetch it, or {@code null} for a delete
 * @param sha256       the checksum to verify against, or {@code null} for a delete
 * @param sizeBytes    the expected size, or {@code -1} for a delete
 */
public record DesiredChange(
    PluginAction action,
    String pluginId,
    String productId,
    String fromVersion,
    String toVersion,
    URI downloadUrl,
    String sha256,
    long sizeBytes
) {

    private static final int SHA256_HEX_LENGTH = 64;

    public DesiredChange {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(pluginId, "pluginId");

        if (pluginId.isBlank()) {
            throw new IllegalArgumentException("A change needs a plugin id.");
        }

        // A plugin id becomes a file name. Anything that could make it a path
        // is refused here, at the boundary, rather than at each use.
        if (pluginId.indexOf('/') >= 0 || pluginId.indexOf('\\') >= 0
            || pluginId.contains("..") || pluginId.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(
                "A plugin id may not contain a path separator: " + pluginId);
        }

        if (action != PluginAction.DELETE) {
            Objects.requireNonNull(toVersion, "toVersion");
            Objects.requireNonNull(downloadUrl, "downloadUrl");
            Objects.requireNonNull(sha256, "sha256");

            // A download with no checksum to verify against is not something
            // this plugin will install, so it is not something it will accept.
            if (sha256.length() != SHA256_HEX_LENGTH || !sha256.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException(
                    "A change must carry a lowercase hex SHA-256: " + sha256);
            }
        }
    }

    /** A one-line description for a log or a command response. */
    public String describe() {
        return switch (action) {
            case INSTALL -> "install " + pluginId + " " + toVersion;
            case UPDATE -> "update " + pluginId + " " + fromVersion + " to " + toVersion;
            case DELETE -> "delete " + pluginId;
        };
    }
}
