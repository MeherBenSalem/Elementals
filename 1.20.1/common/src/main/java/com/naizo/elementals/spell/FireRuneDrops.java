package com.naizo.elementals.spell;

import com.naizo.elementals.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;

public final class FireRuneDrops {
    private FireRuneDrops() {
    }

    public static void onLivingDeath(Entity entity) {
        if (entity instanceof Blaze blaze && !entity.level().isClientSide() && blaze.getRandom().nextFloat() < 0.2F) {
            entity.spawnAtLocation(ModItems.FIRE_RUNE.get());
        }
    }
}
