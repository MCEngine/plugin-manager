package io.github.mcengine.pluginmanager.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers the normalization the request record performs, since every handler
 * relies on it rather than null-checking the payload itself.
 */
class MCPluginManagerRequestTest {

    @Test
    @DisplayName("a null payload is normalized to the empty string")
    void nullPayloadBecomesEmpty() {
        MCPluginManagerRequest request = new MCPluginManagerRequest(UUID.randomUUID(), MCPluginManagerAction.PING, null);

        assertEquals("", request.payload());
    }

    @Test
    @DisplayName("a request without a player is rejected at construction")
    void nullPlayerIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new MCPluginManagerRequest(null, MCPluginManagerAction.PING, ""));
    }

    @Test
    @DisplayName("a request without an action is rejected at construction")
    void nullActionIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new MCPluginManagerRequest(UUID.randomUUID(), null, ""));
    }

    @Test
    @DisplayName("a rejected response carries the reason")
    void rejectedResponseCarriesReason() {
        MCPluginManagerResponse response = MCPluginManagerResponse.rejected("nope");

        assertEquals(false, response.accepted());
        assertEquals("nope", response.message());
    }
}
