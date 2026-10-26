package com.naizo.elementals.forge.client;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ClientPowers;
import com.naizo.elementals.client.ClientSetup;
import com.naizo.elementals.client.PowerHudRenderer;
import com.naizo.elementals.client.PowerKeys;
import com.naizo.elementals.net.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Elementals.MOD_ID, value = Dist.CLIENT)
public final class ElementalsForgeClient {
    private ElementalsForgeClient() {
    }

    @Mod.EventBusSubscriber(modid = Elementals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
        public static void overlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "powers_hud",
                    (gui, graphics, partialTick, width, height) -> PowerHudRenderer.render(graphics, partialTick));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PowerKeys.tick(Minecraft.getInstance());
        }
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
