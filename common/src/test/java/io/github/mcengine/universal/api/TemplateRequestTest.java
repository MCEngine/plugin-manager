package io.github.mcengine.universal.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers the normalization the request record performs, since every handler
 * relies on it rather than null-checking the payload itself.
 */
class TemplateRequestTest {

    @Test
    @DisplayName("a null payload is normalized to the empty string")
    void nullPayloadBecomesEmpty() {
        TemplateRequest request = new TemplateRequest(UUID.randomUUID(), TemplateAction.PING, null);

        assertEquals("", request.payload());
    }

    @Test
    @DisplayName("a request without a player is rejected at construction")
    void nullPlayerIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new TemplateRequest(null, TemplateAction.PING, ""));
    }

    @Test
    @DisplayName("a request without an action is rejected at construction")
    void nullActionIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new TemplateRequest(UUID.randomUUID(), null, ""));
    }

    @Test
    @DisplayName("a rejected response carries the reason")
    void rejectedResponseCarriesReason() {
        TemplateResponse response = TemplateResponse.rejected("nope");

        assertEquals(false, response.accepted());
        assertEquals("nope", response.message());
    }
}
