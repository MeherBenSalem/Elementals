package com.naizo.elementals.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Rule-changing keystones. Trees refer to them by id; their behavior lives in code. */
public final class Keystone {
    private static final Map<String, Keystone> ALL = new LinkedHashMap<>();

    public static final Keystone PYRE_HEART = reg("pyre_heart", "Pyre Heart",
            "You take no damage from burning.", "Your ignites last twice as long.");
    public static final Keystone GLASS_CANNON = reg("glass_cannon", "Glass Cannon",
            "+40% Ability Damage.", "25% less Max Health.");
    public static final Keystone CONDUIT = reg("conduit", "Conduit",
            "Every 4th ability you cast costs no Essence", "and deals double damage.");
    public static final Keystone WINDBORNE = reg("windborne", "Windborne",
            "You take no fall damage.", "+15% Movement Speed.", "15% less Armor.");
    public static final Keystone BLOOD_PACT = reg("blood_pact", "Blood Pact",
            "Abilities cost Health instead of Essence:", "1 Health per 10 Essence.", "+25% Ability Damage.");
    public static final Keystone SOUL_EATER = reg("soul_eater", "Soul Eater",
            "Killing an enemy restores 15% of your Max Essence", "and 2 Health.");
    public static final Keystone ETERNAL_TIDE = reg("eternal_tide", "Eternal Tide",
            "Essence regenerates twice as fast.", "30% less Max Essence.");
    public static final Keystone GLACIAL_BULWARK = reg("glacial_bulwark", "Glacial Bulwark",
            "Take 20% less damage while above half Health.", "Enemies that hit you are chilled.");
    public static final Keystone UNYIELDING = reg("unyielding", "Unyielding",
            "You cannot be knocked back.", "+30% Armor.", "10% less Movement Speed.");
    public static final Keystone BEDROCK_WILL = reg("bedrock_will", "Bedrock Will",
            "Take 15% less damage.", "Ability cooldowns are 20% longer.");
    public static final Keystone UNDYING_GROVE = reg("undying_grove", "Undying Grove",
            "Once every 5 minutes, a killing blow instead", "leaves you at 30% Health and roots nearby enemies.");
    public static final Keystone VERDANT_BLOOM = reg("verdant_bloom", "Verdant Bloom",
            "Your healing is 50% stronger.", "Regenerate 0.5 Health per second.", "20% less Ability Damage.");

    public final String id;
    public final String name;
    public final List<String> lines;

    private Keystone(String id, String name, List<String> lines) {
        this.id = id;
        this.name = name;
        this.lines = lines;
    }

    private static Keystone reg(String id, String name, String... lines) {
        Keystone keystone = new Keystone(id, name, List.of(lines));
        ALL.put(id, keystone);
        return keystone;
    }

    public static Keystone get(String id) {
        return id == null ? null : ALL.get(id);
    }

    public static Map<String, Keystone> all() {
        return Collections.unmodifiableMap(ALL);
    }
}
