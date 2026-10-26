package com.naizo.elementals.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.CRC32;

/** An immutable-by-convention graph of skill nodes with the allocation rules. */
public final class SkillTree {
    private final Map<String, SkillNode> nodes = new LinkedHashMap<>();
    private final List<String> order = new ArrayList<>();
    public String name = "Remnant Constellation";
    public int formatVersion = 1;

    public SkillNode add(SkillNode node) {
        if (nodes.put(node.id, node) == null) {
            order.add(node.id);
        }
        return node;
    }

    public void link(String a, String b) {
        if (a.equals(b)) {
            return;
        }
        SkillNode na = nodes.get(a);
        SkillNode nb = nodes.get(b);
        if (na == null || nb == null) {
            throw new IllegalArgumentException("Cannot link unknown nodes " + a + " - " + b);
        }
        na.links.add(b);
        nb.links.add(a);
    }

    public SkillNode get(String id) {
        return nodes.get(id);
    }

    public boolean contains(String id) {
        return nodes.containsKey(id);
    }

    public Collection<SkillNode> nodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public int size() {
        return nodes.size();
    }

    /** Stable index of a node, used by build codes. */
    public int indexOf(String id) {
        return order.indexOf(id);
    }

    public String idAt(int index) {
        return index >= 0 && index < order.size() ? order.get(index) : null;
    }

    public List<SkillNode> starts() {
        List<SkillNode> starts = new ArrayList<>();
        for (SkillNode node : nodes.values()) {
            if (node.isStart()) {
                starts.add(node);
            }
        }
        return starts;
    }

    /** Every link once, as [a, b] pairs with a before b in insertion order. */
    public List<String[]> edges() {
        List<String[]> edges = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String id : order) {
            for (String other : nodes.get(id).links) {
                String key = id.compareTo(other) < 0 ? id + "|" + other : other + "|" + id;
                if (seen.add(key)) {
                    edges.add(new String[]{id, other});
                }
            }
        }
        return edges;
    }

    public long hash() {
        CRC32 crc = new CRC32();
        for (String id : new TreeSet<>(nodes.keySet())) {
            crc.update(id.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            crc.update(0);
        }
        return crc.getValue();
    }

    public int cost(Collection<String> ids) {
        int total = 0;
        for (String id : ids) {
            SkillNode node = nodes.get(id);
            if (node != null) {
                total += Math.max(0, node.cost);
            }
        }
        return total;
    }

    /**
     * A set is valid when it is empty, or when it holds only known nodes, contains a start node,
     * and is one connected group.
     */
    public boolean isValidAllocation(Set<String> allocated) {
        if (allocated.isEmpty()) {
            return true;
        }
        String root = null;
        for (String id : allocated) {
            SkillNode node = nodes.get(id);
            if (node == null) {
                return false;
            }
            if (root == null && node.isStart()) {
                root = id;
            }
        }
        return root != null && reachable(root, allocated).size() == allocated.size();
    }

    private Set<String> reachable(String from, Set<String> within) {
        Set<String> seen = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(from);
        seen.add(from);
        while (!queue.isEmpty()) {
            for (String next : nodes.get(queue.poll()).links) {
                if (within.contains(next) && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return seen;
    }

    /** True when the node is next to the allocated group, or is a start node and nothing is allocated. */
    public boolean canAllocateDirectly(Set<String> allocated, String id) {
        SkillNode node = nodes.get(id);
        if (node == null || allocated.contains(id)) {
            return false;
        }
        if (allocated.isEmpty()) {
            return node.isStart();
        }
        for (String link : node.links) {
            if (allocated.contains(link)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Shortest chain of unallocated nodes that connects the target to the allocated group, ending with
     * the target. With nothing allocated the chain starts at the nearest start node. Empty when unreachable.
     */
    public List<String> pathTo(Set<String> allocated, String target) {
        if (!nodes.containsKey(target) || allocated.contains(target)) {
            return Collections.emptyList();
        }
        Map<String, String> parent = new HashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        Set<String> sources = new LinkedHashSet<>();
        if (allocated.isEmpty()) {
            for (SkillNode start : starts()) {
                sources.add(start.id);
            }
        } else {
            sources.addAll(allocated);
        }
        for (String source : sources) {
            parent.put(source, null);
            queue.add(source);
        }
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(target)) {
                break;
            }
            for (String next : nodes.get(current).links) {
                if (!parent.containsKey(next)) {
                    parent.put(next, current);
                    queue.add(next);
                }
            }
        }
        if (!parent.containsKey(target)) {
            return Collections.emptyList();
        }
        List<String> path = new ArrayList<>();
        for (String step = target; step != null && !allocated.contains(step); step = parent.get(step)) {
            path.add(0, step);
        }
        return path;
    }

    /** True when the node can be removed and the rest stays a valid allocation. */
    public boolean canRemove(Set<String> allocated, String id) {
        if (!allocated.contains(id)) {
            return false;
        }
        Set<String> rest = new HashSet<>(allocated);
        rest.remove(id);
        return isValidAllocation(rest);
    }

    /**
     * Removes the node and every node that is no longer connected to a start node through the
     * remaining group. Returns the removed ids, the requested node first.
     */
    public List<String> removeWithOrphans(Set<String> allocated, String id) {
        List<String> removed = new ArrayList<>();
        if (!allocated.remove(id)) {
            return removed;
        }
        removed.add(id);
        Set<String> keep = new HashSet<>();
        for (String other : allocated) {
            SkillNode node = nodes.get(other);
            if (node != null && node.isStart()) {
                keep.addAll(reachable(other, allocated));
                break;
            }
        }
        for (String other : new ArrayList<>(allocated)) {
            if (!keep.contains(other)) {
                allocated.remove(other);
                removed.add(other);
            }
        }
        return removed;
    }

    /** Problems that make the tree unusable or confusing; empty when the tree is fine. */
    public List<String> validate(Set<String> knownAbilities, Set<String> knownKeystones) {
        List<String> problems = new ArrayList<>();
        if (starts().isEmpty()) {
            problems.add("The tree has no start nodes");
        }
        for (SkillNode node : nodes.values()) {
            for (String link : node.links) {
                if (!nodes.containsKey(link)) {
                    problems.add(node.id + " links to unknown node " + link);
                }
            }
            if (node.ability != null && !knownAbilities.contains(node.ability)) {
                problems.add(node.id + " unlocks unknown ability " + node.ability);
            }
            if (node.keystone != null && !knownKeystones.contains(node.keystone)) {
                problems.add(node.id + " uses unknown keystone " + node.keystone);
            }
            if (node.links.isEmpty() && nodes.size() > 1) {
                problems.add(node.id + " has no links");
            }
        }
        Set<String> all = new HashSet<>(nodes.keySet());
        Set<String> seen = new HashSet<>();
        for (SkillNode start : starts()) {
            seen.addAll(reachable(start.id, all));
        }
        for (String id : order) {
            if (!seen.contains(id)) {
                problems.add(id + " cannot be reached from any start node");
            }
        }
        return problems;
    }
}
