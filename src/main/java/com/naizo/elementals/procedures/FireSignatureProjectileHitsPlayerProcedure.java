package com.naizo.elementals.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.world.entity.Entity;

import com.naizo.elementals.network.ElementalsModVariables;

public class FireSignatureProjectileHitsPlayerProcedure {
	public static void execute(Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (!(entity == sourceentity)) {
			if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).element <= 1) {
				entity.setSecondsOnFire((int) JaumlConfigLib.getNumberValue("elementals", "fire_spells", "burn_timer_signature_level_0"));
			} else {
				entity.setSecondsOnFire((int) JaumlConfigLib.getNumberValue("elementals", "fire_spells", "burn_timer_signature_level_1"));
			}
		}
	}
}
