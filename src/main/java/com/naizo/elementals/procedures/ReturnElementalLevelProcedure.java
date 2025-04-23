package com.naizo.elementals.procedures;

import net.minecraft.world.entity.Entity;

import com.naizo.elementals.network.ElementalsModVariables;

public class ReturnElementalLevelProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return new java.text.DecimalFormat("##").format((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).elementalLevel);
	}
}
