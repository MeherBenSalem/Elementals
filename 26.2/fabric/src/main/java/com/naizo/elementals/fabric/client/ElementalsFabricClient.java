package com.naizo.elementals.fabric.client;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ElementalHudState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ElementalsFabricClient implements ClientModInitializer {
    private static final KeyMapping.Category ELEMENTAL_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "elemental"));
    private static KeyMapping signatureAbility;

    @Override
    public void onInitializeClient() {
        signatureAbility = new KeyMapping("key.elementals.signature_ability", GLFW.GLFW_KEY_F, ELEMENTAL_CATEGORY);
        KeyMappingHelper.registerKeyMapping(signatureAbility);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.gui.screen() == null) {
                while (signatureAbility.consumeClick()) {
                    Elementals.PLATFORM.sendSignatureAbility();
                }
            }
        });
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "elemental_hud"),
                (graphics, tickDelta) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    String texture = ElementalHudState.texture(minecraft.player);
                    if (texture == null) {
                        return;
                    }
                    int h = minecraft.getWindow().getGuiScaledHeight();
                    Identifier textureId = Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "textures/screens/" + texture + ".png");
                    graphics.blit(RenderPipelines.GUI_TEXTURED, textureId, 4, h - 21, 0, 0, 16, 16, 16, 16);
                    graphics.text(minecraft.font, ElementalHudState.levelText(minecraft.player), 10, h - 15, -1, false);
                }
        );
    }
}
