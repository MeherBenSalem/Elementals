package com.naizo.elementals.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the default Remnant constellation: six school sectors around an inner ring, each with three
 * branches of notable wheels, five abilities, two keystones and bridges into its neighbours.
 * The output is deterministic so build codes stay valid between launches.
 */
public final class DefaultTree {
    private static final double WHEEL_RADIUS = 40;

    private final SkillTree tree = new SkillTree();

    private DefaultTree() {
    }

    public static SkillTree build() {
        DefaultTree builder = new DefaultTree();
        builder.buildAll();
        return builder.tree;
    }

    // ---------------------------------------------------------------- content

    private record Wheel(String name, String icon, String ringStat, double ringValue, String ringIcon, Map<String, Double> notable, String flavor) {
    }

    private record Content(School school, String key, String startName, Map<String, Double> startStats,
                           String[] abilities, String[] keystones, String[] keystoneIcons,
                           Wheel[] wheels, String masteryName, String masteryIcon, Map<String, Double> mastery,
                           String[][] pathPool) {
    }

    private static Map<String, Double> m(Object... kv) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], ((Number) kv[i + 1]).doubleValue());
        }
        return map;
    }

    private static Wheel w(String name, String icon, String ringStat, double ringValue, String ringIcon, Map<String, Double> notable, String flavor) {
        return new Wheel(name, icon, ringStat, ringValue, ringIcon, notable, flavor);
    }

    private static List<Content> content() {
        List<Content> list = new ArrayList<>();
        list.add(new Content(School.PYROMANCY, "pyro", "Pyromancy Origin", m("fire_damage", 10, "max_essence", 10),
                new String[]{"ember_bolt", "explosive_orb", "ember_shield", "inferno_wave", "meteor"},
                new String[]{"pyre_heart", "glass_cannon"}, new String[]{"minecraft:nether_star", "minecraft:end_crystal"},
                new Wheel[]{
                        w("Searing Focus", "minecraft:blaze_powder", "fire_damage", 4, "minecraft:blaze_powder", m("fire_damage", 15, "crit_chance", 5), "Hold the flame until it is white."),
                        w("Kindled Soul", "minecraft:glowstone_dust", "max_essence", 6, "minecraft:lapis_lazuli", m("max_essence", 25, "essence_regen", 10), "A soul is only fuel that remembers."),
                        w("Blaze Rhythm", "minecraft:clock", "cooldown_reduction", 3, "minecraft:clock", m("cooldown_reduction", 10, "ability_power", 8), "Breathe in. Burn out. Again."),
                        w("Scorched Earth", "minecraft:fire_charge", "area", 5, "minecraft:firework_star", m("area", 20, "fire_damage", 10), "Leave nothing for the frost."),
                        w("Ashen Veil", "minecraft:chainmail_chestplate", "armor", 1, "minecraft:iron_chestplate", m("armor", 4, "burn_on_hit", 15, "max_health", 6), "Ash settles on those who stand too close."),
                        w("Cinderheart", "minecraft:blaze_rod", "attack_damage", 4, "minecraft:iron_sword", m("attack_damage", 12, "burn_on_hit", 20, "attack_speed", 5), "Every blow a spark."),
                },
                "Pyromancer's Mastery", "minecraft:fire_charge", m("fire_damage", 20, "crit_damage", 15, "ability_power", 5),
                new String[][]{{"fire_damage", "4"}, {"ability_power", "3"}, {"max_essence", "5"}, {"burn_on_hit", "4"}, {"crit_chance", "2"}, {"max_health", "2"}}));
        list.add(new Content(School.STORMCALLING, "storm", "Stormcalling Origin", m("storm_damage", 10, "movement_speed", 3),
                new String[]{"chain_lightning", "static_field", "thunderclap", "gale_step", "tempest"},
                new String[]{"conduit", "windborne"}, new String[]{"minecraft:beacon", "minecraft:elytra"},
                new Wheel[]{
                        w("Static Charge", "minecraft:spectral_arrow", "crit_chance", 2, "minecraft:spectral_arrow", m("crit_chance", 6, "crit_damage", 15), "The air tastes of copper before it breaks."),
                        w("Overcurrent", "minecraft:redstone", "storm_damage", 4, "minecraft:glowstone_dust", m("storm_damage", 15, "ability_power", 10), "More than any wire can hold."),
                        w("Quickened Mind", "minecraft:clock", "cooldown_reduction", 3, "minecraft:clock", m("cooldown_reduction", 10, "essence_regen", 10), "Think at the speed of the bolt."),
                        w("Eye of the Storm", "minecraft:heart_of_the_sea", "area", 4, "minecraft:firework_star", m("area", 15, "movement_speed", 5), "Stillness, and all around it ruin."),
                        w("Fleet Footed", "minecraft:sugar", "movement_speed", 2, "minecraft:sugar", m("movement_speed", 6, "attack_speed", 8, "dodge", 5), "Be gone before the thunder."),
                        w("Gale Dancer", "minecraft:phantom_membrane", "dodge", 2, "minecraft:rabbit_foot", m("dodge", 8, "fall_reduction", 50), "Let the wind take the blow."),
                },
                "Stormcaller's Mastery", "minecraft:lightning_rod", m("storm_damage", 20, "cooldown_reduction", 5, "movement_speed", 4),
                new String[][]{{"storm_damage", "4"}, {"movement_speed", "2"}, {"crit_chance", "2"}, {"essence_regen", "4"}, {"attack_speed", "3"}, {"max_essence", "5"}}));
        list.add(new Content(School.UMBRAL, "umbral", "Umbral Origin", m("void_damage", 10, "lifesteal", 1),
                new String[]{"shadow_bolt", "soul_harvest", "void_rift", "shadow_step", "eclipse"},
                new String[]{"blood_pact", "soul_eater"}, new String[]{"minecraft:wither_skeleton_skull", "minecraft:sculk_catalyst"},
                new Wheel[]{
                        w("Hungering Dark", "minecraft:fermented_spider_eye", "lifesteal", 1, "minecraft:fermented_spider_eye", m("lifesteal", 4, "void_damage", 8), "It is always hungry. So are you."),
                        w("Blood Rite", "minecraft:redstone_block", "max_health", 4, "minecraft:golden_apple", m("max_health", 12, "health_regen", 0.3), "Paid in full, paid in blood."),
                        w("Shadowed Mind", "minecraft:ink_sac", "void_damage", 4, "minecraft:ender_pearl", m("void_damage", 15, "cooldown_reduction", 5), "Think where no light reaches."),
                        w("Abyssal Gaze", "minecraft:ender_eye", "crit_damage", 5, "minecraft:diamond", m("crit_damage", 20, "crit_chance", 4), "The abyss looks back, and finds the weak spot."),
                        w("Reaper's Toll", "minecraft:wither_rose", "essence_on_kill", 2, "minecraft:soul_lantern", m("essence_on_kill", 8, "power_xp", 15), "Every ending pays a toll."),
                        w("Veilwalker", "minecraft:black_dye", "dodge", 2, "minecraft:rabbit_foot", m("dodge", 6, "movement_speed", 6), "You were never there."),
                },
                "Umbral Mastery", "minecraft:ender_eye", m("void_damage", 20, "lifesteal", 3, "crit_chance", 3),
                new String[][]{{"void_damage", "4"}, {"lifesteal", "1"}, {"crit_damage", "4"}, {"max_health", "2"}, {"ability_power", "3"}, {"dodge", "1"}}));
        list.add(new Content(School.TIDECALLING, "tide", "Tidecalling Origin", m("frost_damage", 10, "essence_regen", 10),
                new String[]{"frost_lance", "tidebinder", "glacial_prison", "tidal_surge", "maelstrom"},
                new String[]{"eternal_tide", "glacial_bulwark"}, new String[]{"minecraft:conduit", "minecraft:blue_ice"},
                new Wheel[]{
                        w("Cold Clarity", "minecraft:lapis_block", "max_essence", 5, "minecraft:lapis_lazuli", m("max_essence", 20, "essence_regen", 15), "Clear as meltwater."),
                        w("Rime Armor", "minecraft:diamond_chestplate", "armor", 1, "minecraft:iron_chestplate", m("armor", 3, "armor_toughness", 2, "chill_on_hit", 15), "Frost is patient armor."),
                        w("Deep Current", "minecraft:nautilus_shell", "duration", 5, "minecraft:prismarine_crystals", m("duration", 20, "frost_damage", 8), "What the tide takes, it keeps."),
                        w("Shatterpoint", "minecraft:packed_ice", "frost_damage", 4, "minecraft:snowball", m("frost_damage", 15, "crit_damage", 10), "Everything frozen breaks the same way."),
                        w("Tidal Grace", "minecraft:kelp", "movement_speed", 2, "minecraft:sugar", m("movement_speed", 5, "health_regen", 0.25), "Move like water moves."),
                        w("Undertow", "minecraft:trident", "chill_on_hit", 3, "minecraft:snowball", m("chill_on_hit", 15, "attack_damage", 8), "Pulled under, held down."),
                },
                "Tidecaller's Mastery", "minecraft:heart_of_the_sea", m("frost_damage", 20, "duration", 10, "max_essence", 10),
                new String[][]{{"frost_damage", "4"}, {"max_essence", "5"}, {"essence_regen", "4"}, {"duration", "4"}, {"chill_on_hit", "3"}, {"armor", "1"}}));
        list.add(new Content(School.EARTHSHAPING, "earth", "Earthshaping Origin", m("earth_damage", 10, "armor", 2),
                new String[]{"stone_spear", "stone_skin", "tremor", "fissure", "mountains_wrath"},
                new String[]{"unyielding", "bedrock_will"}, new String[]{"minecraft:netherite_block", "minecraft:bedrock"},
                new Wheel[]{
                        w("Stoneblood", "minecraft:golden_apple", "max_health", 4, "minecraft:golden_apple", m("max_health", 12, "damage_reduction", 5), "Slow to bleed, slower to fall."),
                        w("Bastion", "minecraft:shield", "armor", 1, "minecraft:iron_chestplate", m("armor", 4, "armor_toughness", 2, "knockback_resistance", 15), "Be the wall."),
                        w("Tectonic Force", "minecraft:tnt", "earth_damage", 4, "minecraft:flint", m("earth_damage", 15, "area", 10), "The ground has been waiting."),
                        w("Rooted", "minecraft:oak_log", "knockback_resistance", 4, "minecraft:moss_block", m("knockback_resistance", 20, "damage_reduction", 6), "Roots go deeper than storms."),
                        w("Ironhide", "minecraft:iron_ingot", "armor_toughness", 1, "minecraft:netherite_scrap", m("armor_toughness", 3, "thorns", 10), "Struck iron rings back."),
                        w("Quarrier", "minecraft:iron_pickaxe", "attack_damage", 4, "minecraft:iron_sword", m("attack_damage", 12, "luck", 1), "Break it. Then break what's under it."),
                },
                "Earthshaper's Mastery", "minecraft:mossy_cobblestone", m("earth_damage", 20, "armor", 3, "max_health", 6),
                new String[][]{{"earth_damage", "4"}, {"armor", "1"}, {"max_health", "2"}, {"knockback_resistance", "3"}, {"attack_damage", "3"}, {"damage_reduction", "1"}}));
        list.add(new Content(School.VERDANCE, "verd", "Verdance Origin", m("life_power", 10, "health_regen", 0.1),
                new String[]{"mending_light", "thorn_burst", "sanctuary", "radiant_nova", "verdant_rebirth"},
                new String[]{"undying_grove", "verdant_bloom"}, new String[]{"minecraft:enchanted_golden_apple", "minecraft:flowering_azalea"},
                new Wheel[]{
                        w("Wellspring", "minecraft:glistering_melon_slice", "health_regen", 0.1, "minecraft:glistering_melon_slice", m("health_regen", 0.4, "max_health", 8), "Drink deep. There is always more."),
                        w("Bloom", "minecraft:pink_tulip", "life_power", 4, "minecraft:sweet_berries", m("life_power", 15, "duration", 10), "Everything wants to grow."),
                        w("Sunlit Mind", "minecraft:sunflower", "essence_regen", 4, "minecraft:experience_bottle", m("essence_regen", 15, "max_essence", 10), "Warm as a clearing at noon."),
                        w("Thornwall", "minecraft:cactus", "thorns", 3, "minecraft:cactus", m("thorns", 15, "armor", 2), "Touch the rose, learn the thorn."),
                        w("Evergreen", "minecraft:spruce_sapling", "max_health", 3, "minecraft:golden_apple", m("max_health", 10, "damage_reduction", 5), "Winter passes. The pine remains."),
                        w("Harvester", "minecraft:wheat", "power_xp", 4, "minecraft:experience_bottle", m("power_xp", 15, "luck", 1), "Reap what was sown."),
                },
                "Verdant Mastery", "minecraft:glow_berries", m("life_power", 20, "health_regen", 0.3, "max_health", 6),
                new String[][]{{"life_power", "4"}, {"health_regen", "0.1"}, {"max_health", "2"}, {"essence_regen", "4"}, {"thorns", "3"}, {"power_xp", "3"}}));
        return list;
    }

    private static final String[][] HYBRIDS = {
            // between school i and school i+1: inner notable, outer notable
            {"Plasma Arc", "fire_damage", "storm_damage", "Ionized Flame", "crit_chance", "fire_damage"},
            {"Eclipsed Static", "storm_damage", "void_damage", "Voidspark", "crit_damage", "storm_damage"},
            {"Abyssal Cold", "void_damage", "frost_damage", "Black Ice", "lifesteal", "frost_damage"},
            {"Permafrost", "frost_damage", "earth_damage", "Glacier's Weight", "armor", "frost_damage"},
            {"Overgrowth", "earth_damage", "life_power", "Ancient Roots", "max_health", "earth_damage"},
            {"Phoenix Ash", "life_power", "fire_damage", "Rekindling", "health_regen", "fire_damage"},
    };

    private static final Map<String, String> STAT_ICONS = new LinkedHashMap<>();
    private static final Map<String, String> STAT_NAMES = new LinkedHashMap<>();

    static {
        icon("fire_damage", "minecraft:blaze_powder", "Fire Damage");
        icon("frost_damage", "minecraft:snowball", "Frost Damage");
        icon("storm_damage", "minecraft:glowstone_dust", "Storm Damage");
        icon("earth_damage", "minecraft:flint", "Earth Damage");
        icon("void_damage", "minecraft:ender_pearl", "Void Damage");
        icon("life_power", "minecraft:sweet_berries", "Life Power");
        icon("ability_power", "minecraft:amethyst_shard", "Ability Damage");
        icon("max_essence", "minecraft:lapis_lazuli", "Essence");
        icon("essence_regen", "minecraft:experience_bottle", "Essence Flow");
        icon("cooldown_reduction", "minecraft:clock", "Haste of Mind");
        icon("area", "minecraft:firework_star", "Reach");
        icon("duration", "minecraft:prismarine_crystals", "Persistence");
        icon("crit_chance", "minecraft:spectral_arrow", "Precision");
        icon("crit_damage", "minecraft:diamond", "Ruthlessness");
        icon("lifesteal", "minecraft:fermented_spider_eye", "Leech");
        icon("max_health", "minecraft:golden_apple", "Vitality");
        icon("armor", "minecraft:iron_chestplate", "Armor");
        icon("armor_toughness", "minecraft:netherite_scrap", "Toughness");
        icon("attack_damage", "minecraft:iron_sword", "Melee Damage");
        icon("attack_speed", "minecraft:golden_sword", "Attack Speed");
        icon("movement_speed", "minecraft:sugar", "Swiftness");
        icon("knockback_resistance", "minecraft:moss_block", "Steadfast");
        icon("luck", "minecraft:rabbit_foot", "Fortune");
        icon("dodge", "minecraft:phantom_membrane", "Evasion");
        icon("damage_reduction", "minecraft:shield", "Fortitude");
        icon("health_regen", "minecraft:glistering_melon_slice", "Regeneration");
        icon("thorns", "minecraft:cactus", "Thorns");
        icon("fall_reduction", "minecraft:feather", "Featherfall");
        icon("burn_on_hit", "minecraft:flint_and_steel", "Kindling");
        icon("chill_on_hit", "minecraft:powder_snow_bucket", "Chill");
        icon("power_xp", "minecraft:experience_bottle", "Insight");
        icon("essence_on_kill", "minecraft:soul_lantern", "Soul Toll");
    }

    private static void icon(String stat, String item, String name) {
        STAT_ICONS.put(stat, item);
        STAT_NAMES.put(stat, name);
    }

    // ---------------------------------------------------------------- geometry

    private static double px(double angleDeg, double r) {
        return Math.round(r * Math.sin(Math.toRadians(angleDeg)) * 10) / 10.0;
    }

    private static double py(double angleDeg, double r) {
        return Math.round(-r * Math.cos(Math.toRadians(angleDeg)) * 10) / 10.0;
    }

    private SkillNode node(String id, NodeType type, School school, double angle, double r, String name, String icon) {
        return tree.add(new SkillNode(id, type, school, px(angle, r), py(angle, r), name, icon));
    }

    private SkillNode minor(String id, School school, double angle, double r, String stat, double value) {
        SkillNode node = node(id, NodeType.MINOR, school, angle, r, STAT_NAMES.getOrDefault(stat, stat), STAT_ICONS.getOrDefault(stat, school.icon));
        node.stat(stat, value);
        return node;
    }

    // ---------------------------------------------------------------- build

    private void buildAll() {
        List<Content> contents = content();
        // Inner ring: start nodes with two connectors to the next school.
        for (Content c : contents) {
            double t = c.school.angle();
            SkillNode start = node(c.key + ".start", NodeType.START, c.school, t, 160, c.startName, c.school.icon);
            c.startStats.forEach(start::stat);
            start.flavor = "Begin your path as a student of " + c.school.displayName + ".";
            minor(c.key + ".ring1", School.NONE, t + 20, 160, "max_health", 3).name = "Remnant Path";
            minor(c.key + ".ring2", School.NONE, t + 40, 160, "max_essence", 6).name = "Remnant Path";
        }
        for (int i = 0; i < contents.size(); i++) {
            Content c = contents.get(i);
            Content next = contents.get((i + 1) % contents.size());
            tree.link(c.key + ".start", c.key + ".ring1");
            tree.link(c.key + ".ring1", c.key + ".ring2");
            tree.link(c.key + ".ring2", next.key + ".start");
        }
        for (Content c : contents) {
            buildSchool(c);
        }
        for (int i = 0; i < contents.size(); i++) {
            buildBridges(contents.get(i), contents.get((i + 1) % contents.size()), HYBRIDS[i]);
        }
    }

    private SkillNode ability(Content c, int index, double angle, double r) {
        AbilityDef def = AbilityCatalog.get(c.abilities[index]);
        SkillNode node = node(c.key + ".ability." + def.id, NodeType.ABILITY, c.school, angle, r, def.name, def.icon);
        node.ability = def.id;
        if (def.ultimate) {
            node.cost = 2;
            node.requiredPowerLevel = 0;
        }
        return node;
    }

    private SkillNode keystone(Content c, int index, double angle, double r) {
        Keystone keystone = Keystone.get(c.keystones[index]);
        SkillNode node = node(c.key + ".keystone." + keystone.id, NodeType.KEYSTONE, c.school, angle, r, keystone.name, c.keystoneIcons[index]);
        node.keystone = keystone.id;
        node.flavor = "Keystones change the rules. Choose carefully.";
        return node;
    }

    /** Builds a wheel and returns {entry ring node, exit ring node}. */
    private String[] wheel(Content c, String key, Wheel wheel, double angle, double r) {
        double cx = px(angle, r);
        double cy = py(angle, r);
        double outward = Math.atan2(cx, -cy);
        String[] ring = new String[6];
        for (int k = 0; k < 6; k++) {
            double a = outward + Math.toRadians(60 * k);
            double x = Math.round((cx + WHEEL_RADIUS * Math.sin(a)) * 10) / 10.0;
            double y = Math.round((cy - WHEEL_RADIUS * Math.cos(a)) * 10) / 10.0;
            SkillNode node = tree.add(new SkillNode(c.key + "." + key + ".ring" + k, NodeType.MINOR, c.school, x, y,
                    STAT_NAMES.getOrDefault(wheel.ringStat, wheel.name), wheel.ringIcon));
            node.stat(wheel.ringStat, wheel.ringValue);
            ring[k] = node.id;
        }
        for (int k = 0; k < 6; k++) {
            tree.link(ring[k], ring[(k + 1) % 6]);
        }
        SkillNode notable = tree.add(new SkillNode(c.key + "." + key + ".notable", NodeType.NOTABLE, c.school, cx, cy, wheel.name, wheel.icon));
        wheel.notable.forEach(notable::stat);
        notable.flavor = wheel.flavor;
        tree.link(notable.id, ring[1]);
        tree.link(notable.id, ring[5]);
        return new String[]{ring[3], ring[0]};
    }

    private String path(Content c, String prefix, int[] counter, String from, double angle, double... radii) {
        String previous = from;
        for (double r : radii) {
            String[] option = c.pathPool[counter[0] % c.pathPool.length];
            counter[0]++;
            SkillNode node = minor(c.key + "." + prefix + counter[0], c.school, angle, r, option[0], Double.parseDouble(option[1]));
            if (previous != null) {
                tree.link(previous, node.id);
            }
            previous = node.id;
        }
        return previous;
    }

    private void buildSchool(Content c) {
        double t = c.school.angle();
        int[] counter = {0};
        String start = c.key + ".start";
        SkillNode a1 = ability(c, 0, t, 235);
        tree.link(start, a1.id);

        // Left branch: wheel L1, ability 3, wheel L2, keystone 1.
        String left = path(c, "path", counter, a1.id, t - 15, 300, 340, 380);
        String[] l1 = wheel(c, "l1", c.wheels[0], t - 18, 455);
        tree.link(left, l1[0]);
        String leftMid = path(c, "path", counter, l1[1], t - 16, 525, 562);
        SkillNode a3 = ability(c, 2, t - 15, 605);
        tree.link(leftMid, a3.id);
        String leftOuter = path(c, "path", counter, a3.id, t - 16, 648, 688);
        String[] l2 = wheel(c, "l2", c.wheels[1], t - 18, 762);
        tree.link(leftOuter, l2[0]);
        String leftEnd = path(c, "path", counter, l2[1], t - 18, 832);
        SkillNode k1 = keystone(c, 0, t - 18, 892);
        tree.link(leftEnd, k1.id);

        // Middle branch: ability 2, wheel M1, wheel M2, mastery, ultimate.
        String middle = path(c, "path", counter, a1.id, t, 290, 330);
        SkillNode a2 = ability(c, 1, t, 380);
        tree.link(middle, a2.id);
        String middle2 = path(c, "path", counter, a2.id, t, 425, 462);
        String[] m1 = wheel(c, "m1", c.wheels[2], t, 540);
        tree.link(middle2, m1[0]);
        String middle3 = path(c, "path", counter, m1[1], t, 620, 660);
        String[] m2 = wheel(c, "m2", c.wheels[3], t, 742);
        tree.link(middle3, m2[0]);
        String middle4 = path(c, "path", counter, m2[1], t, 822, 862);
        SkillNode mastery = node(c.key + ".mastery", NodeType.NOTABLE, c.school, t, 908, c.masteryName, c.masteryIcon);
        c.mastery.forEach(mastery::stat);
        mastery.flavor = "The deepest teaching of " + c.school.displayName + ".";
        tree.link(middle4, mastery.id);
        SkillNode a5 = ability(c, 4, t, 972);
        tree.link(mastery.id, a5.id);

        // Right branch: wheel R1, ability 4, wheel R2, keystone 2.
        String right = path(c, "path", counter, a1.id, t + 15, 300, 340, 380);
        String[] r1 = wheel(c, "r1", c.wheels[4], t + 18, 455);
        tree.link(right, r1[0]);
        String rightMid = path(c, "path", counter, r1[1], t + 16, 525, 562);
        SkillNode a4 = ability(c, 3, t + 15, 605);
        tree.link(rightMid, a4.id);
        String rightOuter = path(c, "path", counter, a4.id, t + 16, 648, 688);
        String[] r2 = wheel(c, "r2", c.wheels[5], t + 18, 762);
        tree.link(rightOuter, r2[0]);
        String rightEnd = path(c, "path", counter, r2[1], t + 18, 832);
        SkillNode k2 = keystone(c, 1, t + 18, 892);
        tree.link(rightEnd, k2.id);

        // Cross links between the branches.
        crossLink(c, "xl1", t - 8, 642, leftOuter, middle3, counter);
        crossLink(c, "xl2", t - 9, 847, leftEnd, middle4, counter);
        crossLink(c, "xr1", t + 8, 642, rightOuter, middle3, counter);
        crossLink(c, "xr2", t + 9, 847, rightEnd, middle4, counter);
    }

    private void crossLink(Content c, String key, double angle, double r, String a, String b, int[] counter) {
        String[] option = c.pathPool[counter[0]++ % c.pathPool.length];
        SkillNode node = minor(c.key + "." + key, c.school, angle, r, option[0], Double.parseDouble(option[1]));
        tree.link(a, node.id);
        tree.link(node.id, b);
    }

    private void buildBridges(Content a, Content b, String[] hybrid) {
        double t = a.school.angle();
        bridge(a, b, "inner", t, 520, 540, "r1", "l1", hybrid[0], hybrid[1], hybrid[2]);
        bridge(a, b, "outer", t, 812, 830, "r2", "l2", hybrid[3], hybrid[4], hybrid[5]);
    }

    private void bridge(Content a, Content b, String key, double t, double r, double rNotable, String fromWheel, String toWheel,
                        String name, String statA, String statB) {
        String id = a.key + "-" + b.key + "." + key;
        SkillNode left = minor(id + ".a", a.school, t + 25, r, statA, 4);
        SkillNode notable = node(id + ".notable", NodeType.NOTABLE, School.NONE, t + 30, rNotable, name, STAT_ICONS.get(statA));
        notable.stat(statA, 10).stat(statB, 10);
        notable.flavor = "Where " + a.school.element.toLowerCase() + " meets " + b.school.element.toLowerCase() + ".";
        SkillNode right = minor(id + ".b", b.school, t + 35, r, statB, 4);
        tree.link(left.id, notable.id);
        tree.link(notable.id, right.id);
        tree.link(nearest(a.key + "." + fromWheel + ".ring", left), left.id);
        tree.link(right.id, nearest(b.key + "." + toWheel + ".ring", right));
    }

    private String nearest(String ringPrefix, SkillNode to) {
        String best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int k = 0; k < 6; k++) {
            SkillNode node = tree.get(ringPrefix + k);
            double d = Math.hypot(node.x - to.x, node.y - to.y);
            if (d < bestDistance) {
                bestDistance = d;
                best = node.id;
            }
        }
        return best;
    }
}
