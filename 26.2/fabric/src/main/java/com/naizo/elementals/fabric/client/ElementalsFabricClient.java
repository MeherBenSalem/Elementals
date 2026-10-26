package com.naizo.elementals.fabric.client;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ClientPowers;
import com.naizo.elementals.client.ClientSetup;
import com.naizo.elementals.client.PowerHudRenderer;
import com.naizo.elementals.client.PowerKeys;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.fabric.FabricPayloads;
import com.naizo.elementals.net.Net;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class ElementalsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PowerKeys.all().forEach(KeyMappingHelper::registerKeyMapping);
        ClientTickEvents.END_CLIENT_TICK.register(PowerKeys::tick);
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Compat.id(Elementals.MOD_ID, "powers_hud"),
                (graphics, tracker) -> PowerHudRenderer.render(graphics, tracker.getGameTimeDeltaPartialTick(false)));
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
