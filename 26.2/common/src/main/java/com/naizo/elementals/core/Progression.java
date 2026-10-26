package com.naizo.elementals.core;

/** Level, experience and skill point arithmetic. */
public final class Progression {
    private Progression() {
    }

    public static double xpToNext(PowersConfig config, int level) {
        return Math.max(1, config.xpBase + config.xpPerLevel * level);
    }

    /** Skill points earned in total, before spending. */
    public static int earnedPoints(PowersConfig config, PowerProfile profile, boolean rasActive) {
        int points = config.startingPoints + profile.bonusPoints;
        String mode = config.effectiveRasMode(rasActive);
        if (!"replace".equals(mode)) {
            points += profile.level * config.pointsPerLevel;
        }
        if (!"off".equals(mode)) {
            points += profile.rasLevelCredited * config.pointsPerRasLevel;
        }
        return Math.max(0, points);
    }

    public static int spentPoints(SkillTree tree, PowerProfile profile) {
        return tree == null ? 0 : tree.cost(profile.allocated);
    }

    public static int availablePoints(PowersConfig config, SkillTree tree, PowerProfile profile, boolean rasActive) {
        return earnedPoints(config, profile, rasActive) - spentPoints(tree, profile);
    }

    /** Adds experience and returns the number of levels gained. */
    public static int addXp(PowersConfig config, PowerProfile profile, double amount) {
        if (amount <= 0 || profile.level >= config.maxLevel) {
            return 0;
        }
        profile.xp += amount;
        int gained = 0;
        while (profile.level < config.maxLevel && profile.xp >= xpToNext(config, profile.level)) {
            profile.xp -= xpToNext(config, profile.level);
            profile.level++;
            gained++;
        }
        if (profile.level >= config.maxLevel) {
            profile.xp = 0;
        }
        return gained;
    }
}
