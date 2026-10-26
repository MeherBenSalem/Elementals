package com.naizo.elementals.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.HumanoidArm;

/** Draws the HUD from a loader's HUD event. Minecraft 1.21.1 edition. */
public final class PowerHudRenderer {
    private PowerHudRenderer() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        GuiCanvas canvas = new GuiCanvas(graphics);
        PowerHud.render(canvas, minecraft.level.getGameTime(), partialTick, PowerKeys::castKey, PowerKeys.treeKey(), minecraft.options.hideGui,
                minecraft.player.getMainArm() == HumanoidArm.LEFT && !minecraft.player.getOffhandItem().isEmpty());
        canvas.finish();
    }
}
