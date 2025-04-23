package com.naizo.elementals.init;

import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.naizo.elementals.configuration.MainConfigConfiguration;
import com.naizo.elementals.ElementalsMod;

@Mod.EventBusSubscriber(modid = ElementalsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ElementalsModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MainConfigConfiguration.SPEC, "elementals_main.toml");
		});
	}
}
