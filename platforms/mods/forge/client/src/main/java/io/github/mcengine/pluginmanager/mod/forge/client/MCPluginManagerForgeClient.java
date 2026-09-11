package io.github.mcengine.pluginmanager.mod.forge.client;

import io.github.mcengine.pluginmanager.api.MCPluginManagerAction;
import io.github.mcengine.pluginmanager.api.MCPluginManagerRequest;
import io.github.mcengine.pluginmanager.api.MCPluginManagerResponse;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerChannel;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerPayloadCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PayloadChannel;

/**
 * Client half of the Forge mod.
 *
 * <p>Sends actions to the server and renders whatever comes back. It decides
 * nothing itself, so a modified client cannot grant itself a result the server
 * did not give it.</p>
 */
@Mod("mcpluginmanager_client")
public class MCPluginManagerForgeClient {

    /**
     * The channel both payloads travel on, versioned so an old client and a new
     * server refuse each other rather than misreading each other's bytes.
     */
    private static final PayloadChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(MCPluginManagerChannel.NAMESPACE, MCPluginManagerChannel.REQUEST_PATH))
        .optional()
        .networkProtocolVersion(1)
        .payloadChannel()
        .play()
        .clientbound()
        .add(MCPluginManagerPayloads.Response.class, MCPluginManagerPayloads.Response.CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                MCPluginManagerResponse response = MCPluginManagerPayloadCodec.decodeResponse(payload.data());
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal(response.message()));
                }
            }))
        .serverbound()
        .add(MCPluginManagerPayloads.Request.class, MCPluginManagerPayloads.Request.CODEC,
            (payload, context) -> {
                // The server owns this direction; declared so the type is known
                // to the channel on both sides.
            })
        .build();

    /**
     * Forge constructs each mod with its event bus.
     *
     * @param modEventBus The bus Forge hands each mod at construction.
     */
    public MCPluginManagerForgeClient(IEventBus modEventBus) {
        // The channel is built in the static initializer above; nothing else to
        // register on the mod bus for this mod.
    }

    /**
     * Sends one action to the server.
     *
     * @param action The action to request.
     * @param payload Free-form argument for the action, or the empty string.
     */
    public static void send(MCPluginManagerAction action, String payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        MCPluginManagerRequest request = new MCPluginManagerRequest(client.player.getUUID(), action, payload);
        CHANNEL.send(new MCPluginManagerPayloads.Request(MCPluginManagerPayloadCodec.encodeRequest(request)),
            client.getConnection().getConnection());
    }
}
