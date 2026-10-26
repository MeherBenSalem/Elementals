package com.naizo.elementals.fabric.client;

import com.naizo.elementals.client.ClientPowers;
import com.naizo.elementals.client.ClientSetup;
import com.naizo.elementals.client.PowerHudRenderer;
import com.naizo.elementals.client.PowerKeys;
import com.naizo.elementals.fabric.FabricPayloads;
import com.naizo.elementals.net.Net;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ElementalsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PowerKeys.all().forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(PowerKeys::tick);
        HudRenderCallback.EVENT.register((graphics, tracker) -> PowerHudRenderer.render(graphics, tracker.getGameTimeDeltaPartialTick(false)));
        ClientPlayNetworking.registerGlobalReceiver(FabricPayloads.ToClient.TYPE,
                (payload, context) -> context.client().execute(() -> ClientPowers.handle(payload.json())));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPowers.send(Net.message(Net.HELLO)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(ClientSetup::disconnected));
        ClientSetup.renderers(new ClientSetup.RendererSink() {
            @Override
            public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                EntityRendererRegistry.register(type, provider);
            }
        });
    }
}
