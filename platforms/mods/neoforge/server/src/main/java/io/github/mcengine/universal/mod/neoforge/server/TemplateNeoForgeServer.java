package io.github.mcengine.universal.mod.neoforge.server;

import io.github.mcengine.universal.TemplateProvider;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.mod.core.TemplatePayloadCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server half of the NeoForge mod.
 *
 * <p>Decodes what the client sent, runs it through the same
 * {@link TemplateProvider} the Bukkit plugin uses, and sends the answer back.</p>
 */
@Mod("template_server")
public class TemplateNeoForgeServer {

    /**
     * Log target for this mod.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("template-server");

    /**
     * Brings the service up and registers the payload handlers.
     *
     * @param modEventBus The bus NeoForge hands each mod at construction.
     */
    public TemplateNeoForgeServer(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);

        TemplateProvider.create().initialize().exceptionally(error -> {
            LOGGER.error("Failed to start the template service", error);
            return null;
        });
    }

    /**
     * Registers both directions.
     *
     * @param event The registration event.
     */
    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TemplatePayloads.Request.TYPE, TemplatePayloads.Request.CODEC,
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

                // The player id comes from the connection, never from the
                // payload: a client claiming to be someone else is not believed.
                ServerPlayer player = (ServerPlayer) context.player();
                TemplateRequest trusted =
                    new TemplateRequest(player.getUUID(), request.action(), request.payload());

                TemplateProvider.instance.handle(trusted).thenAccept(response ->
                    context.enqueueWork(() -> PacketDistributor.sendToPlayer(player,
                        new TemplatePayloads.Response(TemplatePayloadCodec.encodeResponse(response)))));
            });
        registrar.playToClient(TemplatePayloads.Response.TYPE, TemplatePayloads.Response.CODEC,
            (payload, context) -> {
                // The client owns this direction.
            });
    }
}
