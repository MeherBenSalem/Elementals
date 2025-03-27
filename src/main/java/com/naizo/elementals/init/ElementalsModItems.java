
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.naizo.elementals.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.ForgeSpawnEggItem;

import net.minecraft.world.item.Item;

import com.naizo.elementals.item.InfernoWaveSpellBookItem;
import com.naizo.elementals.item.FireRuneItem;
import com.naizo.elementals.item.FireElementFoodItem;
import com.naizo.elementals.item.ExplosiveOrbSpellBookItem;
import com.naizo.elementals.item.EmberFireShieldSpellBookItem;
import com.naizo.elementals.ElementalsMod;

public class ElementalsModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ElementalsMod.MODID);
	public static final RegistryObject<Item> FIRE_ELEMENT_FOOD = REGISTRY.register("fire_element_food", () -> new FireElementFoodItem());
	public static final RegistryObject<Item> FIRE_GOLEM_SPAWN_EGG = REGISTRY.register("fire_golem_spawn_egg", () -> new ForgeSpawnEggItem(ElementalsModEntities.FIRE_GOLEM, -6750208, -256, new Item.Properties()));
	public static final RegistryObject<Item> FIRE_RUNE = REGISTRY.register("fire_rune", () -> new FireRuneItem());
	public static final RegistryObject<Item> EXPLOSIVE_ORB_SPELL_BOOK = REGISTRY.register("explosive_orb_spell_book", () -> new ExplosiveOrbSpellBookItem());
	public static final RegistryObject<Item> EMBER_FIRE_SHIELD_SPELL_BOOK = REGISTRY.register("ember_fire_shield_spell_book", () -> new EmberFireShieldSpellBookItem());
	public static final RegistryObject<Item> INFERNO_WAVE_SPELL_BOOK = REGISTRY.register("inferno_wave_spell_book", () -> new InfernoWaveSpellBookItem());
	// Start of user code block custom items
	// End of user code block custom items
}
