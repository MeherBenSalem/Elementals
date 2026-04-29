package com.naizo.elementals.fabric.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ElementalHudState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class ElementalsFabricClient implements ClientModInitializer {
    private static KeyMapping signatureAbility;

    @Override
    public void onInitializeClient() {
        signatureAbility = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.elementals.signature_ability", GLFW.GLFW_KEY_F, "key.categories.elemental"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (signatureAbility.consumeClick()) {
                Elementals.PLATFORM.sendSignatureAbility();
            }
        });
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            Minecraft minecraft = Minecraft.getInstance();
            String texture = ElementalHudState.texture(minecraft.player);
            if (texture == null) {
                return;
            }
            int h = minecraft.getWindow().getGuiScaledHeight();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            graphics.blit(new ResourceLocation(Elementals.MOD_ID, "textures/screens/" + texture + ".png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
            graphics.drawString(minecraft.font, ElementalHudState.levelText(minecraft.player), 10, h - 15, -1, false);
        });
    }
}
