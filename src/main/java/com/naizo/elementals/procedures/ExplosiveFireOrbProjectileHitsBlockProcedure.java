package com.naizo.elementals.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

public class ExplosiveFireOrbProjectileHitsBlockProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (world instanceof Level _level && !_level.isClientSide())
			_level.explode(null, x, y, z, (float) JaumlConfigLib.getNumberValue("elementals", "fire_spells", "explosive_orb_explosion_power"), Level.ExplosionInteraction.TNT);
	}
}
