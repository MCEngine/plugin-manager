package io.github.mcengine.universal.mod.neoforge.client;

import io.github.mcengine.universal.api.TemplateAction;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;
import io.github.mcengine.universal.mod.core.TemplatePayloadCodec;
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
@Mod(value = "template_client", dist = net.neoforged.api.distmarker.Dist.CLIENT)
public class TemplateNeoForgeClient {

    /**
     * Registers the payload handlers on the mod event bus.
     *
     * @param modEventBus The bus NeoForge hands each mod at construction.
     */
    public TemplateNeoForgeClient(IEventBus modEventBus) {
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
        registrar.playToServer(TemplatePayloads.Request.TYPE, TemplatePayloads.Request.CODEC,
            (payload, context) -> {
                // Nothing to do on the client for an outgoing type; the server
                // owns this direction.
            });
        registrar.playToClient(TemplatePayloads.Response.TYPE, TemplatePayloads.Response.CODEC,
            (payload, context) -> {
                TemplateResponse response = TemplatePayloadCodec.decodeResponse(payload.data());
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
    public static void send(TemplateAction action, String payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        TemplateRequest request = new TemplateRequest(client.player.getUUID(), action, payload);
        ClientPacketDistributor.sendToServer(
            new TemplatePayloads.Request(TemplatePayloadCodec.encodeRequest(request)));
    }
}
