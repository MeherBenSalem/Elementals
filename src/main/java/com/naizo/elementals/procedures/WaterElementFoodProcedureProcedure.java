package com.naizo.elementals.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import com.naizo.elementals.network.ElementalsModVariables;

public class WaterElementFoodProcedureProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).element != 2) {
			{
				double _setval = 2;
				entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.element = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("\u00A7bThe ancient tides stir within you! \u00A73You are now infused with Elemental Water Power! \u00A79Master the flow, command the depths, and let your watery destiny surge forth!"),
						false);
		} else {
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("\u00A74+50"), true);
			HandleElementalXpLevelUpProcedure.execute(world, x, y, z, entity, 50);
		}
	}
}
