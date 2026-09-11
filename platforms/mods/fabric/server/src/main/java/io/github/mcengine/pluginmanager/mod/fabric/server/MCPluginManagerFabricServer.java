package io.github.mcengine.pluginmanager.mod.fabric.server;

import io.github.mcengine.pluginmanager.MCPluginManagerProvider;
import io.github.mcengine.pluginmanager.api.MCPluginManagerRequest;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerChannel;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerPayloadCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server half of the mod.
 *
 * <p>Decodes what the client sent, runs it through the same
 * {@link MCPluginManagerProvider} the Bukkit plugin uses, and sends the answer back.
 * Because both sides compile against the shared contract and share one codec,
 * a change to the protocol breaks the build rather than the server.</p>
 */
public class MCPluginManagerFabricServer implements ModInitializer {

    /**
     * Log target for this mod.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("mcpluginmanager-server");

    /**
     * Registers both payload types, brings the service up, and handles requests.
     */
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(MCPluginManagerPayloads.Request.ID, MCPluginManagerPayloads.Request.CODEC);
        PayloadTypeRegistry.playS2C().register(MCPluginManagerPayloads.Response.ID, MCPluginManagerPayloads.Response.CODEC);

        MCPluginManagerProvider.create().initialize().exceptionally(error -> {
            LOGGER.error("Failed to start the MCPluginManager service", error);
            return null;
        });

        ServerPlayNetworking.registerGlobalReceiver(MCPluginManagerPayloads.Request.ID, (payload, context) -> {
            MCPluginManagerRequest request;
            try {
                request = MCPluginManagerPayloadCodec.decodeRequest(payload.data());
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
            MCPluginManagerRequest trusted = new MCPluginManagerRequest(
                context.player().getUuid(), request.action(), request.payload());

            MCPluginManagerProvider.instance.handle(trusted).thenAccept(response ->
                context.server().execute(() ->
                    ServerPlayNetworking.send(context.player(),
                        new MCPluginManagerPayloads.Response(MCPluginManagerPayloadCodec.encodeResponse(response)))));
        });

        LOGGER.info("MCPluginManager server ready on channel {}", MCPluginManagerChannel.REQUEST_ID);
    }
}
