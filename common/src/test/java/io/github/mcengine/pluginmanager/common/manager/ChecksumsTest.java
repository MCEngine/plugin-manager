package io.github.mcengine.pluginmanager.common.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChecksumsTest {

    /** The published SHA-256 of the empty input, as an independent check. */
    private static final String EMPTY =
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Test
    @DisplayName("matches a known digest")
    void knownDigest() throws IOException {
        assertEquals(EMPTY, Checksums.sha256(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    @DisplayName("hashes a file the same as its bytes")
    void fileAndStreamAgree(@TempDir Path directory) throws IOException {
        byte[] bytes = "some jar content".getBytes(StandardCharsets.UTF_8);
        Path file = directory.resolve("a.jar");
        Files.write(file, bytes);

        assertEquals(Checksums.sha256(new ByteArrayInputStream(bytes)), Checksums.sha256(file));
    }

    @Test
    @DisplayName("hashes a file larger than one buffer")
    void largeFile(@TempDir Path directory) throws IOException {
        byte[] bytes = new byte[300 * 1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i % 251);
        }
        Path file = directory.resolve("big.jar");
        Files.write(file, bytes);

        assertEquals(Checksums.sha256(new ByteArrayInputStream(bytes)), Checksums.sha256(file));
    }

    @Test
    @DisplayName("compares digests, and refuses anything that is not one")
    void comparison() {
        assertTrue(Checksums.matches(EMPTY, EMPTY));
        assertFalse(Checksums.matches(EMPTY, EMPTY.replace('e', 'f')));
        assertFalse(Checksums.matches(EMPTY, null));
        assertFalse(Checksums.matches(null, EMPTY));
        assertFalse(Checksums.matches(EMPTY, EMPTY.substring(1)));
    }
}
