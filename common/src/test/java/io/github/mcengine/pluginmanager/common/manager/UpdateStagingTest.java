package io.github.mcengine.pluginmanager.common.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdateStagingTest {

    @Test
    @DisplayName("finds the jar a plugin is installed from, whatever the file is called")
    void findsInstalledJar(@TempDir Path plugins) throws IOException {
        PluginInventoryTest.pluginJar(plugins, "EssentialsX-2.19.0.jar", "Essentials", "2.19.0");
        UpdateStaging staging = new UpdateStaging(plugins);

        Path found = staging.findInstalledJar("Essentials");
        assertNotNull(found);
        assertEquals("EssentialsX-2.19.0.jar", found.getFileName().toString());

        // Case-insensitive, because a descriptor's capitalisation is nobody's
        // idea of an identifier.
        assertNotNull(staging.findInstalledJar("essentials"));
        assertNull(staging.findInstalledJar("NotInstalled"));
    }

    @Test
    @DisplayName("records a deletion rather than performing it while the jar is loaded")
    void recordsDeletion(@TempDir Path plugins) throws IOException {
        Path jar = PluginInventoryTest.pluginJar(plugins, "old.jar", "OldPlugin", "1.0.0");
        UpdateStaging staging = new UpdateStaging(plugins);

        staging.markForDeletion("OldPlugin");

        // Still there: on Windows a loaded jar cannot be deleted at all.
        assertTrue(Files.exists(jar));
        assertEquals(List.of("OldPlugin"), staging.readPendingDeletions());
    }

    @Test
    @DisplayName("records a deletion once, however many times it is asked")
    void deletionIsIdempotent(@TempDir Path plugins) throws IOException {
        UpdateStaging staging = new UpdateStaging(plugins);
        staging.markForDeletion("OldPlugin");
        staging.markForDeletion("OldPlugin");
        assertEquals(List.of("OldPlugin"), staging.readPendingDeletions());
    }

    @Test
    @DisplayName("performs pending deletions and clears the record")
    void appliesDeletions(@TempDir Path plugins) throws IOException {
        Path doomed = PluginInventoryTest.pluginJar(plugins, "old.jar", "OldPlugin", "1.0.0");
        Path kept = PluginInventoryTest.pluginJar(plugins, "keep.jar", "Keeper", "1.0.0");

        UpdateStaging staging = new UpdateStaging(plugins);
        staging.markForDeletion("OldPlugin");

        assertEquals(List.of("OldPlugin"), staging.applyPendingDeletions());
        assertFalse(Files.exists(doomed));
        assertTrue(Files.exists(kept));
        assertTrue(staging.readPendingDeletions().isEmpty());
        assertFalse(Files.exists(plugins.resolve(UpdateStaging.PENDING_DELETIONS_FILE)));
    }

    @Test
    @DisplayName("treats an already-absent plugin as done rather than retrying forever")
    void alreadyGone(@TempDir Path plugins) throws IOException {
        UpdateStaging staging = new UpdateStaging(plugins);
        staging.markForDeletion("NeverInstalled");

        assertEquals(List.of("NeverInstalled"), staging.applyPendingDeletions());
        assertTrue(staging.readPendingDeletions().isEmpty());
    }

    @Test
    @DisplayName("ignores a record line that could be a path")
    void ignoresPathsInTheRecord(@TempDir Path plugins) throws IOException {
        Files.createDirectories(plugins);
        // The file lives on disk where anything could edit it, so a line that
        // could escape the directory is dropped rather than resolved.
        Files.write(plugins.resolve(UpdateStaging.PENDING_DELETIONS_FILE),
            List.of("Good", "../../etc/passwd", "a/b", "a\\b", "..", ""));

        assertEquals(List.of("Good"), new UpdateStaging(plugins).readPendingDeletions());
    }

    @Test
    @DisplayName("names the update directory the server itself applies")
    void updateDirectory(@TempDir Path plugins) {
        assertEquals("update",
            new UpdateStaging(plugins).updateDirectory().getFileName().toString());
    }
}
