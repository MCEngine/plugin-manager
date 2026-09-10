package io.github.mcengine.universal.mod.core;

import io.github.mcengine.universal.api.TemplateAction;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trips the wire format, which is the one thing the client and the server
 * must agree on exactly.
 */
class TemplatePayloadCodecTest {

    @Test
    @DisplayName("a request survives an encode and decode unchanged")
    void requestRoundTrips() {
        TemplateRequest original = new TemplateRequest(UUID.randomUUID(), TemplateAction.GREET, "Good evening");

        TemplateRequest decoded = TemplatePayloadCodec.decodeRequest(
            TemplatePayloadCodec.encodeRequest(original));

        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("an empty payload survives the round trip")
    void emptyPayloadRoundTrips() {
        TemplateRequest original = TemplateRequest.of(UUID.randomUUID(), TemplateAction.PING);

        assertEquals(original, TemplatePayloadCodec.decodeRequest(
            TemplatePayloadCodec.encodeRequest(original)));
    }

    @Test
    @DisplayName("a response survives an encode and decode unchanged")
    void responseRoundTrips() {
        TemplateResponse original = TemplateResponse.rejected("not today");

        assertEquals(original, TemplatePayloadCodec.decodeResponse(
            TemplatePayloadCodec.encodeResponse(original)));
    }

    @Test
    @DisplayName("the action is written by name, so reordering the enum cannot change the wire format")
    void actionIsEncodedByName() {
        byte[] encoded = TemplatePayloadCodec.encodeRequest(
            TemplateRequest.of(UUID.randomUUID(), TemplateAction.GREET));

        assertTrue(new String(encoded, java.nio.charset.StandardCharsets.UTF_8).contains("GREET"));
    }

    @Test
    @DisplayName("truncated bytes are rejected rather than silently misread")
    void truncatedPayloadIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> TemplatePayloadCodec.decodeRequest(new byte[] {1, 2, 3}));
    }

    @Test
    @DisplayName("an action this build does not know is rejected rather than defaulted")
    void unknownActionIsRejected() {
        byte[] encoded = TemplatePayloadCodec.encodeRequest(
            TemplateRequest.of(UUID.randomUUID(), TemplateAction.PING));
        // Rewrite the action name in place to one no build knows.
        String corrupted = new String(encoded, java.nio.charset.StandardCharsets.UTF_8)
            .replace("PING", "XXXX");

        assertThrows(IllegalArgumentException.class, () -> TemplatePayloadCodec.decodeRequest(
            corrupted.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
