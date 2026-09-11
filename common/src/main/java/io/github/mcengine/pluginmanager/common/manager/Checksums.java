package io.github.mcengine.pluginmanager.common.manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** SHA-256 over a stream or a file. */
public final class Checksums {

    private static final int BUFFER_BYTES = 64 * 1024;

    private Checksums() {
    }

    /**
     * Hashes everything in {@code source} while copying it nowhere.
     *
     * @param source the stream, which is fully consumed but not closed
     * @return lowercase hex
     * @throws IOException if reading fails
     */
    public static String sha256(InputStream source) throws IOException {
        MessageDigest digest = newDigest();
        try (DigestInputStream digesting = new DigestInputStream(source, digest)) {
            byte[] buffer = new byte[BUFFER_BYTES];
            while (digesting.read(buffer) >= 0) {
                // Reading is the point; the digest updates as a side effect.
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /**
     * Hashes a file.
     *
     * @param file the file
     * @return lowercase hex
     * @throws IOException if reading fails
     */
    public static String sha256(Path file) throws IOException {
        try (InputStream source = Files.newInputStream(file)) {
            return sha256(source);
        }
    }

    /**
     * Compares two hex digests without leaking where they first differ.
     *
     * @param a one digest
     * @param b the other
     * @return whether they match
     */
    public static boolean matches(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        // Constant time. The values here are not secret, but the habit is
        // cheap and the alternative is a comparison that leaks a prefix.
        int difference = 0;
        for (int i = 0; i < a.length(); i++) {
            difference |= a.charAt(i) ^ b.charAt(i);
        }
        return difference == 0;
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            // Every Java implementation is required to provide SHA-256.
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
