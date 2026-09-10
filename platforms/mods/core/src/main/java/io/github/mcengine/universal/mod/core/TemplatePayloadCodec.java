package io.github.mcengine.universal.mod.core;

import io.github.mcengine.universal.api.TemplateAction;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

/**
 * Turns the shared contract types into bytes and back.
 *
 * <p>This class is why {@code platforms/mods/core} exists. The client encodes and
 * the server decodes, so if the two ever disagreed about the byte layout the
 * failure would be a corrupt read on a live server rather than a compile error.
 * Writing the format once, against the same {@link TemplateAction} the Bukkit
 * side uses, makes that disagreement impossible.</p>
 *
 * <p>It deliberately depends on nothing but {@code api} and the JDK — no
 * Minecraft, no mod loader — so all six loader modules can share it.</p>
 */
public final class TemplatePayloadCodec {

    /**
     * Not instantiable.
     */
    private TemplatePayloadCodec() {
    }

    /**
     * Encodes a request.
     *
     * @param request The request to encode.
     * @return The bytes to send.
     */
    public static byte[] encodeRequest(TemplateRequest request) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeLong(request.playerId().getMostSignificantBits());
            out.writeLong(request.playerId().getLeastSignificantBits());
            // The ordinal is deliberately not used: reordering the enum would
            // silently change the wire format. The name costs a few bytes and
            // survives a reorder.
            out.writeUTF(request.action().name());
            out.writeUTF(request.payload());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to encode a request", e);
        }
        return buffer.toByteArray();
    }

    /**
     * Decodes a request.
     *
     * @param bytes The received bytes.
     * @return The request.
     * @throws IllegalArgumentException When the bytes do not describe a request
     *         this build understands, which includes an action added by a newer
     *         version of the mod.
     */
    public static TemplateRequest decodeRequest(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            UUID playerId = new UUID(in.readLong(), in.readLong());
            String action = in.readUTF();
            String payload = in.readUTF();
            return new TemplateRequest(playerId, TemplateAction.valueOf(action), payload);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed request payload", e);
        }
    }

    /**
     * Encodes a response.
     *
     * @param response The response to encode.
     * @return The bytes to send.
     */
    public static byte[] encodeResponse(TemplateResponse response) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeBoolean(response.accepted());
            out.writeUTF(response.message());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to encode a response", e);
        }
        return buffer.toByteArray();
    }

    /**
     * Decodes a response.
     *
     * @param bytes The received bytes.
     * @return The response.
     * @throws IllegalArgumentException When the bytes do not describe a response.
     */
    public static TemplateResponse decodeResponse(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            return new TemplateResponse(in.readBoolean(), in.readUTF());
        } catch (IOException e) {
            throw new IllegalArgumentException("Malformed response payload", e);
        }
    }
}
