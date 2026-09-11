package io.github.mcengine.pluginmanager.common.manager;

import io.github.mcengine.pluginmanager.api.manager.InstalledPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads what is actually installed in a {@code plugins/} directory.
 *
 * <p>Each jar's {@code plugin.yml} is read for its {@code name} and
 * {@code version}. The two fields are pulled out by line rather than through a
 * YAML parser, which keeps this class free of any Bukkit dependency and
 * therefore testable without a running server — and the two keys are always
 * top-level scalars, which is the whole of what makes that safe.</p>
 */
public final class PluginInventory {

    /** Descriptors tried in order; the first one present is used. */
    private static final List<String> DESCRIPTORS = List.of("paper-plugin.yml", "plugin.yml");

    private PluginInventory() {
    }

    /**
     * Reads every plugin jar in a directory.
     *
     * @param pluginsDirectory the server's {@code plugins/} directory
     * @param withChecksums    whether to hash each jar, which costs a full read
     * @return one entry per readable jar, sorted by id
     * @throws IOException if the directory cannot be listed
     */
    public static List<InstalledPlugin> read(Path pluginsDirectory, boolean withChecksums)
        throws IOException {
        List<InstalledPlugin> found = new ArrayList<>();
        if (!Files.isDirectory(pluginsDirectory)) {
            return found;
        }

        try (DirectoryStream<Path> jars = Files.newDirectoryStream(pluginsDirectory, "*.jar")) {
            for (Path jar : jars) {
                InstalledPlugin plugin = readJar(jar, withChecksums);
                // A file that is not a plugin, or one being written right now,
                // is skipped. One unreadable jar must not stop the report.
                if (plugin != null) {
                    found.add(plugin);
                }
            }
        }

        found.sort((a, b) -> a.pluginId().compareToIgnoreCase(b.pluginId()));
        return found;
    }

    /**
     * Reads one jar.
     *
     * @param jar           the file
     * @param withChecksum  whether to hash it
     * @return the plugin, or {@code null} when the jar declares no name
     */
    public static InstalledPlugin readJar(Path jar, boolean withChecksum) {
        try (ZipFile archive = new ZipFile(jar.toFile())) {
            for (String descriptor : DESCRIPTORS) {
                ZipEntry entry = archive.getEntry(descriptor);
                if (entry == null) {
                    continue;
                }
                try (InputStream body = archive.getInputStream(entry)) {
                    String text = new String(body.readAllBytes(), StandardCharsets.UTF_8);
                    String name = scalar(text, "name");
                    if (name == null || name.isBlank()) {
                        return null;
                    }
                    String checksum = withChecksum ? Checksums.sha256(jar) : null;
                    return new InstalledPlugin(name, scalar(text, "version"), checksum);
                }
            }
            return null;
        } catch (IOException | RuntimeException unreadable) {
            return null;
        }
    }

    /**
     * Pulls one top-level scalar out of a descriptor.
     *
     * @param yaml the descriptor's text
     * @param key  the key to read
     * @return the value with quotes stripped, or {@code null}
     */
    static String scalar(String yaml, String key) {
        for (String line : yaml.split("\\R")) {
            // Top-level only: an indented line is inside some other mapping, and
            // a `name:` under `commands:` is a command, not the plugin.
            if (line.isEmpty() || Character.isWhitespace(line.charAt(0)) || line.startsWith("#")) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon < 0) {
                continue;
            }
            if (!line.substring(0, colon).trim().equalsIgnoreCase(key)) {
                continue;
            }

            String value = line.substring(colon + 1).trim();
            int comment = value.indexOf(" #");
            if (comment >= 0) {
                value = value.substring(0, comment).trim();
            }
            if (value.length() >= 2
                && ((value.startsWith("'") && value.endsWith("'"))
                    || (value.startsWith("\"") && value.endsWith("\"")))) {
                value = value.substring(1, value.length() - 1);
            }
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    /**
     * The file name a plugin's jar is staged as.
     *
     * <p>Built from the plugin id, which {@code DesiredChange} has already
     * refused to accept with a separator in it — this strips anything else that
     * could surprise a filesystem, so the result is always a plain name.</p>
     *
     * @param pluginId the plugin id
     * @return a safe file name ending in {@code .jar}
     */
    public static String jarNameFor(String pluginId) {
        String cleaned = pluginId.replaceAll("[^A-Za-z0-9._-]", "_")
            // A leading dot makes the file hidden on Unix, and a leading ".."
            // reads as a traversal even once it can no longer be one. Neither
            // is worth keeping just because the substitution made it harmless.
            .replaceFirst("^\\.+", "");

        if (cleaned.isBlank()) {
            cleaned = "plugin";
        }
        return cleaned.toLowerCase(Locale.ROOT).endsWith(".jar") ? cleaned : cleaned + ".jar";
    }
}
