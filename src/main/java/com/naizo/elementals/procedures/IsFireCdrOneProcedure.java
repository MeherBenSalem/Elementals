package com.naizo.elementals.procedures;

import net.minecraft.world.entity.Entity;

import com.naizo.elementals.network.ElementalsModVariables;

public class IsFireCdrOneProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).element == 1 && entity.getPersistentData().getDouble("el_cdr") <= 50) {
			return true;
		}
		return false;
	}
}
