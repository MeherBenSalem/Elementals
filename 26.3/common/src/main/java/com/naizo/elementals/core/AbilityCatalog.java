package com.naizo.elementals.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The built-in abilities. Every parameter here is a default: config/remnants/skills_and_powers/abilities.json
 * can override any of them. "cooldown" and "duration" are seconds, "cost" is Essence.
 */
public final class AbilityCatalog {
    private static final Map<String, AbilityDef> ALL = new LinkedHashMap<>();

    static {
        // Pyromancy
        def("ember_bolt", School.PYROMANCY, "Ember Bolt", "minecraft:fire_charge", false,
                "Hurl a searing bolt that deals {damage} Fire damage and ignites the target for {burn}s.")
                .p("cost", 10).p("cooldown", 1.5).p("damage", 6).p("burn", 4).p("speed", 2.2);
        def("explosive_orb", School.PYROMANCY, "Explosive Orb", "minecraft:magma_cream", false,
                "Launch a volatile orb that bursts for {damage} Fire damage in a {radius}-block radius. It never breaks blocks.")
                .p("cost", 25).p("cooldown", 6).p("damage", 10).p("radius", 3.5).p("speed", 1.4);
        def("ember_shield", School.PYROMANCY, "Ember Shield", "minecraft:blaze_rod", false,
                "Wreathe yourself in embers for {duration}s: gain {absorption} absorption Health, and attackers burn for {burn}s.")
                .p("cost", 30).p("cooldown", 18).p("duration", 10).p("absorption", 8).p("burn", 3);
        def("inferno_wave", School.PYROMANCY, "Inferno Wave", "minecraft:flint_and_steel", false,
                "A ring of flame rolls out {radius} blocks, dealing {damage} Fire damage and igniting enemies for {burn}s.")
                .p("cost", 40).p("cooldown", 12).p("damage", 8).p("radius", 7).p("burn", 4);
        def("meteor", School.PYROMANCY, "Meteor", "minecraft:magma_block", true,
                "Call a meteor onto the spot you aim at within {range} blocks. After {delay}s it strikes for {damage} Fire damage in a {radius}-block radius.")
                .p("cost", 60).p("cooldown", 30).p("damage", 22).p("radius", 5).p("range", 32).p("delay", 1.2).p("burn", 5);
        // Stormcalling
        def("chain_lightning", School.STORMCALLING, "Chain Lightning", "minecraft:lightning_rod", false,
                "Lightning strikes the enemy you aim at within {range} blocks for {damage} Storm damage, then arcs to {chains} more enemies.")
                .p("cost", 12).p("cooldown", 2).p("damage", 5).p("range", 20).p("chains", 3).p("chain_range", 6);
        def("static_field", School.STORMCALLING, "Static Field", "minecraft:copper_ingot", false,
                "For {duration}s a crackling field shocks the nearest enemy within {radius} blocks for {damage} Storm damage every half second.")
                .p("cost", 30).p("cooldown", 16).p("damage", 2).p("radius", 6).p("duration", 6);
        def("thunderclap", School.STORMCALLING, "Thunderclap", "minecraft:firework_rocket", false,
                "Thunder breaks around you: enemies within {radius} blocks take {damage} Storm damage and are hurled away.")
                .p("cost", 25).p("cooldown", 10).p("damage", 6).p("radius", 5).p("knockback", 1.6);
        def("gale_step", School.STORMCALLING, "Gale Step", "minecraft:feather", false,
                "Ride the wind up to {range} blocks forward. Your next landing takes no fall damage.")
                .p("cost", 15).p("cooldown", 5).p("range", 10);
        def("tempest", School.STORMCALLING, "Tempest", "minecraft:trident", true,
                "Call a tempest on the spot you aim at within {range} blocks: {strikes} lightning strikes hit enemies around it over {duration}s for {damage} Storm damage each.")
                .p("cost", 65).p("cooldown", 35).p("damage", 10).p("strikes", 6).p("radius", 8).p("range", 30).p("duration", 3);
        // Umbral Arts
        def("shadow_bolt", School.UMBRAL, "Shadow Bolt", "minecraft:wither_rose", false,
                "A bolt of shadow hits the enemy you aim at within {range} blocks for {damage} Void damage and withers it for {wither}s.")
                .p("cost", 12).p("cooldown", 2).p("damage", 6).p("range", 24).p("wither", 3);
        def("soul_harvest", School.UMBRAL, "Soul Harvest", "minecraft:soul_lantern", false,
                "Tear at the souls of enemies within {radius} blocks for {damage} Void damage and heal {leech}% of the damage dealt.")
                .p("cost", 30).p("cooldown", 14).p("damage", 5).p("radius", 6).p("leech", 50);
        def("void_rift", School.UMBRAL, "Void Rift", "minecraft:crying_obsidian", false,
                "Open a rift on the spot you aim at within {range} blocks. For {duration}s it pulls enemies in, blinds them and deals {damage} Void damage per second.")
                .p("cost", 40).p("cooldown", 18).p("damage", 3).p("radius", 4).p("range", 24).p("duration", 5);
        def("shadow_step", School.UMBRAL, "Shadow Step", "minecraft:echo_shard", false,
                "Step through the shadows to behind the enemy you aim at within {range} blocks and turn invisible for {duration}s.")
                .p("cost", 18).p("cooldown", 8).p("range", 16).p("duration", 2);
        def("eclipse", School.UMBRAL, "Eclipse", "minecraft:dragon_breath", true,
                "Become one with the dark for {duration}s: turn invisible, move faster and deal {bonus}% more ability damage. Enemies within {radius} blocks are engulfed in Darkness.")
                .p("cost", 60).p("cooldown", 40).p("duration", 8).p("bonus", 40).p("radius", 10);
        // Tidecalling
        def("frost_lance", School.TIDECALLING, "Frost Lance", "minecraft:packed_ice", false,
                "A lance of ice pierces up to {pierce} enemies in a {range}-block line for {damage} Frost damage and slows them for {slow}s.")
                .p("cost", 10).p("cooldown", 1.75).p("damage", 5).p("range", 18).p("pierce", 4).p("slow", 3);
        def("tidebinder", School.TIDECALLING, "Tidebinder", "minecraft:nautilus_shell", false,
                "Bind the tide to yourself for {duration}s: faster movement, Regeneration and Water Breathing. Puts out fire.")
                .p("cost", 20).p("cooldown", 20).p("duration", 10);
        def("glacial_prison", School.TIDECALLING, "Glacial Prison", "minecraft:blue_ice", false,
                "Freeze every enemy within {radius} blocks in place for {duration}s and deal {damage} Frost damage.")
                .p("cost", 40).p("cooldown", 20).p("damage", 6).p("radius", 6).p("duration", 3);
        def("tidal_surge", School.TIDECALLING, "Tidal Surge", "minecraft:prismarine_shard", false,
                "Surge forward on a wave up to {range} blocks. Enemies in your path take {damage} Frost damage and are swept aside.")
                .p("cost", 20).p("cooldown", 8).p("damage", 6).p("range", 10);
        def("maelstrom", School.TIDECALLING, "Maelstrom", "minecraft:conduit", true,
                "Raise a maelstrom on the spot you aim at within {range} blocks. For {duration}s it drags enemies within {radius} blocks to its eye and deals {damage} Frost damage per second.")
                .p("cost", 60).p("cooldown", 35).p("damage", 4).p("radius", 7).p("range", 26).p("duration", 6);
        // Earthshaping
        def("stone_spear", School.EARTHSHAPING, "Stone Spear", "minecraft:pointed_dripstone", false,
                "A stone spear erupts under the enemy you aim at within {range} blocks, dealing {damage} Earth damage and launching it into the air.")
                .p("cost", 12).p("cooldown", 2.5).p("damage", 7).p("range", 20);
        def("stone_skin", School.EARTHSHAPING, "Stone Skin", "minecraft:iron_block", false,
                "Harden your skin for {duration}s: Resistance and full knockback resistance.")
                .p("cost", 25).p("cooldown", 22).p("duration", 8).p("amplifier", 1);
        def("tremor", School.EARTHSHAPING, "Tremor", "minecraft:anvil", false,
                "Slam the ground: enemies within {radius} blocks take {damage} Earth damage and are thrown upward.")
                .p("cost", 30).p("cooldown", 12).p("damage", 8).p("radius", 5);
        def("fissure", School.EARTHSHAPING, "Fissure", "minecraft:dripstone_block", false,
                "Split the earth in a {range}-block line. Each eruption deals {damage} Earth damage.")
                .p("cost", 30).p("cooldown", 12).p("damage", 7).p("range", 14);
        def("mountains_wrath", School.EARTHSHAPING, "Mountain's Wrath", "minecraft:lodestone", true,
                "Become the mountain for {duration}s: Resistance, Strength and no knockback. The ground shatters around you for {damage} Earth damage in a {radius}-block radius.")
                .p("cost", 60).p("cooldown", 45).p("duration", 10).p("damage", 10).p("radius", 6);
        // Verdance
        def("mending_light", School.VERDANCE, "Mending Light", "minecraft:golden_carrot", false,
                "Heal yourself and allies within {radius} blocks for {heal} Health.")
                .p("cost", 18).p("cooldown", 6).p("heal", 6).p("radius", 6);
        def("thorn_burst", School.VERDANCE, "Thorn Burst", "minecraft:poisonous_potato", false,
                "Burst with thorns: enemies within {radius} blocks take {damage} Life damage and are poisoned for {poison}s.")
                .p("cost", 25).p("cooldown", 10).p("damage", 5).p("radius", 5).p("poison", 4);
        def("sanctuary", School.VERDANCE, "Sanctuary", "minecraft:lantern", false,
                "Hallow the ground for {duration}s: allies within {radius} blocks regenerate and undead there take {damage} Life damage every second.")
                .p("cost", 40).p("cooldown", 26).p("duration", 8).p("radius", 6).p("damage", 3);
        def("radiant_nova", School.VERDANCE, "Radiant Nova", "minecraft:sunflower", false,
                "Release a burst of radiance: enemies within {radius} blocks take {damage} Life damage, doubled against undead, and glow for {duration}s.")
                .p("cost", 30).p("cooldown", 12).p("damage", 7).p("radius", 6).p("duration", 6);
        def("verdant_rebirth", School.VERDANCE, "Verdant Rebirth", "minecraft:totem_of_undying", true,
                "Bloom anew: heal to full, gain {absorption} absorption Health, cleanse harmful effects, and survive one killing blow within {duration}s.")
                .p("cost", 70).p("cooldown", 90).p("absorption", 8).p("duration", 10);
    }

    private AbilityCatalog() {
    }

    private static AbilityDef def(String id, School school, String name, String icon, boolean ultimate, String description) {
        AbilityDef def = new AbilityDef(id, school, name, icon, ultimate, description);
        ALL.put(id, def);
        return def;
    }

    public static AbilityDef get(String id) {
        return id == null ? null : ALL.get(id);
    }

    public static Map<String, AbilityDef> all() {
        return Collections.unmodifiableMap(ALL);
    }

    public static List<AbilityDef> of(School school) {
        List<AbilityDef> list = new ArrayList<>();
        for (AbilityDef def : ALL.values()) {
            if (def.school == school) {
                list.add(def);
            }
        }
        return list;
    }
}
