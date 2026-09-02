package com.naizo.elementals.fabric.init;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.registry.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ElementalsFabricTabs {
    private ElementalsFabricTabs() {
    }

    public static void register() {
        CreativeModeTab tab = FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup.elementals"))
                .icon(() -> new ItemStack(ModItems.FIRE_ELEMENT_FOOD.get()))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "main"), tab);

        CreativeModeTabEvents.modifyOutputEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "main")))
                .register(content -> {
                    content.accept(ModItems.FIRE_ELEMENT_FOOD.get());
                    content.accept(ModItems.WATER_ELEMENT_FOOD.get());
                    content.accept(ModItems.FIRE_RUNE.get());
                    content.accept(ModItems.EXPLOSIVE_ORB_SPELL_BOOK.get());
                    content.accept(ModItems.EMBER_FIRE_SHIELD_SPELL_BOOK.get());
                    content.accept(ModItems.INFERNO_WAVE_SPELL_BOOK.get());
                    content.accept(ModItems.FIRE_GOLEM_SPAWN_EGG.get());
                });
    }
}
