package io.github.mcengine.pluginmanager.api.manager;

import java.io.IOException;

/**
 * Thrown when a downloaded jar does not hash to what the central server said.
 *
 * <p>Its own type, not a generic {@link IOException}, because the two need
 * different treatment: a network failure is worth retrying and this is not.
 * A mismatch means the bytes are not what was published, and downloading them
 * again is as likely to produce the same wrong file as a different one.</p>
 */
public class ChecksumMismatchException extends IOException {

    private static final long serialVersionUID = 1L;

    private final String expected;
    private final String actual;

    public ChecksumMismatchException(String pluginId, String expected, String actual) {
        super("Refusing " + pluginId + ": expected SHA-256 " + expected + " but the download hashed to " + actual);
        this.expected = expected;
        this.actual = actual;
    }

    public String expected() {
        return expected;
    }

    public String actual() {
        return actual;
    }
}
