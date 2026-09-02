package com.naizo.elementals.spell;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class WaterAndFoodSpells {
    private WaterAndFoodSpells() {
    }

    public static void consumeFireSeed(Level level, LivingEntity entity) {
        chooseElement(level, entity, 1, "Fire");
    }

    public static void consumeWaterSeed(Level level, LivingEntity entity) {
        chooseElement(level, entity, 2, "Water");
        ElementalProgression.addExperience(level, entity, 50);
    }

    private static void chooseElement(Level level, LivingEntity entity, int element, String name) {
        if (level.isClientSide()) {
            return;
        }
        ElementalPlayerData data = ElementalProgression.data(entity);
        if (data.element != element) {
            data.element = element;
            data.elementalLevel = Math.max(data.elementalLevel, 0);
            if (entity instanceof ServerPlayer player) {
                player.sendOverlayMessage(Component.literal(name + " element awakened"));
                Elementals.PLATFORM.syncPlayerData(player);
            }
        }
    }
}
