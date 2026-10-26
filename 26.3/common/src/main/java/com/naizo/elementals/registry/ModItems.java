package com.naizo.elementals.registry;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class ModItems {
    public static Supplier<Item> FIRE_ELEMENT_FOOD;
    public static Supplier<Item> WATER_ELEMENT_FOOD;
    public static Supplier<Item> FIRE_RUNE;
    public static Supplier<Item> WATER_RUNE;
    public static Supplier<Item> EXPLOSIVE_ORB_SPELL_BOOK;
    public static Supplier<Item> EMBER_FIRE_SHIELD_SPELL_BOOK;
    public static Supplier<Item> INFERNO_WAVE_SPELL_BOOK;
    public static Supplier<Item> FIRE_GOLEM_SPAWN_EGG;
    public static Supplier<Item> REMNANT_TOME;
    public static Supplier<Item> TOME_OF_UNBINDING;

    private ModItems() {
    }

    /** Every item in creative tab order, with its registry name. */
    public static final String[] NAMES = {
            "remnant_tome", "tome_of_unbinding", "fire_element_food", "water_element_food", "fire_rune", "water_rune",
            "explosive_orb_spell_book", "ember_fire_shield_spell_book", "inferno_wave_spell_book", "fire_golem_spawn_egg"
    };
}
