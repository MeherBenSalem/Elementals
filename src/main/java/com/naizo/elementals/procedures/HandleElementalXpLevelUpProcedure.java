package com.naizo.elementals.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import com.naizo.elementals.network.ElementalsModVariables;

public class HandleElementalXpLevelUpProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, double xp) {
		if (entity == null)
			return;
		if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).elementalLevel < 2) {
			{
				double _setval = (entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).elementalExp + xp;
				entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.elementalExp = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
			if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).elementalExp >= 400
					&& (entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).elementalLevel < 2) {
				{
					double _setval = 0;
					entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.elementalExp = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				{
					double _setval = (entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).elementalLevel + 1;
					entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.elementalLevel = _setval;
						capability.syncPlayerVariables(entity);
					});
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.levelup")), SoundSource.NEUTRAL, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.player.levelup")), SoundSource.NEUTRAL, 1, 1, false);
					}
				}
				if (entity instanceof Player _player && !_player.level().isClientSide())
					_player.displayClientMessage(Component.literal("\u00A76I feel a surge of \u00A74stronger \u00A75elemental power \u00A76coursing through me!"), false);
			}
		}
	}
}
