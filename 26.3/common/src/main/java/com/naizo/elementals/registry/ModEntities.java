package com.naizo.elementals.registry;

import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public final class ModEntities {
    public static Supplier<EntityType<FireSignatureEntity>> FIRE_SIGNATURE;
    public static Supplier<EntityType<ExplosiveFireOrbEntity>> EXPLOSIVE_FIRE_ORB;
    public static Supplier<EntityType<FireGolemEntity>> FIRE_GOLEM;

    private ModEntities() {
    }
}
