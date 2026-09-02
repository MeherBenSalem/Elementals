package com.naizo.elementals.spell;

import com.naizo.elementals.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;

public final class FireRuneDrops {
    private FireRuneDrops() {
    }

    public static void onLivingDeath(Entity entity) {
        if (entity instanceof Blaze blaze && entity.level() instanceof ServerLevel serverLevel && blaze.getRandom().nextFloat() < 0.2F) {
            entity.spawnAtLocation(serverLevel, ModItems.FIRE_RUNE.get());
        }
    }
}
