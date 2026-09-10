package io.github.mcengine.universal.mod.fabric.server;

import io.github.mcengine.universal.TemplateProvider;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.mod.core.TemplateChannel;
import io.github.mcengine.universal.mod.core.TemplatePayloadCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server half of the mod.
 *
 * <p>Decodes what the client sent, runs it through the same
 * {@link TemplateProvider} the Bukkit plugin uses, and sends the answer back.
 * Because both sides compile against the shared contract and share one codec,
 * a change to the protocol breaks the build rather than the server.</p>
 */
public class TemplateFabricServer implements ModInitializer {

    /**
     * Log target for this mod.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("template-server");

    /**
     * Registers both payload types, brings the service up, and handles requests.
     */
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(TemplatePayloads.Request.ID, TemplatePayloads.Request.CODEC);
        PayloadTypeRegistry.playS2C().register(TemplatePayloads.Response.ID, TemplatePayloads.Response.CODEC);

        TemplateProvider.create().initialize().exceptionally(error -> {
            LOGGER.error("Failed to start the template service", error);
            return null;
        });

        ServerPlayNetworking.registerGlobalReceiver(TemplatePayloads.Request.ID, (payload, context) -> {
            TemplateRequest request;
            try {
                request = TemplatePayloadCodec.decodeRequest(payload.data());
            } catch (IllegalArgumentException e) {
                // A client sent something this build cannot read -- an older or
                // newer mod, or a tampered packet. Drop it and say so once;
                // never let a malformed payload take the receiver down.
                LOGGER.warn("Dropped a malformed request from {}: {}",
                    context.player().getUuid(), e.getMessage());
                return;
            }

            // The player id is taken from the connection, never from the payload:
            // a client that claims to be someone else must not be believed.
            TemplateRequest trusted = new TemplateRequest(
                context.player().getUuid(), request.action(), request.payload());

            TemplateProvider.instance.handle(trusted).thenAccept(response ->
                context.server().execute(() ->
                    ServerPlayNetworking.send(context.player(),
                        new TemplatePayloads.Response(TemplatePayloadCodec.encodeResponse(response)))));
        });

        LOGGER.info("Template server ready on channel {}", TemplateChannel.REQUEST_ID);
    }
}
