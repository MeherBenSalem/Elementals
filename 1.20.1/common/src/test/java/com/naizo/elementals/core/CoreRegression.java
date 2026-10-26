package com.naizo.elementals.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Plain-Java checks of the tree, allocation rules, stats, progression and build codes. Run by the check task. */
public final class CoreRegression {
    private static int failures;
    private static int checks;

    public static void main(String[] args) {
        SkillTree tree = DefaultTree.build();
        treeShape(tree);
        layout(tree);
        codec(tree);
        allocation(tree);
        stats(tree);
        progression();
        buildCodes(tree);
        profile();
        config();
        System.out.println("CoreRegression: " + checks + " checks, " + failures + " failures, " + tree.size() + " nodes");
        if (failures > 0) {
            System.exit(1);
        }
    }

    static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            failures++;
            System.err.println("FAIL: " + message);
        }
    }

    static void treeShape(SkillTree tree) {
        List<String> problems = tree.validate(AbilityCatalog.all().keySet(), Keystone.all().keySet());
        check(problems.isEmpty(), "default tree validates: " + problems);
        check(tree.size() >= 500, "default tree is large: " + tree.size());
        check(tree.starts().size() == 6, "six start nodes");
        Set<String> abilities = new HashSet<>();
        Set<String> keystones = new HashSet<>();
        int notables = 0;
        for (SkillNode node : tree.nodes()) {
            if (node.ability != null) {
                check(abilities.add(node.ability), "ability appears once: " + node.ability);
            }
            if (node.keystone != null) {
                check(keystones.add(node.keystone), "keystone appears once: " + node.keystone);
            }
            if (node.type == NodeType.NOTABLE) {
                notables++;
            }
            for (String key : node.stats.keySet()) {
                check(Stat.get(key) != null, node.id + " uses known stat " + key);
            }
        }
        check(abilities.equals(AbilityCatalog.all().keySet()), "every ability is in the tree");
        check(keystones.equals(Keystone.all().keySet()), "every keystone is in the tree");
        check(notables >= 48, "plenty of notables: " + notables);
    }

    static void layout(SkillTree tree) {
        List<SkillNode> nodes = new ArrayList<>(tree.nodes());
        double worst = Double.MAX_VALUE;
        String pair = "";
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                SkillNode a = nodes.get(i);
                SkillNode b = nodes.get(j);
                double gap = Math.hypot(a.x - b.x, a.y - b.y) - a.type.radius - b.type.radius;
                if (gap < worst) {
                    worst = gap;
                    pair = a.id + " / " + b.id;
                }
            }
        }
        check(worst >= 3, "nodes never overlap (closest gap " + worst + " between " + pair + ")");
        double longest = 0;
        String edge = "";
        for (String[] link : tree.edges()) {
            SkillNode a = tree.get(link[0]);
            SkillNode b = tree.get(link[1]);
            double length = Math.hypot(a.x - b.x, a.y - b.y);
            if (length > longest) {
                longest = length;
                edge = link[0] + " - " + link[1];
            }
        }
        check(longest < 140, "no overly long links (" + longest + " " + edge + ")");
    }

    static void codec(SkillTree tree) {
        String json = TreeCodec.write(tree, true);
        SkillTree copy = TreeCodec.read(json);
        check(copy.size() == tree.size(), "round trip keeps node count");
        check(copy.hash() == tree.hash(), "round trip keeps hash");
        check(copy.edges().size() == tree.edges().size(), "round trip keeps links");
        SkillNode original = tree.get("pyro.start");
        SkillNode read = copy.get("pyro.start");
        check(read.stats.equals(original.stats) && read.x == original.x && read.type == NodeType.START, "round trip keeps node data");
        check(TreeCodec.write(copy, false).equals(TreeCodec.write(tree, false)), "round trip is stable");
        try {
            TreeCodec.read("{\"nodes\":[{\"id\":\"a\",\"links\":[\"missing\"]}]}");
            check(false, "unknown link rejected");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage().contains("missing"), "error names the missing node");
        }
    }

    static void allocation(SkillTree tree) {
        Set<String> empty = new LinkedHashSet<>();
        check(tree.canAllocateDirectly(empty, "pyro.start"), "can start at an origin");
        check(!tree.canAllocateDirectly(empty, "pyro.ability.ember_bolt"), "cannot start mid-tree");
        List<String> path = tree.pathTo(empty, "pyro.ability.explosive_orb");
        check(!path.isEmpty() && path.get(0).equals("pyro.start") && path.get(path.size() - 1).equals("pyro.ability.explosive_orb"),
                "path from nothing begins at the nearest start: " + path);
        Set<String> allocated = new LinkedHashSet<>(path);
        check(tree.isValidAllocation(allocated), "path allocation is valid");
        check(!tree.canRemove(allocated, "pyro.start"), "cannot remove the root under other nodes");
        check(tree.canRemove(allocated, "pyro.ability.explosive_orb"), "can remove a leaf");
        Set<String> island = new LinkedHashSet<>(allocated);
        island.add("tide.ability.maelstrom");
        check(!tree.isValidAllocation(island), "disconnected nodes are invalid");
        Set<String> cascade = new LinkedHashSet<>(allocated);
        List<String> removed = tree.removeWithOrphans(cascade, "pyro.ability.ember_bolt");
        check(removed.get(0).equals("pyro.ability.ember_bolt") && removed.size() == allocated.size() - 1, "removing a trunk drops orphans: " + removed);
        check(cascade.equals(Set.of("pyro.start")), "only the root remains");
        List<String> far = tree.pathTo(allocated, "pyro.keystone.pyre_heart");
        check(far.size() > 5 && tree.isValidAllocation(union(allocated, far)), "long paths stay connected");
        List<String> neighbour = tree.pathTo(Set.of("pyro.start"), "storm.start");
        check(neighbour.size() == 3, "neighbouring origins are three steps apart: " + neighbour);
    }

    static Set<String> union(Set<String> a, List<String> b) {
        Set<String> set = new LinkedHashSet<>(a);
        set.addAll(b);
        return set;
    }

    static void stats(SkillTree tree) {
        Set<String> allocated = new LinkedHashSet<>(tree.pathTo(new LinkedHashSet<>(), "pyro.keystone.glass_cannon"));
        StatSheet sheet = StatSheet.of(tree, allocated);
        check(sheet.has(Keystone.GLASS_CANNON), "keystone detected");
        check(sheet.abilities.contains("ember_bolt") && sheet.abilities.contains("inferno_wave"), "abilities detected: " + sheet.abilities);
        double base = StatSheet.of(tree, Set.of("pyro.start")).damageMultiplier(School.PYROMANCY, 0);
        check(Math.abs(base - 1.10) < 1e-9, "start node gives +10% fire: " + base);
        check(sheet.damageMultiplier(School.PYROMANCY, 0) > 1.4, "glass cannon multiplies damage");
        StatSheet capped = new StatSheet();
        capped.totals.put("cooldown_reduction", 400.0);
        check(Math.abs(capped.cooldownMultiplier(50) - 0.5) < 1e-9, "cooldown reduction is capped");
        capped.keystones.add("eternal_tide");
        check(Math.abs(capped.maxEssence(100) - 70) < 1e-9 && capped.essenceRegenPerSecond(4) == 8, "eternal tide halves and doubles");
        check(Stat.describe("fire_damage", 12).equals("+12% Fire Damage"), "percent stat text");
        check(Stat.describe("health_regen", 0.25).equals("+0.25 Health per Second"), "decimal stat text");
        check(Stat.describe("max_health", -3).equals("-3 Max Health"), "negative stat text");
    }

    static void progression() {
        PowersConfig config = new PowersConfig();
        PowerProfile profile = new PowerProfile();
        check(Progression.earnedPoints(config, profile, false) == 2, "starting points");
        int gained = Progression.addXp(config, profile, 40 + 52 + 10);
        check(gained == 2 && profile.level == 2 && Math.abs(profile.xp - 10) < 1e-9, "level ups carry remaining xp");
        check(Progression.earnedPoints(config, profile, false) == 4, "levels give points");
        profile.rasLevelCredited = 10;
        check(Progression.earnedPoints(config, profile, false) == 4, "RPG levels ignored without the mod");
        check(Progression.earnedPoints(config, profile, true) == 12, "auto mode replaces Power levels with RPG levels");
        config.rasMode = "add";
        check(Progression.earnedPoints(config, profile, true) == 14, "add mode stacks both");
        config.maxLevel = 3;
        Progression.addXp(config, profile, 100000);
        check(profile.level == 3 && profile.xp == 0, "max level caps");
    }

    static void buildCodes(SkillTree tree) {
        Set<String> allocated = new LinkedHashSet<>(tree.pathTo(new LinkedHashSet<>(), "storm.ability.tempest"));
        String code = BuildCode.encode(tree, allocated);
        check(code.startsWith(BuildCode.PREFIX) && code.length() < 80, "short code: " + code);
        BuildCode.Decoded decoded = BuildCode.decode(tree, code);
        check(decoded.sameTree() && new HashSet<>(decoded.nodes()).equals(allocated), "code round trip");
        try {
            BuildCode.decode(tree, "RSP1-!!!");
            check(false, "damaged code rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "damaged code rejected");
        }
    }

    static void profile() {
        PowerProfile profile = new PowerProfile();
        profile.origin = "pyromancy";
        profile.level = 7;
        profile.allocated.add("pyro.start");
        profile.slots[0] = "ember_bolt";
        profile.cooldowns.put("ember_bolt", 1234L);
        profile.timers.put("undying_grove", 99L);
        PowerProfile copy = PowerProfile.fromJson(profile.toJsonString());
        check(copy.origin.equals("pyromancy") && copy.level == 7 && copy.allocated.contains("pyro.start"), "profile round trip");
        check(copy.slots[0].equals("ember_bolt") && copy.slots[3].isEmpty() && copy.cooldownReady("ember_bolt") == 1234L, "slots and cooldowns persist");
        check(PowerProfile.fromJson("").level == 0, "empty profile");
    }

    static void config() {
        PowersConfig config = new PowersConfig();
        config.abilityParams.get("meteor").put("damage", 40.0);
        PowersConfig client = PowersConfig.fromClientJson(config.toClientJson());
        check(client.param("meteor", "damage") == 40.0, "client config carries ability params");
        check(config.param("ember_bolt", "cooldown") == 1.5, "default params");
        for (Map.Entry<String, AbilityDef> entry : AbilityCatalog.all().entrySet()) {
            AbilityDef def = entry.getValue();
            check(def.defaults.containsKey("cost") && def.defaults.containsKey("cooldown"), def.id + " has cost and cooldown");
            String text = def.describe(def.defaults);
            check(!text.contains("{"), def.id + " description fills every placeholder: " + text);
        }
    }
}
