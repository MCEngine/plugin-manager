package io.github.mcengine.pluginmanager.common.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mcengine.pluginmanager.api.manager.InstalledPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PluginInventoryTest {

    /** Writes a jar containing one descriptor. */
    static Path jar(Path directory, String fileName, String descriptor, String body)
        throws IOException {
        Path file = directory.resolve(fileName);
        try (OutputStream out = Files.newOutputStream(file);
             ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(descriptor));
            zip.write(body.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return file;
    }

    static Path pluginJar(Path directory, String fileName, String name, String version)
        throws IOException {
        return jar(directory, fileName, "plugin.yml",
            "name: " + name + "\nversion: " + version + "\nmain: com.example.Main\n");
    }

    @Test
    @DisplayName("reads every jar in the directory, sorted by id")
    void readsDirectory(@TempDir Path plugins) throws IOException {
        pluginJar(plugins, "zed.jar", "Zed", "1.0.0");
        pluginJar(plugins, "essentials.jar", "Essentials", "2.19.0");

        List<InstalledPlugin> found = PluginInventory.read(plugins, false);

        assertEquals(List.of("Essentials", "Zed"), found.stream().map(InstalledPlugin::pluginId).toList());
        assertEquals("2.19.0", found.get(0).version());
    }

    @Test
    @DisplayName("identifies a plugin by its descriptor, not by its file name")
    void nameComesFromDescriptor(@TempDir Path plugins) throws IOException {
        // EssentialsX-2.20.1.jar declares `name: Essentials`, and matching on
        // the file name would miss it.
        pluginJar(plugins, "EssentialsX-2.20.1.jar", "Essentials", "2.20.1");

        List<InstalledPlugin> found = PluginInventory.read(plugins, false);
        assertEquals("Essentials", found.get(0).pluginId());
    }

    @Test
    @DisplayName("prefers paper-plugin.yml where a jar carries one")
    void prefersPaperDescriptor(@TempDir Path plugins) throws IOException {
        jar(plugins, "modern.jar", "paper-plugin.yml", "name: Modern\nversion: 3.0.0\n");
        assertEquals("Modern", PluginInventory.read(plugins, false).get(0).pluginId());
    }

    @Test
    @DisplayName("skips a file that is not a plugin rather than failing the whole report")
    void skipsUnreadable(@TempDir Path plugins) throws IOException {
        pluginJar(plugins, "good.jar", "Good", "1.0.0");
        Files.writeString(plugins.resolve("corrupt.jar"), "this is not a zip");
        jar(plugins, "nameless.jar", "plugin.yml", "version: 1.0.0\n");

        // One unreadable jar must not stop a server reporting the other twenty.
        List<InstalledPlugin> found = PluginInventory.read(plugins, false);
        assertEquals(1, found.size());
        assertEquals("Good", found.get(0).pluginId());
    }

    @Test
    @DisplayName("returns an empty list rather than failing when there is no plugins directory")
    void missingDirectory(@TempDir Path base) throws IOException {
        assertTrue(PluginInventory.read(base.resolve("nope"), false).isEmpty());
    }

    @Test
    @DisplayName("hashes each jar only when asked, because it costs a full read")
    void checksums(@TempDir Path plugins) throws IOException {
        Path file = pluginJar(plugins, "a.jar", "A", "1.0.0");

        assertNull(PluginInventory.read(plugins, false).get(0).sha256());
        assertEquals(Checksums.sha256(file), PluginInventory.read(plugins, true).get(0).sha256());
    }

    @Test
    @DisplayName("reads only top-level keys, so a command's name is not the plugin's")
    void ignoresNestedKeys() {
        String descriptor = """
            name: Essentials
            version: 2.19.0
            commands:
              home:
                name: NotThePluginName
            """;
        assertEquals("Essentials", PluginInventory.scalar(descriptor, "name"));
        assertEquals("2.19.0", PluginInventory.scalar(descriptor, "version"));
    }

    @Test
    @DisplayName("strips quotes and trailing comments from a scalar")
    void cleansScalars() {
        assertEquals("Essentials", PluginInventory.scalar("name: 'Essentials'", "name"));
        assertEquals("Essentials", PluginInventory.scalar("name: \"Essentials\"", "name"));
        assertEquals("1.0.0", PluginInventory.scalar("version: 1.0.0 # a comment", "version"));
        assertNull(PluginInventory.scalar("name:", "name"));
        assertNull(PluginInventory.scalar("# name: Commented", "name"));
    }

    @Test
    @DisplayName("never produces a jar name that is a path")
    void jarNamesAreAlwaysPlain() {
        // Case is preserved; only the .jar suffix is checked case-insensitively,
        // so a plugin id that already ends in one does not get a second.
        assertEquals("Essentials.jar", PluginInventory.jarNameFor("Essentials.jar"));
        assertEquals("Essentials.JAR", PluginInventory.jarNameFor("Essentials.JAR"));
        assertEquals("Essentials.jar", PluginInventory.jarNameFor("Essentials"));

        for (String hostile : List.of("../../etc/passwd", "a/b", "a\\b", "..", "...", "")) {
            String name = PluginInventory.jarNameFor(hostile);
            assertFalse(name.contains("/"), name);
            assertFalse(name.contains("\\"), name);
            assertFalse(name.startsWith(".."), name);
            assertNotNull(name);
        }
    }
}
