package com.naizo.elementals.procedures;

import net.minecraft.world.entity.Entity;

import com.naizo.elementals.network.ElementalsModVariables;
import com.naizo.elementals.configuration.MainConfigConfiguration;

public class FireSignatureProjectileHitsPlayerProcedure {
	public static void execute(Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (!(entity == sourceentity)) {
			if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).element <= 1) {
				entity.setSecondsOnFire((int) (double) MainConfigConfiguration.BURN_TIMER_1.get());
			} else {
				entity.setSecondsOnFire((int) (double) MainConfigConfiguration.BURN_TIMER_2.get());
			}
		}
	}
}
