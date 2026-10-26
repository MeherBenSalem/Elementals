package com.naizo.elementals.effect;

import net.minecraft.resources.Identifier;
import com.naizo.elementals.Elementals;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TidebinderEffect extends MobEffect {
    public TidebinderEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x0066D9);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "tidebinder_speed"), 0.1, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
