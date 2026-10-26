package com.naizo.elementals.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** One node of the skill tree. Mutable while a tree is built, then treated as read-only. */
public final class SkillNode {
    public final String id;
    public String name;
    public NodeType type;
    public School school;
    public double x;
    public double y;
    public String icon;
    public final Map<String, Double> stats = new LinkedHashMap<>();
    /** Ability unlocked by this node, or null. */
    public String ability;
    /** Keystone rule enabled by this node, or null. */
    public String keystone;
    public String flavor = "";
    public int cost = 1;
    public int requiredRasLevel;
    public int requiredPowerLevel;
    public final List<String> onAllocate = new ArrayList<>();
    public final List<String> onRefund = new ArrayList<>();
    final Set<String> links = new LinkedHashSet<>();

    public SkillNode(String id, NodeType type, School school, double x, double y, String name, String icon) {
        this.id = id;
        this.type = type;
        this.school = school;
        this.x = x;
        this.y = y;
        this.name = name;
        this.icon = icon;
    }

    public SkillNode stat(String key, double value) {
        stats.merge(key, value, Double::sum);
        return this;
    }

    public Set<String> links() {
        return Collections.unmodifiableSet(links);
    }

    public boolean isStart() {
        return type == NodeType.START;
    }

    /** Lines shown in a tooltip for the node's stats. */
    public List<String> statLines() {
        List<String> lines = new ArrayList<>();
        stats.forEach((key, value) -> lines.add(Stat.describe(key, value)));
        return lines;
    }
}
