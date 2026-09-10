package io.github.mcengine.pluginmanager.mod.neoforge.client;

import io.github.mcengine.pluginmanager.api.MCPluginManagerAction;
import io.github.mcengine.pluginmanager.api.MCPluginManagerRequest;
import io.github.mcengine.pluginmanager.api.MCPluginManagerResponse;
import io.github.mcengine.pluginmanager.mod.core.MCPluginManagerPayloadCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Client half of the NeoForge mod.
 *
 * <p>Sends actions to the server and renders whatever comes back. It decides
 * nothing itself, so a modified client cannot grant itself a result the server
 * did not give it.</p>
 */
@Mod(value = "mcpluginmanager_client", dist = net.neoforged.api.distmarker.Dist.CLIENT)
public class MCPluginManagerNeoForgeClient {

    /**
     * Registers the payload handlers on the mod event bus.
     *
     * @param modEventBus The bus NeoForge hands each mod at construction.
     */
    public MCPluginManagerNeoForgeClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
    }

    /**
     * Registers both directions.
     *
     * <p>Both, not just the outgoing one: a payload type this side has not
     * registered is rejected on arrival, so registering only the request would
     * send fine and then silently drop every answer.</p>
     *
     * @param event The registration event.
     */
    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(MCPluginManagerPayloads.Request.TYPE, MCPluginManagerPayloads.Request.CODEC,
            (payload, context) -> {
                // Nothing to do on the client for an outgoing type; the server
                // owns this direction.
            });
        registrar.playToClient(MCPluginManagerPayloads.Response.TYPE, MCPluginManagerPayloads.Response.CODEC,
            (payload, context) -> {
                MCPluginManagerResponse response = MCPluginManagerPayloadCodec.decodeResponse(payload.data());
                context.enqueueWork(() -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.player != null) {
                        client.player.displayClientMessage(Component.literal(response.message()), false);
                    }
                });
            });
    }

    /**
     * Sends one action to the server.
     *
     * <p>Public so a keybind, a screen, or another mod can drive it; the mod
     * ships no UI of its own, because what a fork triggers this from is a fork's
     * decision.</p>
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
        ClientPacketDistributor.sendToServer(
            new MCPluginManagerPayloads.Request(MCPluginManagerPayloadCodec.encodeRequest(request)));
    }
}
