package io.github.mcengine.pluginmanager.mod.core;

import io.github.mcengine.pluginmanager.api.MCPluginManagerAction;
import io.github.mcengine.pluginmanager.api.MCPluginManagerRequest;
import io.github.mcengine.pluginmanager.api.MCPluginManagerResponse;
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
class MCPluginManagerPayloadCodecTest {

    @Test
    @DisplayName("a request survives an encode and decode unchanged")
    void requestRoundTrips() {
        MCPluginManagerRequest original = new MCPluginManagerRequest(UUID.randomUUID(), MCPluginManagerAction.GREET, "Good evening");

        MCPluginManagerRequest decoded = MCPluginManagerPayloadCodec.decodeRequest(
            MCPluginManagerPayloadCodec.encodeRequest(original));

        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("an empty payload survives the round trip")
    void emptyPayloadRoundTrips() {
        MCPluginManagerRequest original = MCPluginManagerRequest.of(UUID.randomUUID(), MCPluginManagerAction.PING);

        assertEquals(original, MCPluginManagerPayloadCodec.decodeRequest(
            MCPluginManagerPayloadCodec.encodeRequest(original)));
    }

    @Test
    @DisplayName("a response survives an encode and decode unchanged")
    void responseRoundTrips() {
        MCPluginManagerResponse original = MCPluginManagerResponse.rejected("not today");

        assertEquals(original, MCPluginManagerPayloadCodec.decodeResponse(
            MCPluginManagerPayloadCodec.encodeResponse(original)));
    }

    @Test
    @DisplayName("the action is written by name, so reordering the enum cannot change the wire format")
    void actionIsEncodedByName() {
        byte[] encoded = MCPluginManagerPayloadCodec.encodeRequest(
            MCPluginManagerRequest.of(UUID.randomUUID(), MCPluginManagerAction.GREET));

        assertTrue(new String(encoded, java.nio.charset.StandardCharsets.UTF_8).contains("GREET"));
    }

    @Test
    @DisplayName("truncated bytes are rejected rather than silently misread")
    void truncatedPayloadIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> MCPluginManagerPayloadCodec.decodeRequest(new byte[] {1, 2, 3}));
    }

    @Test
    @DisplayName("an action this build does not know is rejected rather than defaulted")
    void unknownActionIsRejected() {
        byte[] encoded = MCPluginManagerPayloadCodec.encodeRequest(
            MCPluginManagerRequest.of(UUID.randomUUID(), MCPluginManagerAction.PING));
        // Rewrite the action name in place to one no build knows.
        String corrupted = new String(encoded, java.nio.charset.StandardCharsets.UTF_8)
            .replace("PING", "XXXX");

        assertThrows(IllegalArgumentException.class, () -> MCPluginManagerPayloadCodec.decodeRequest(
            corrupted.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
