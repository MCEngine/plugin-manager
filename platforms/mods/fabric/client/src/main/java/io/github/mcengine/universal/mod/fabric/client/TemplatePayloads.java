package io.github.mcengine.universal.mod.fabric.client;

import io.github.mcengine.universal.mod.core.TemplateChannel;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * The two custom payloads this mod puts on the wire.
 *
 * <p>Both are a thin wrapper around a {@code byte[]} produced by
 * {@link io.github.mcengine.universal.mod.core.TemplatePayloadCodec}.
 * Keeping the wrapper this thin is deliberate: the byte layout, which is the
 * part client and server must agree on exactly, lives once in the shared module
 * and is unit-tested there. What is duplicated per loader is only the plumbing,
 * because {@code CustomPayload} is a Minecraft type and each loader sees it
 * under different mappings.</p>
 */
public final class TemplatePayloads {

    /**
     * Not instantiable.
     */
    private TemplatePayloads() {
    }

    /**
     * A request travelling from the client to the server.
     *
     * @param data The encoded request.
     */
    public record Request(byte[] data) implements CustomPayload {

        /**
         * The channel this payload travels on.
         */
        public static final CustomPayload.Id<Request> ID = new CustomPayload.Id<>(
            Identifier.of(TemplateChannel.NAMESPACE, TemplateChannel.REQUEST_PATH));

        /**
         * Reads and writes the payload's bytes.
         */
        public static final PacketCodec<RegistryByteBuf, Request> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeByteArray(value.data()),
            buf -> new Request(buf.readByteArray()));

        /**
         * {@inheritDoc}
         */
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * A response travelling from the server back to the client.
     *
     * @param data The encoded response.
     */
    public record Response(byte[] data) implements CustomPayload {

        /**
         * The channel this payload travels on.
         */
        public static final CustomPayload.Id<Response> ID = new CustomPayload.Id<>(
            Identifier.of(TemplateChannel.NAMESPACE, TemplateChannel.RESPONSE_PATH));

        /**
         * Reads and writes the payload's bytes.
         */
        public static final PacketCodec<RegistryByteBuf, Response> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeByteArray(value.data()),
            buf -> new Response(buf.readByteArray()));

        /**
         * {@inheritDoc}
         */
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
