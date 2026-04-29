package com.naizo.elementals.spell;

import com.naizo.elementals.config.ElementalsConfig;
import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ElementalLogic {
    private ElementalLogic() {
    }

    public static void tickPlayer(Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide()) {
            return;
        }
        ElementalPlayerData data = ElementalProgression.data(entity);
        if (data.element == 1) {
            if (data.cooldown > 0) {
                data.cooldown -= 1;
            }
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, (int) data.elementalLevel, false, false));
        } else if (data.element == 2) {
            if (data.cooldown > 0) {
                data.cooldown -= 1;
            }
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, (int) data.elementalLevel, false, false));
            if (data.elementalLevel >= 1) {
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, (int) ElementalsConfig.water("water_breathing_level"), false, false));
            }
            if (data.elementalLevel >= 2) {
                living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, (int) ElementalsConfig.water("dolphin_grace_level"), false, false));
            }
        }
    }
}
