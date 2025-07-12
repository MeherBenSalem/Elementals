package com.naizo.elementals.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateWaterSpellsConfigProcedure {
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		execute();
	}

	public static void execute() {
		execute(null);
	}

	private static void execute(@Nullable Event event) {
		String directory = "";
		String filename = "";
		directory = "elementals";
		filename = "water_spells";
		if (JaumlConfigLib.createConfigFile(directory, filename)) {
			JaumlConfigLib.createConfigFile(directory, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "water_breathing_level")) {
			JaumlConfigLib.setNumberValue(directory, filename, "water_breathing_level", 1);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "dolphin_grace_level")) {
			JaumlConfigLib.setNumberValue(directory, filename, "dolphin_grace_level", 1);
		}
	}
}
