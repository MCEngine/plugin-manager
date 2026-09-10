package io.github.mcengine.universal.mod.forge.client;

import io.github.mcengine.universal.api.TemplateAction;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;
import io.github.mcengine.universal.mod.core.TemplateChannel;
import io.github.mcengine.universal.mod.core.TemplatePayloadCodec;
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
@Mod("template_client")
public class TemplateForgeClient {

    /**
     * The channel both payloads travel on, versioned so an old client and a new
     * server refuse each other rather than misreading each other's bytes.
     */
    private static final PayloadChannel CHANNEL = ChannelBuilder
        .named(Identifier.fromNamespaceAndPath(TemplateChannel.NAMESPACE, TemplateChannel.REQUEST_PATH))
        .optional()
        .networkProtocolVersion(1)
        .payloadChannel()
        .play()
        .clientbound()
        .add(TemplatePayloads.Response.class, TemplatePayloads.Response.CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                TemplateResponse response = TemplatePayloadCodec.decodeResponse(payload.data());
                Minecraft client = Minecraft.getInstance();
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal(response.message()));
                }
            }))
        .serverbound()
        .add(TemplatePayloads.Request.class, TemplatePayloads.Request.CODEC,
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
    public TemplateForgeClient(IEventBus modEventBus) {
        // The channel is built in the static initializer above; nothing else to
        // register on the mod bus for this template.
    }

    /**
     * Sends one action to the server.
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
        CHANNEL.send(new TemplatePayloads.Request(TemplatePayloadCodec.encodeRequest(request)),
            client.getConnection().getConnection());
    }
}
