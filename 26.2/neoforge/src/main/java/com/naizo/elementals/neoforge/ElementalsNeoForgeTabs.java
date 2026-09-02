package com.naizo.elementals.neoforge;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ElementalsNeoForgeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, Elementals.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTRY.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.elementals"))
                    .icon(() -> new ItemStack(ModItems.FIRE_ELEMENT_FOOD.get()))
                    .displayItems((parameters, tabData) -> {
                        tabData.accept(ModItems.FIRE_ELEMENT_FOOD.get());
                        tabData.accept(ModItems.WATER_ELEMENT_FOOD.get());
                        tabData.accept(ModItems.FIRE_RUNE.get());
                        tabData.accept(ModItems.EXPLOSIVE_ORB_SPELL_BOOK.get());
                        tabData.accept(ModItems.EMBER_FIRE_SHIELD_SPELL_BOOK.get());
                        tabData.accept(ModItems.INFERNO_WAVE_SPELL_BOOK.get());
                        tabData.accept(ModItems.FIRE_GOLEM_SPAWN_EGG.get());
                    })
                    .build());

    private ElementalsNeoForgeTabs() {
    }
}
