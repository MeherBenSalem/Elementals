package com.naizo.elementals.procedures;

import net.minecraft.world.entity.Entity;

public class FireSignatureProjectileHitsPlayerProcedure {
	public static void execute(Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (!(entity == sourceentity)) {
			entity.setSecondsOnFire(3);
		}
	}
}
