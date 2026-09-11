package io.github.mcengine.pluginmanager.common.manager;

import io.github.mcengine.pluginmanager.api.manager.DesiredChange;
import io.github.mcengine.pluginmanager.api.manager.ManagerClient;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Applying a change to a {@code plugins/} directory.
 *
 * <p><strong>Bukkit cannot safely unload a plugin.</strong> There is no supported
 * {@code unload}, and the classloader tricks that pretend otherwise leak and
 * corrupt state. So nothing here loads or unloads anything:</p>
 *
 * <ul>
 *   <li>An install or an update downloads into {@code plugins/update/}, which the
 *       server itself moves over the old jar on its next start. That is a
 *       built-in mechanism, not a trick.</li>
 *   <li>A delete is recorded and performed at shutdown, because a loaded jar
 *       cannot be deleted on Windows while the server is running.</li>
 * </ul>
 *
 * <p>Every one of these steps therefore takes effect at the next restart, and
 * saying so is the honest description of what this plugin does.</p>
 */
public final class UpdateStaging {

    /** The directory Spigot and Paper apply on startup. */
    public static final String UPDATE_DIRECTORY = "update";

    /** Where pending deletions are recorded between a request and a shutdown. */
    public static final String PENDING_DELETIONS_FILE = ".mcpluginmanager-pending-deletions";

    private final Path pluginsDirectory;

    public UpdateStaging(Path pluginsDirectory) {
        this.pluginsDirectory = Objects.requireNonNull(pluginsDirectory, "pluginsDirectory");
    }

    /** The directory the server applies on startup. */
    public Path updateDirectory() {
        return pluginsDirectory.resolve(UPDATE_DIRECTORY);
    }

    /**
     * Downloads a jar into the update directory.
     *
     * <p>The client verifies the checksum and writes nothing on a mismatch, so
     * an unverified jar never reaches this directory — which matters more here
     * than anywhere else, because the server will load whatever is in it.</p>
     *
     * @param client the client for the server that described the change
     * @param change the install or update to stage
     * @return where the jar was written
     * @throws IOException if the download or the write fails
     */
    public Path stage(ManagerClient client, DesiredChange change) throws IOException {
        Path directory = updateDirectory();
        Files.createDirectories(directory);

        // The name must match the jar already installed for the server to
        // replace it rather than load a second copy alongside it.
        Path existing = findInstalledJar(change.pluginId());
        String fileName = existing != null
            ? existing.getFileName().toString()
            : PluginInventory.jarNameFor(change.pluginId());

        Path target = directory.resolve(fileName);
        client.download(change, target);
        return target;
    }

    /**
     * Records that a plugin should be removed.
     *
     * <p>Recorded rather than performed: the jar is loaded, and on Windows a
     * loaded jar cannot be deleted. {@link #applyPendingDeletions()} runs at
     * shutdown, when it can.</p>
     *
     * @param pluginId the plugin to remove
     * @throws IOException if the record cannot be written
     */
    public void markForDeletion(String pluginId) throws IOException {
        Files.createDirectories(pluginsDirectory);
        Path record = pluginsDirectory.resolve(PENDING_DELETIONS_FILE);

        List<String> pending = readPendingDeletions();
        if (pending.contains(pluginId)) {
            return;
        }
        pending.add(pluginId);
        Files.write(record, pending);
    }

    /** Every plugin currently marked for deletion. */
    public List<String> readPendingDeletions() throws IOException {
        Path record = pluginsDirectory.resolve(PENDING_DELETIONS_FILE);
        if (!Files.exists(record)) {
            return new ArrayList<>();
        }
        List<String> pending = new ArrayList<>();
        for (String line : Files.readAllLines(record)) {
            String trimmed = line.trim();
            // The file is written by this class, but it lives on disk where
            // anything could edit it -- so a line that could be a path is
            // ignored rather than resolved.
            if (!trimmed.isEmpty() && trimmed.indexOf('/') < 0 && trimmed.indexOf('\\') < 0
                && !trimmed.contains("..")) {
                pending.add(trimmed);
            }
        }
        return pending;
    }

    /**
     * Deletes every jar marked for removal.
     *
     * <p>Called at shutdown. A jar that cannot be deleted stays on the list, so
     * the next shutdown tries again rather than the request being lost.</p>
     *
     * @return the plugin ids actually deleted
     * @throws IOException if the record cannot be rewritten
     */
    public List<String> applyPendingDeletions() throws IOException {
        List<String> pending = readPendingDeletions();
        if (pending.isEmpty()) {
            return List.of();
        }

        List<String> deleted = new ArrayList<>();
        List<String> remaining = new ArrayList<>();

        for (String pluginId : pending) {
            Path jar = findInstalledJar(pluginId);
            if (jar == null) {
                // Already gone, by this plugin or by hand. Either way, done.
                deleted.add(pluginId);
                continue;
            }
            try {
                Files.delete(jar);
                deleted.add(pluginId);
            } catch (IOException stillLocked) {
                remaining.add(pluginId);
            }
        }

        Path record = pluginsDirectory.resolve(PENDING_DELETIONS_FILE);
        if (remaining.isEmpty()) {
            Files.deleteIfExists(record);
        } else {
            Files.write(record, remaining);
        }
        return deleted;
    }

    /**
     * Finds the jar a plugin is installed from.
     *
     * <p>By reading each descriptor rather than by guessing at file names: a jar
     * called {@code EssentialsX-2.20.1.jar} declares {@code name: Essentials},
     * and matching on the file name would miss it.</p>
     *
     * @param pluginId the plugin id
     * @return the jar, or {@code null} when it is not installed
     */
    public Path findInstalledJar(String pluginId) {
        if (!Files.isDirectory(pluginsDirectory)) {
            return null;
        }
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(pluginsDirectory, "*.jar")) {
            for (Path jar : jars) {
                var installed = PluginInventory.readJar(jar, false);
                if (installed != null && installed.pluginId().equalsIgnoreCase(pluginId)) {
                    return jar;
                }
            }
        } catch (IOException unreadable) {
            return null;
        }
        return null;
    }
}
