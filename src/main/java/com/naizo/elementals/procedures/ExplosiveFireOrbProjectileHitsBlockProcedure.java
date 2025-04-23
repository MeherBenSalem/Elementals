package com.naizo.elementals.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import com.naizo.elementals.configuration.MainConfigConfiguration;

public class ExplosiveFireOrbProjectileHitsBlockProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (world instanceof Level _level && !_level.isClientSide())
			_level.explode(null, x, y, z, (float) (double) MainConfigConfiguration.EXPLOSION_POWER.get(), Level.ExplosionInteraction.TNT);
	}
}
