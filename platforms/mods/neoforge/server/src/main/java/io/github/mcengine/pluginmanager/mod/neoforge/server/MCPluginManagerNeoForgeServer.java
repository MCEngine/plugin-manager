package io.github.mcengine.pluginmanager.mod.neoforge.server;

import io.github.mcengine.pluginmanager.MCPluginManagerProvider;
import io.github.mcengine.pluginmanager.api.MCPluginManagerRequest;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerPayloadCodec;
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
 * {@link MCPluginManagerProvider} the Bukkit plugin uses, and sends the answer back.</p>
 */
@Mod("mcpluginmanager_server")
public class MCPluginManagerNeoForgeServer {

    /**
     * Log target for this mod.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("mcpluginmanager-server");

    /**
     * Brings the service up and registers the payload handlers.
     *
     * @param modEventBus The bus NeoForge hands each mod at construction.
     */
    public MCPluginManagerNeoForgeServer(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);

        MCPluginManagerProvider.create().initialize().exceptionally(error -> {
            LOGGER.error("Failed to start the MCPluginManager service", error);
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
        registrar.playToServer(MCPluginManagerPayloads.Request.TYPE, MCPluginManagerPayloads.Request.CODEC,
            (payload, context) -> {
                MCPluginManagerRequest request;
                try {
                    request = MCPluginManagerPayloadCodec.decodeRequest(payload.data());
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
                MCPluginManagerRequest trusted =
                    new MCPluginManagerRequest(player.getUUID(), request.action(), request.payload());

                MCPluginManagerProvider.instance.handle(trusted).thenAccept(response ->
                    context.enqueueWork(() -> PacketDistributor.sendToPlayer(player,
                        new MCPluginManagerPayloads.Response(MCPluginManagerPayloadCodec.encodeResponse(response)))));
            });
        registrar.playToClient(MCPluginManagerPayloads.Response.TYPE, MCPluginManagerPayloads.Response.CODEC,
            (payload, context) -> {
                // The client owns this direction.
            });
    }
}
