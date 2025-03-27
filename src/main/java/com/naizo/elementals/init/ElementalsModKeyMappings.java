
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package com.naizo.elementals.init;

import org.lwjgl.glfw.GLFW;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import com.naizo.elementals.network.SignatureAbilityMessage;
import com.naizo.elementals.ElementalsMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class ElementalsModKeyMappings {
	public static final KeyMapping SIGNATURE_ABILITY = new KeyMapping("key.elementals.signature_ability", GLFW.GLFW_KEY_F, "key.categories.elemental") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				ElementalsMod.PACKET_HANDLER.sendToServer(new SignatureAbilityMessage(0, 0));
				SignatureAbilityMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(SIGNATURE_ABILITY);
	}

	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				SIGNATURE_ABILITY.consumeClick();
			}
		}
	}
}
