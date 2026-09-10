package io.github.mcengine.universal.mod.fabric.client;

import io.github.mcengine.universal.api.TemplateAction;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;
import io.github.mcengine.universal.mod.core.TemplatePayloadCodec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client half of the mod.
 *
 * <p>Sends actions to the server and shows whatever comes back. It never decides
 * anything itself: the server owns the state, so a modified client cannot grant
 * itself a result it was not given. That is the reason the client and server
 * halves are separate jars rather than one jar with two entry points.</p>
 */
public class TemplateFabricClient implements ClientModInitializer {

    /**
     * Log target for this mod.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("template-client");

    /**
     * Registers both payload types and the handler for the server's reply.
     *
     * <p>Both directions are registered here, not just the outgoing one: a
     * payload type the client has not registered is rejected on arrival, so
     * registering only the request would send fine and then silently drop every
     * answer.</p>
     */
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(TemplatePayloads.Request.ID, TemplatePayloads.Request.CODEC);
        PayloadTypeRegistry.playS2C().register(TemplatePayloads.Response.ID, TemplatePayloads.Response.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(TemplatePayloads.Response.ID, (payload, context) -> {
            TemplateResponse response = TemplatePayloadCodec.decodeResponse(payload.data());
            // Hop back to the client thread: the network thread must not touch
            // the game state, and the chat HUD is game state.
            context.client().execute(() -> {
                MinecraftClient client = context.client();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal(response.message()), false);
                }
            });
        });

        LOGGER.info("Template client ready on channel {}",
            io.github.mcengine.universal.mod.core.TemplateChannel.REQUEST_ID);
    }

    /**
     * Sends one action to the server.
     *
     * <p>Public so a keybind, a screen, or another mod can drive it; the mod
     * ships no UI of its own, because what a fork wants to trigger this from is
     * a fork's decision.</p>
     *
     * @param action The action to request.
     * @param payload Free-form argument for the action, or the empty string.
     */
    public static void send(TemplateAction action, String payload) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        if (!ClientPlayNetworking.canSend(TemplatePayloads.Request.ID)) {
            // The server has no matching mod or plugin installed. Saying so beats
            // sending into a channel nothing is listening on.
            client.player.sendMessage(Text.literal("This server does not support the template mod."), false);
            return;
        }
        TemplateRequest request = new TemplateRequest(client.player.getUuid(), action, payload);
        ClientPlayNetworking.send(new TemplatePayloads.Request(TemplatePayloadCodec.encodeRequest(request)));
    }
}
