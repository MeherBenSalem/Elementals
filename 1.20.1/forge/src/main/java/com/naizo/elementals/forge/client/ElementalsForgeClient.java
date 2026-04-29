package com.naizo.elementals.forge.client;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ElementalHudState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Elementals.MOD_ID, value = Dist.CLIENT)
public final class ElementalsForgeClient {
    private static final KeyMapping SIGNATURE_ABILITY = new KeyMapping("key.elementals.signature_ability", GLFW.GLFW_KEY_F, "key.categories.elemental");

    private ElementalsForgeClient() {
    }

    @Mod.EventBusSubscriber(modid = Elementals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(SIGNATURE_ABILITY);
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().screen == null) {
            while (SIGNATURE_ABILITY.consumeClick()) {
                Elementals.PLATFORM.sendSignatureAbility();
            }
        }
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        String texture = ElementalHudState.texture(minecraft.player);
        if (texture == null) {
            return;
        }
        int h = event.getWindow().getGuiScaledHeight();
        event.getGuiGraphics().blit(new ResourceLocation(Elementals.MOD_ID, "textures/screens/" + texture + ".png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
        event.getGuiGraphics().drawString(minecraft.font, ElementalHudState.levelText(minecraft.player), 10, h - 15, -1, false);
    }
}
