package com.naizo.elementals.registry;

import com.naizo.elementals.core.School;
import com.naizo.elementals.item.ElementSeedItem;
import com.naizo.elementals.item.RemnantTomeItem;
import com.naizo.elementals.item.RuneItem;
import com.naizo.elementals.item.SpellBookItem;
import com.naizo.elementals.item.TomeOfUnbindingItem;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.world.item.Item;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Item factories shared by every loader. Each loader passes a register function that supplies
 * Item.Properties (with the item id already set on versions that need it).
 */
public final class ModContent {
    private ModContent() {
    }

    public static void items(BiFunction<String, Function<Item.Properties, Item>, Supplier<Item>> register, Function<Item.Properties, Item> spawnEgg) {
        ModItems.REMNANT_TOME = register.apply("remnant_tome", RemnantTomeItem::new);
        ModItems.TOME_OF_UNBINDING = register.apply("tome_of_unbinding", TomeOfUnbindingItem::new);
        ModItems.FIRE_ELEMENT_FOOD = register.apply("fire_element_food", p -> new ElementSeedItem(School.PYROMANCY, true, p));
        ModItems.WATER_ELEMENT_FOOD = register.apply("water_element_food", p -> new ElementSeedItem(School.TIDECALLING, false, p));
        ModItems.FIRE_RUNE = register.apply("fire_rune", p -> new RuneItem("blaze", true, p));
        ModItems.WATER_RUNE = register.apply("water_rune", p -> new RuneItem("drowned", false, p));
        ModItems.EXPLOSIVE_ORB_SPELL_BOOK = register.apply("explosive_orb_spell_book", p -> new SpellBookItem("explosive_orb", () -> PowerManager.CONFIG.explosiveOrbBookLevel, p));
        ModItems.EMBER_FIRE_SHIELD_SPELL_BOOK = register.apply("ember_fire_shield_spell_book", p -> new SpellBookItem("ember_shield", () -> PowerManager.CONFIG.emberShieldBookLevel, p));
        ModItems.INFERNO_WAVE_SPELL_BOOK = register.apply("inferno_wave_spell_book", p -> new SpellBookItem("inferno_wave", () -> PowerManager.CONFIG.infernoWaveBookLevel, p));
        ModItems.FIRE_GOLEM_SPAWN_EGG = register.apply("fire_golem_spawn_egg", spawnEgg);
    }
}
