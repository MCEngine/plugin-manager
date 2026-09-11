package io.github.mcengine.pluginmanager.api.manager;

import java.util.Objects;

/**
 * One plugin as it exists on this server right now.
 *
 * <p>Reported to the central server as a full inventory, not a patch: a plugin
 * removed by hand simply stops appearing, and a patch would leave it recorded
 * forever.</p>
 *
 * @param pluginId the {@code name:} from its {@code plugin.yml}
 * @param version  its {@code version:}, or {@code null} when it declares none
 * @param sha256   the checksum of the jar on disk, or {@code null} when unread
 */
public record InstalledPlugin(String pluginId, String version, String sha256) {

    public InstalledPlugin {
        Objects.requireNonNull(pluginId, "pluginId");
        if (pluginId.isBlank()) {
            throw new IllegalArgumentException("An installed plugin needs an id.");
        }
    }
}
