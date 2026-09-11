package io.github.mcengine.pluginmanager.common.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VersionsTest {

    @Test
    @DisplayName("orders by number, which is the bug the whole class exists to prevent")
    void ordersNumerically() {
        // Compared as text, "1.9.0" > "1.10.0" is true -- and a plugin comparing
        // versions that way updates backwards.
        assertTrue("1.9.0".compareTo("1.10.0") > 0, "the text comparison this replaces");

        assertTrue(Versions.compare("1.10.0", "1.9.0") > 0);
        assertTrue(Versions.compare("1.9.0", "1.10.0") < 0);
        assertEquals(0, Versions.compare("2.0.0", "2.0.0"));
    }

    @Test
    @DisplayName("treats a missing segment as zero")
    void padsMissingSegments() {
        assertEquals(0, Versions.compare("1.2", "1.2.0"));
        assertTrue(Versions.compare("1.2.1", "1.2") > 0);
    }

    @Test
    @DisplayName("ignores a leading v and surrounding space")
    void normalizes() {
        assertEquals(0, Versions.compare(" v3.2.1 ", "3.2.1"));
    }

    @Test
    @DisplayName("puts a pre-release before its release, as semver requires")
    void preReleaseIsOlder() {
        assertTrue(Versions.compare("1.0.0-beta", "1.0.0") < 0);
        assertTrue(Versions.compare("1.0.0", "1.0.0-beta") > 0);
        assertTrue(Versions.compare("1.0.0-alpha", "1.0.0-beta") < 0);
    }

    @Test
    @DisplayName("sorts a realistic list the way a person would")
    void sortsRealistically() {
        List<String> versions = new ArrayList<>(
            List.of("1.10.0", "1.2.0", "0.9.9", "1.9.0", "2.0.0-rc.1", "2.0.0"));
        versions.sort(Versions.COMPARATOR);
        assertEquals(List.of("0.9.9", "1.2.0", "1.9.0", "1.10.0", "2.0.0-rc.1", "2.0.0"), versions);
    }

    @Test
    @DisplayName("treats an unparseable segment as zero rather than failing the poll")
    void toleratesRubbish() {
        // A version this plugin cannot parse should not stop it managing every
        // other plugin on the server.
        assertEquals(0, Versions.compare("1.x.3", "1.0.3"));
    }

    @Test
    @DisplayName("isNewer decides whether an update is staged at all")
    void isNewer() {
        assertTrue(Versions.isNewer("1.10.0", "1.9.0"));
        assertFalse(Versions.isNewer("1.9.0", "1.10.0"));
        assertFalse(Versions.isNewer("1.9.0", "1.9.0"));
        // Nothing installed means anything is newer -- that is an install.
        assertTrue(Versions.isNewer("1.0.0", null));
        assertFalse(Versions.isNewer(null, "1.0.0"));
    }
}
