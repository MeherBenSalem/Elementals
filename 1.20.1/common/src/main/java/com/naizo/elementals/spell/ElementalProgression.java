package com.naizo.elementals.spell;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public final class ElementalProgression {
    private ElementalProgression() {
    }

    public static ElementalPlayerData data(Entity entity) {
        return Elementals.PLATFORM.playerData(entity);
    }

    public static void addExperience(LevelAccessor level, Entity entity, double amount) {
        if (entity == null || level.isClientSide()) {
            return;
        }
        ElementalPlayerData data = data(entity);
        if (data.elementalLevel >= 2) {
            return;
        }
        data.elementalExp += amount;
        if (data.elementalExp >= 400 && data.elementalLevel < 2) {
            data.elementalExp = 0;
            data.elementalLevel += 1;
            entity.level().playSound(null, entity.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1, 1);
            if (entity instanceof ServerPlayer player) {
                player.displayClientMessage(Component.literal("Elemental level up!"), true);
            }
        }
        if (entity instanceof ServerPlayer player) {
            Elementals.PLATFORM.syncPlayerData(player);
        }
    }
}
