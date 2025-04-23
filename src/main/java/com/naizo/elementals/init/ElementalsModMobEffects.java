
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package com.naizo.elementals.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import com.naizo.elementals.potion.TidebinderEffectMobEffect;
import com.naizo.elementals.ElementalsMod;

public class ElementalsModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ElementalsMod.MODID);
	public static final RegistryObject<MobEffect> TIDEBINDER_EFFECT = REGISTRY.register("tidebinder_effect", () -> new TidebinderEffectMobEffect());
}
