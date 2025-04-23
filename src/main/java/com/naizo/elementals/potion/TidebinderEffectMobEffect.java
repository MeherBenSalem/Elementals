
package com.naizo.elementals.potion;

import net.minecraftforge.common.ForgeMod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

import java.util.List;
import java.util.ArrayList;

public class TidebinderEffectMobEffect extends MobEffect {
	public TidebinderEffectMobEffect() {
		super(MobEffectCategory.BENEFICIAL, -16750951);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7fbaa510-4e3f-3031-a235-2f49c1c6b962", 0.1, AttributeModifier.Operation.ADDITION);
		this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "277ba22f-c961-39ac-931f-9929a804d01d", 0.1, AttributeModifier.Operation.ADDITION);
	}

	@Override
	public List<ItemStack> getCurativeItems() {
		ArrayList<ItemStack> cures = new ArrayList<ItemStack>();
		return cures;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
