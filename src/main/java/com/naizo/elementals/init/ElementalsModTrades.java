
/*
*	MCreator note: This file will be REGENERATED on each build.
*/
package com.naizo.elementals.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.common.BasicItemListing;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.npc.VillagerProfession;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElementalsModTrades {
	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event) {
		if (event.getType() == VillagerProfession.LIBRARIAN) {
			event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.BOOK), new ItemStack(ElementalsModItems.FIRE_RUNE.get()), new ItemStack(ElementalsModItems.EXPLOSIVE_ORB_SPELL_BOOK.get()), 1, 10, 0.05f));
			event.getTrades().get(4).add(new BasicItemListing(new ItemStack(Items.BOOK), new ItemStack(ElementalsModItems.FIRE_RUNE.get(), 2), new ItemStack(ElementalsModItems.EMBER_FIRE_SHIELD_SPELL_BOOK.get()), 1, 10, 0.05f));
			event.getTrades().get(5).add(new BasicItemListing(new ItemStack(Items.BOOK), new ItemStack(ElementalsModItems.FIRE_RUNE.get(), 3), new ItemStack(ElementalsModItems.INFERNO_WAVE_SPELL_BOOK.get()), 1, 10, 0.05f));
		}
	}
}
