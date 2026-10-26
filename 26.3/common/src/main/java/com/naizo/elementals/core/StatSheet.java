package com.naizo.elementals.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Totals of every stat, keystone and ability granted by a set of allocated nodes. */
public final class StatSheet {
    public final Map<String, Double> totals = new LinkedHashMap<>();
    public final Set<String> keystones = new LinkedHashSet<>();
    public final Set<String> abilities = new LinkedHashSet<>();
    public final Map<School, Integer> schoolPoints = new LinkedHashMap<>();

    public static StatSheet of(SkillTree tree, Collection<String> allocated) {
        StatSheet sheet = new StatSheet();
        if (tree == null) {
            return sheet;
        }
        for (String id : allocated) {
            SkillNode node = tree.get(id);
            if (node == null) {
                continue;
            }
            node.stats.forEach((key, value) -> sheet.totals.merge(key, value, Double::sum));
            if (node.keystone != null && Keystone.get(node.keystone) != null) {
                sheet.keystones.add(node.keystone);
            }
            if (node.ability != null && AbilityCatalog.get(node.ability) != null) {
                sheet.abilities.add(node.ability);
            }
            sheet.schoolPoints.merge(node.school, 1, Integer::sum);
        }
        return sheet;
    }

    /** Raw summed value, capped at the stat's cap when it has one. */
    public double get(String key) {
        double value = totals.getOrDefault(key, 0.0);
        Stat stat = Stat.get(key);
        if (stat != null && stat.cap > 0) {
            value = Math.min(value, stat.cap);
        }
        return value;
    }

    public double get(Stat stat) {
        return get(stat.key);
    }

    public boolean has(Keystone keystone) {
        return keystones.contains(keystone.id);
    }

    public double maxEssence(double baseMax) {
        double max = baseMax + get(Stat.MAX_ESSENCE);
        if (has(Keystone.ETERNAL_TIDE)) {
            max *= 0.7;
        }
        return Math.max(10, max);
    }

    public double essenceRegenPerSecond(double baseRegen) {
        double regen = baseRegen * (1 + get(Stat.ESSENCE_REGEN) / 100.0);
        if (has(Keystone.ETERNAL_TIDE)) {
            regen *= 2;
        }
        return Math.max(0, regen);
    }

    public double cooldownMultiplier(double maxReduction) {
        double reduction = Math.min(get(Stat.COOLDOWN_REDUCTION), maxReduction);
        double multiplier = 1 - reduction / 100.0;
        if (has(Keystone.BEDROCK_WILL)) {
            multiplier *= 1.2;
        }
        return Math.max(0.1, multiplier);
    }

    /** Damage multiplier for an ability of the given school, before crits and temporary buffs. */
    public double damageMultiplier(School school, double rasBonusPercent) {
        double percent = get(Stat.ABILITY_POWER) + get(school.damageStat) + rasBonusPercent;
        double multiplier = 1 + percent / 100.0;
        if (has(Keystone.GLASS_CANNON)) {
            multiplier *= 1.4;
        }
        if (has(Keystone.BLOOD_PACT)) {
            multiplier *= 1.25;
        }
        if (has(Keystone.VERDANT_BLOOM)) {
            multiplier *= 0.8;
        }
        return Math.max(0, multiplier);
    }

    public double healMultiplier() {
        double multiplier = 1 + get(Stat.LIFE_POWER) / 100.0;
        if (has(Keystone.VERDANT_BLOOM)) {
            multiplier *= 1.5;
        }
        return multiplier;
    }

    public double areaMultiplier() {
        return 1 + get(Stat.AREA) / 100.0;
    }

    public double durationMultiplier() {
        return 1 + get(Stat.DURATION) / 100.0;
    }

    public double critChance() {
        return get(Stat.CRIT_CHANCE) / 100.0;
    }

    public double critMultiplier() {
        return 1.5 + get(Stat.CRIT_DAMAGE) / 100.0;
    }

    /** Fraction of incoming damage that remains after reductions (1 = full damage). */
    public double incomingDamageMultiplier(float healthFraction) {
        double multiplier = 1 - get(Stat.DAMAGE_REDUCTION) / 100.0;
        if (has(Keystone.BEDROCK_WILL)) {
            multiplier *= 0.85;
        }
        if (has(Keystone.GLACIAL_BULWARK) && healthFraction > 0.5f) {
            multiplier *= 0.8;
        }
        return Math.max(0, multiplier);
    }

    public double healthRegenPerSecond() {
        double regen = get(Stat.HEALTH_REGEN);
        if (has(Keystone.VERDANT_BLOOM)) {
            regen += 0.5;
        }
        return regen;
    }
}
