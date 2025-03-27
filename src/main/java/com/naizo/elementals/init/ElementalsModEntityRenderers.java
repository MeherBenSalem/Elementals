
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.naizo.elementals.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import com.naizo.elementals.client.renderer.FireGolemRenderer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ElementalsModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ElementalsModEntities.FIRE_SIGNATURE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ElementalsModEntities.FIRE_GOLEM.get(), FireGolemRenderer::new);
		event.registerEntityRenderer(ElementalsModEntities.EXPLOSIVE_FIRE_ORB.get(), ThrownItemRenderer::new);
	}
}
