package com.naizo.elementals.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.HumanoidArm;

/** Draws the HUD from a loader's HUD event. Minecraft 26.2 and 26.3 edition. */
public final class PowerHudRenderer {
    private PowerHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        GuiCanvas canvas = new GuiCanvas(graphics);
        PowerHud.render(canvas, minecraft.level.getGameTime(), partialTick, PowerKeys::castKey, PowerKeys.treeKey(), minecraft.gui.hud.isHidden(),
                minecraft.player.getMainArm() == HumanoidArm.LEFT && !minecraft.player.getOffhandItem().isEmpty());
        canvas.finish();
    }
}
