package com.naizo.elementals.core;

import java.util.Locale;

/** The six schools of power. Each owns a sector of the skill tree. */
public enum School {
    PYROMANCY("pyromancy", "Pyromancy", "Fire", 0xFF6A2A, "minecraft:blaze_powder", "fire_damage"),
    STORMCALLING("stormcalling", "Stormcalling", "Storm", 0x6FE7FF, "minecraft:lightning_rod", "storm_damage"),
    UMBRAL("umbral", "Umbral Arts", "Void", 0xB26BFF, "minecraft:ender_eye", "void_damage"),
    TIDECALLING("tidecalling", "Tidecalling", "Frost", 0x4F8DFF, "minecraft:heart_of_the_sea", "frost_damage"),
    EARTHSHAPING("earthshaping", "Earthshaping", "Earth", 0xC9A05A, "minecraft:mossy_cobblestone", "earth_damage"),
    VERDANCE("verdance", "Verdance", "Life", 0x6EE07A, "minecraft:glow_berries", "life_power"),
    NONE("none", "Neutral", "Neutral", 0xD8CFB8, "minecraft:amethyst_shard", "ability_power");

    public final String id;
    public final String displayName;
    public final String element;
    public final int color;
    public final String icon;
    public final String damageStat;

    School(String id, String displayName, String element, int color, String icon, String damageStat) {
        this.id = id;
        this.displayName = displayName;
        this.element = element;
        this.color = color;
        this.icon = icon;
        this.damageStat = damageStat;
    }

    /** Angle of the school's sector in degrees, clockwise from straight up. */
    public double angle() {
        return ordinal() * 60.0;
    }

    public static School byId(String id) {
        if (id == null) {
            return NONE;
        }
        String key = id.toLowerCase(Locale.ROOT);
        for (School school : values()) {
            if (school.id.equals(key) || school.element.toLowerCase(Locale.ROOT).equals(key)) {
                return school;
            }
        }
        return NONE;
    }

    public static School[] playable() {
        return new School[]{PYROMANCY, STORMCALLING, UMBRAL, TIDECALLING, EARTHSHAPING, VERDANCE};
    }
}
