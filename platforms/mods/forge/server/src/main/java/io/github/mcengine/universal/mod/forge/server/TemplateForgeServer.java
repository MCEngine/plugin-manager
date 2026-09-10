package io.github.mcengine.universal.mod.forge.server;

import io.github.mcengine.universal.TemplateProvider;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.mod.core.TemplateChannel;
import io.github.mcengine.universal.mod.core.TemplatePayloadCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PayloadChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server half of the Forge mod.
 *
 * <p>Decodes what the client sent, runs it through the same
 * {@link TemplateProvider} the Bukkit plugin uses, and sends the answer back.</p>
 */
@Mod("template_server")
public class TemplateForgeServer {

    /**
     * Log target for this mod.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("template-server");

    /**
     * The channel both payloads travel on.
     */
    private static final PayloadChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(TemplateChannel.NAMESPACE, TemplateChannel.REQUEST_PATH))
        .optional()
        .networkProtocolVersion(1)
        .payloadChannel()
        .play()
        .serverbound()
        .add(TemplatePayloads.Request.class, TemplatePayloads.Request.CODEC,
            (payload, context) -> {
                TemplateRequest request;
                try {
                    request = TemplatePayloadCodec.decodeRequest(payload.data());
                } catch (IllegalArgumentException e) {
                    // An older or newer client, or a tampered packet. Drop it and
                    // say so once; never let a malformed payload take the handler
                    // down.
                    LOGGER.warn("Dropped a malformed request: {}", e.getMessage());
                    return;
                }

                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }

                // The player id comes from the connection, never from the
                // payload: a client claiming to be someone else is not believed.
                TemplateRequest trusted =
                    new TemplateRequest(player.getUUID(), request.action(), request.payload());

                TemplateProvider.instance.handle(trusted).thenAccept(response ->
                    context.enqueueWork(() -> CHANNEL.send(
                        new TemplatePayloads.Response(TemplatePayloadCodec.encodeResponse(response)),
                        player.connection.getConnection())));
            })
        .clientbound()
        .add(TemplatePayloads.Response.class, TemplatePayloads.Response.CODEC,
            (payload, context) -> {
                // The client owns this direction.
            })
        .build();

    /**
     * Brings the service up.
     *
     * @param modEventBus The bus Forge hands each mod at construction.
     */
    public TemplateForgeServer(IEventBus modEventBus) {
        TemplateProvider.create().initialize().exceptionally(error -> {
            LOGGER.error("Failed to start the template service", error);
            return null;
        });
        LOGGER.info("Template server ready on channel {}", TemplateChannel.REQUEST_ID);
    }
}
