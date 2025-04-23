
package com.naizo.elementals.client.screens;

import org.checkerframework.checker.units.qual.h;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import com.naizo.elementals.procedures.ReturnElementalLevelProcedure;
import com.naizo.elementals.procedures.IsWaterNoCdrProcedure;
import com.naizo.elementals.procedures.IsWaterCdrZeroProcedure;
import com.naizo.elementals.procedures.IsWaterCdrTwoProcedure;
import com.naizo.elementals.procedures.IsWaterCdrOneProcedure;
import com.naizo.elementals.procedures.IsFireNoCdrProcedure;
import com.naizo.elementals.procedures.IsFireCdrZeroProcedure;
import com.naizo.elementals.procedures.IsFireCdrTwoProcedure;
import com.naizo.elementals.procedures.IsFireCdrOneProcedure;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class MainOverlayOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getWindow().getGuiScaledWidth();
		int h = event.getWindow().getGuiScaledHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		if (true) {
			if (IsFireCdrZeroProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/fire_0.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			if (IsFireCdrOneProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/fire_1.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			if (IsFireCdrTwoProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/fire_2.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			if (IsFireNoCdrProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/fire.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			if (IsWaterCdrZeroProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/water_0.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			if (IsWaterCdrOneProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/water_1.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			if (IsWaterCdrTwoProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/water_2.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			if (IsWaterNoCdrProcedure.execute(entity)) {
				event.getGuiGraphics().blit(new ResourceLocation("elementals:textures/screens/water.png"), 4, h - 21, 0, 0, 16, 16, 16, 16);
			}
			event.getGuiGraphics().drawString(Minecraft.getInstance().font,

					ReturnElementalLevelProcedure.execute(entity), 10, h - 15, -1, false);
		}
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}
