package com.naizo.elementals.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every stat a skill node can grant. Percent stats are stored as whole percents (12 = +12%).
 * Unknown keys in a custom tree are kept and shown, but have no effect.
 */
public final class Stat {
    public enum Kind { FLAT, PERCENT, DECIMAL }

    private static final Map<String, Stat> ALL = new LinkedHashMap<>();

    // Vanilla attributes (applied as attribute modifiers).
    public static final Stat MAX_HEALTH = reg("max_health", "%s Max Health", Kind.FLAT, 0, true);
    public static final Stat ARMOR = reg("armor", "%s Armor", Kind.FLAT, 0, true);
    public static final Stat ARMOR_TOUGHNESS = reg("armor_toughness", "%s Armor Toughness", Kind.FLAT, 0, true);
    public static final Stat ATTACK_DAMAGE = reg("attack_damage", "%s Melee Damage", Kind.PERCENT, 0, true);
    public static final Stat ATTACK_SPEED = reg("attack_speed", "%s Attack Speed", Kind.PERCENT, 0, true);
    public static final Stat MOVEMENT_SPEED = reg("movement_speed", "%s Movement Speed", Kind.PERCENT, 0, true);
    public static final Stat KNOCKBACK_RESISTANCE = reg("knockback_resistance", "%s Knockback Resistance", Kind.PERCENT, 100, true);
    public static final Stat LUCK = reg("luck", "%s Luck", Kind.FLAT, 0, true);

    // Ability power.
    public static final Stat ABILITY_POWER = reg("ability_power", "%s Ability Damage", Kind.PERCENT, 0, false);
    public static final Stat FIRE_DAMAGE = reg("fire_damage", "%s Fire Damage", Kind.PERCENT, 0, false);
    public static final Stat FROST_DAMAGE = reg("frost_damage", "%s Frost Damage", Kind.PERCENT, 0, false);
    public static final Stat STORM_DAMAGE = reg("storm_damage", "%s Storm Damage", Kind.PERCENT, 0, false);
    public static final Stat EARTH_DAMAGE = reg("earth_damage", "%s Earth Damage", Kind.PERCENT, 0, false);
    public static final Stat VOID_DAMAGE = reg("void_damage", "%s Void Damage", Kind.PERCENT, 0, false);
    public static final Stat LIFE_POWER = reg("life_power", "%s Life Power (healing and life damage)", Kind.PERCENT, 0, false);
    public static final Stat CRIT_CHANCE = reg("crit_chance", "%s Ability Critical Chance", Kind.PERCENT, 75, false);
    public static final Stat CRIT_DAMAGE = reg("crit_damage", "%s Ability Critical Damage", Kind.PERCENT, 0, false);
    public static final Stat COOLDOWN_REDUCTION = reg("cooldown_reduction", "%s Cooldown Reduction", Kind.PERCENT, 50, false);
    public static final Stat AREA = reg("area", "%s Area of Effect", Kind.PERCENT, 150, false);
    public static final Stat DURATION = reg("duration", "%s Effect Duration", Kind.PERCENT, 200, false);
    public static final Stat LIFESTEAL = reg("lifesteal", "%s of Ability Damage Leeched as Life", Kind.PERCENT, 50, false);

    // Essence.
    public static final Stat MAX_ESSENCE = reg("max_essence", "%s Max Essence", Kind.FLAT, 0, false);
    public static final Stat ESSENCE_REGEN = reg("essence_regen", "%s Essence Regeneration", Kind.PERCENT, 0, false);
    public static final Stat ESSENCE_ON_KILL = reg("essence_on_kill", "%s Essence on Kill", Kind.FLAT, 0, false);

    // Defence and utility.
    public static final Stat HEALTH_REGEN = reg("health_regen", "%s Health per Second", Kind.DECIMAL, 0, false);
    public static final Stat DAMAGE_REDUCTION = reg("damage_reduction", "%s Damage Taken Reduction", Kind.PERCENT, 60, false);
    public static final Stat DODGE = reg("dodge", "%s Chance to Evade Attacks", Kind.PERCENT, 40, false);
    public static final Stat THORNS = reg("thorns", "%s of Melee Damage Taken Reflected", Kind.PERCENT, 200, false);
    public static final Stat FALL_REDUCTION = reg("fall_reduction", "%s Fall Damage Reduction", Kind.PERCENT, 100, false);
    public static final Stat BURN_ON_HIT = reg("burn_on_hit", "%s Chance to Ignite on Melee Hit", Kind.PERCENT, 100, false);
    public static final Stat CHILL_ON_HIT = reg("chill_on_hit", "%s Chance to Chill on Melee Hit", Kind.PERCENT, 100, false);
    public static final Stat POWER_XP = reg("power_xp", "%s Power Experience Gained", Kind.PERCENT, 0, false);

    public final String key;
    public final String format;
    public final Kind kind;
    /** Upper bound on the summed value, 0 for none. */
    public final double cap;
    public final boolean vanillaAttribute;

    private Stat(String key, String format, Kind kind, double cap, boolean vanillaAttribute) {
        this.key = key;
        this.format = format;
        this.kind = kind;
        this.cap = cap;
        this.vanillaAttribute = vanillaAttribute;
    }

    private static Stat reg(String key, String format, Kind kind, double cap, boolean vanilla) {
        Stat stat = new Stat(key, format, kind, cap, vanilla);
        ALL.put(key, stat);
        return stat;
    }

    public static Stat get(String key) {
        return ALL.get(key);
    }

    public static Map<String, Stat> all() {
        return Collections.unmodifiableMap(ALL);
    }

    public String describe(double value) {
        return String.format(format, signed(value, kind));
    }

    public static String describe(String key, double value) {
        Stat stat = ALL.get(key);
        if (stat == null) {
            return signed(value, Kind.FLAT) + " " + key;
        }
        return stat.describe(value);
    }

    public static String signed(double value, Kind kind) {
        String sign = value >= 0 ? "+" : "-";
        double abs = Math.abs(value);
        String number = kind == Kind.DECIMAL ? trim(abs, 2) : trim(abs, 1);
        return sign + number + (kind == Kind.PERCENT ? "%" : "");
    }

    public static String trim(double value, int decimals) {
        if (Math.abs(value - Math.rint(value)) < 1e-6) {
            return String.valueOf((long) Math.rint(value));
        }
        String text = String.format(java.util.Locale.ROOT, "%." + decimals + "f", value);
        while (text.contains(".") && (text.endsWith("0") || text.endsWith("."))) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
