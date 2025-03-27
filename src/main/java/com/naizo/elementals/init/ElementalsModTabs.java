
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.naizo.elementals.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

import com.naizo.elementals.ElementalsMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ElementalsModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ElementalsMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
			tabData.accept(ElementalsModItems.FIRE_ELEMENT_FOOD.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(ElementalsModItems.FIRE_GOLEM_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
			tabData.accept(ElementalsModItems.FIRE_RUNE.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.accept(ElementalsModItems.EXPLOSIVE_ORB_SPELL_BOOK.get());
			tabData.accept(ElementalsModItems.EMBER_FIRE_SHIELD_SPELL_BOOK.get());
			tabData.accept(ElementalsModItems.INFERNO_WAVE_SPELL_BOOK.get());
		}
	}
}
