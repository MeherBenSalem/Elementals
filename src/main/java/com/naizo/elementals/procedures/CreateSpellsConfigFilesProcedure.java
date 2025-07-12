package com.naizo.elementals.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreateSpellsConfigFilesProcedure {
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
		filename = "fire_spells";
		if (JaumlConfigLib.createConfigFile(directory, filename)) {
			JaumlConfigLib.createConfigFile(directory, filename);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "burn_timer_signature_level_0")) {
			JaumlConfigLib.setNumberValue(directory, filename, "burn_timer_signature_level_0", 3);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "burn_timer_signature_level_1")) {
			JaumlConfigLib.setNumberValue(directory, filename, "burn_timer_signature_level_1", 5);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "explosive_orb_explosion_power")) {
			JaumlConfigLib.setNumberValue(directory, filename, "explosive_orb_explosion_power", 6);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "explosive_orb_cdr")) {
			JaumlConfigLib.setNumberValue(directory, filename, "explosive_orb_cdr", 100);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "ember_shield_absorption_lvl")) {
			JaumlConfigLib.setNumberValue(directory, filename, "ember_shield_absorption_lvl", 3);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "ember_shield_cdr")) {
			JaumlConfigLib.setNumberValue(directory, filename, "ember_shield_cdr", 200);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "inferno_wave_damage")) {
			JaumlConfigLib.setNumberValue(directory, filename, "inferno_wave_damage", 3);
		}
		if (!JaumlConfigLib.arrayKeyExists(directory, filename, "inferno_wave_cdr")) {
			JaumlConfigLib.setNumberValue(directory, filename, "inferno_wave_cdr", 300);
		}
	}
}
