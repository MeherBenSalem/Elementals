package com.naizo.elementals.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TidebinderEffect extends MobEffect {
    public TidebinderEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x0066D9);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "7fbaa510-4e3f-3031-a235-2f49c1c6b962", 0.1, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
