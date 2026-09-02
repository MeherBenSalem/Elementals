package com.naizo.elementals.neoforge.client;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ElementalHudState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Elementals.MOD_ID, value = Dist.CLIENT)
public final class ElementalsNeoForgeClient {
    public static final KeyMapping.Category ELEMENTAL_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "elemental"));
    public static final KeyMapping SIGNATURE_ABILITY = new KeyMapping("key.elementals.signature_ability", GLFW.GLFW_KEY_F, ELEMENTAL_CATEGORY);

    private ElementalsNeoForgeClient() {
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SIGNATURE_ABILITY);
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().gui.screen() == null) {
            while (SIGNATURE_ABILITY.consumeClick()) {
                Elementals.PLATFORM.sendSignatureAbility();
            }
        }
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.HOTBAR.equals(event.getName())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        String texture = ElementalHudState.texture(minecraft.player);
        if (texture == null) {
            return;
        }
        int h = minecraft.getWindow().getGuiScaledHeight();
        Identifier textureId = Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "textures/screens/" + texture + ".png");
        event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, textureId, 4, h - 21, 0, 0, 16, 16, 16, 16);
        event.getGuiGraphics().text(minecraft.font, ElementalHudState.levelText(minecraft.player), 10, h - 15, -1, false);
    }
}
