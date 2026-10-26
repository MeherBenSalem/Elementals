package com.naizo.elementals.neoforge.client;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ClientPowers;
import com.naizo.elementals.client.ClientSetup;
import com.naizo.elementals.client.PowerHudRenderer;
import com.naizo.elementals.client.PowerKeys;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Elementals.MOD_ID, value = Dist.CLIENT)
public final class ElementalsNeoForgeClient {
    private ElementalsNeoForgeClient() {
    }

    @EventBusSubscriber(modid = Elementals.MOD_ID, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            PowerKeys.all().forEach(event::register);
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            ClientSetup.renderers(new ClientSetup.RendererSink() {
                @Override
                public <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
                    event.registerEntityRenderer(type, provider);
                }
            });
        }

        @SubscribeEvent
        public static void layers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.HOTBAR, Compat.id(Elementals.MOD_ID, "powers_hud"),
                    (graphics, tracker) -> PowerHudRenderer.render(graphics, tracker.getGameTimeDeltaPartialTick(false)));
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        PowerKeys.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void loggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientPowers.send(Net.message(Net.HELLO));
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSetup.disconnected();
    }
}
