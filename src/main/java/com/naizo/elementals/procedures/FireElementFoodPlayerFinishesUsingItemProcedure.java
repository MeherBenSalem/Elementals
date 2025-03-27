package com.naizo.elementals.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import com.naizo.elementals.network.ElementalsModVariables;

public class FireElementFoodPlayerFinishesUsingItemProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).element != 1) {
			{
				double _setval = 1;
				entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.element = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("\u00A7cThe ancient flames awaken within you! \u00A76You are now infused with Elemental Fire Power! \u00A74Master the blaze, wield the heat, and let your fiery destiny burn bright!"),
						false);
		}
	}
}
