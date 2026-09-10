package io.github.mcengine.pluginmanager.mod.forge.server;

import io.github.mcengine.pluginmanager.MCPluginManagerProvider;
import io.github.mcengine.pluginmanager.api.MCPluginManagerRequest;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerChannel;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerPayloadCodec;
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
 * {@link MCPluginManagerProvider} the Bukkit plugin uses, and sends the answer back.</p>
 */
@Mod("mcpluginmanager_server")
public class MCPluginManagerForgeServer {

    /**
     * Log target for this mod.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("mcpluginmanager-server");

    /**
     * The channel both payloads travel on.
     */
    private static final PayloadChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(MCPluginManagerChannel.NAMESPACE, MCPluginManagerChannel.REQUEST_PATH))
        .optional()
        .networkProtocolVersion(1)
        .payloadChannel()
        .play()
        .serverbound()
        .add(MCPluginManagerPayloads.Request.class, MCPluginManagerPayloads.Request.CODEC,
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

                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }

                // The player id comes from the connection, never from the
                // payload: a client claiming to be someone else is not believed.
                MCPluginManagerRequest trusted =
                    new MCPluginManagerRequest(player.getUUID(), request.action(), request.payload());

                MCPluginManagerProvider.instance.handle(trusted).thenAccept(response ->
                    context.enqueueWork(() -> CHANNEL.send(
                        new MCPluginManagerPayloads.Response(MCPluginManagerPayloadCodec.encodeResponse(response)),
                        player.connection.getConnection())));
            })
        .clientbound()
        .add(MCPluginManagerPayloads.Response.class, MCPluginManagerPayloads.Response.CODEC,
            (payload, context) -> {
                // The client owns this direction.
            })
        .build();

    /**
     * Brings the service up.
     *
     * @param modEventBus The bus Forge hands each mod at construction.
     */
    public MCPluginManagerForgeServer(IEventBus modEventBus) {
        MCPluginManagerProvider.create().initialize().exceptionally(error -> {
            LOGGER.error("Failed to start the MCPluginManager service", error);
            return null;
        });
        LOGGER.info("MCPluginManager server ready on channel {}", MCPluginManagerChannel.REQUEST_ID);
    }
}
