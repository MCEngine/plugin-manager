package io.github.mcengine.universal.mod.neoforge.client;

import io.github.mcengine.universal.mod.core.TemplateChannel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * The two custom payloads this mod puts on the wire.
 *
 * <p>The same two channels the Fabric modules use, spelled in Mojang mappings
 * rather than Yarn. The bytes inside are produced by the shared
 * {@link io.github.mcengine.universal.mod.core.TemplatePayloadCodec}, so
 * a NeoForge client and a Fabric server would still understand each other.</p>
 *
 * <p>Note the argument order: NeoForge's {@code StreamCodec.of} takes
 * {@code (buffer, value)} where Fabric's {@code PacketCodec.of} takes
 * {@code (value, buffer)}. Getting it backwards compiles and then writes
 * nonsense, so it is worth reading twice.</p>
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
    public record Request(byte[] data) implements CustomPacketPayload {

        /**
         * The channel this payload travels on.
         */
        public static final CustomPacketPayload.Type<Request> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(TemplateChannel.NAMESPACE, TemplateChannel.REQUEST_PATH));

        /**
         * Reads and writes the payload's bytes.
         */
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeByteArray(value.data()),
            buf -> new Request(buf.readByteArray()));

        /**
         * {@inheritDoc}
         */
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * A response travelling from the server back to the client.
     *
     * @param data The encoded response.
     */
    public record Response(byte[] data) implements CustomPacketPayload {

        /**
         * The channel this payload travels on.
         */
        public static final CustomPacketPayload.Type<Response> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(TemplateChannel.NAMESPACE, TemplateChannel.RESPONSE_PATH));

        /**
         * Reads and writes the payload's bytes.
         */
        public static final StreamCodec<RegistryFriendlyByteBuf, Response> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeByteArray(value.data()),
            buf -> new Response(buf.readByteArray()));

        /**
         * {@inheritDoc}
         */
        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
