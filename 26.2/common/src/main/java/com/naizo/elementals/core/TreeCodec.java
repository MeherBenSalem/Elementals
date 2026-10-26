package com.naizo.elementals.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reads and writes skill trees as JSON. Used for skill_tree.json and for syncing the tree to clients. */
public final class TreeCodec {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Gson COMPACT = new GsonBuilder().disableHtmlEscaping().create();

    private TreeCodec() {
    }

    public static String write(SkillTree tree, boolean pretty) {
        JsonObject root = new JsonObject();
        root.addProperty("format", tree.formatVersion);
        root.addProperty("name", tree.name);
        JsonArray nodes = new JsonArray();
        List<String[]> edges = tree.edges();
        for (SkillNode node : tree.nodes()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", node.id);
            o.addProperty("type", node.type.id());
            o.addProperty("school", node.school.id);
            o.addProperty("name", node.name);
            o.addProperty("x", node.x);
            o.addProperty("y", node.y);
            o.addProperty("icon", node.icon);
            if (!node.stats.isEmpty()) {
                JsonObject stats = new JsonObject();
                node.stats.forEach(stats::addProperty);
                o.add("stats", stats);
            }
            if (node.ability != null) {
                o.addProperty("ability", node.ability);
            }
            if (node.keystone != null) {
                o.addProperty("keystone", node.keystone);
            }
            if (node.cost != 1) {
                o.addProperty("cost", node.cost);
            }
            if (node.requiredRasLevel > 0 || node.requiredPowerLevel > 0) {
                JsonObject requires = new JsonObject();
                if (node.requiredRasLevel > 0) {
                    requires.addProperty("ras_level", node.requiredRasLevel);
                }
                if (node.requiredPowerLevel > 0) {
                    requires.addProperty("power_level", node.requiredPowerLevel);
                }
                o.add("requires", requires);
            }
            if (node.flavor != null && !node.flavor.isEmpty()) {
                o.addProperty("flavor", node.flavor);
            }
            JsonArray links = new JsonArray();
            for (String[] edge : edges) {
                if (edge[0].equals(node.id)) {
                    links.add(edge[1]);
                }
            }
            if (!links.isEmpty()) {
                o.add("links", links);
            }
            if (!node.onAllocate.isEmpty()) {
                o.add("on_allocate", strings(node.onAllocate));
            }
            if (!node.onRefund.isEmpty()) {
                o.add("on_refund", strings(node.onRefund));
            }
            nodes.add(o);
        }
        root.add("nodes", nodes);
        return (pretty ? PRETTY : COMPACT).toJson(root);
    }

    private static JsonArray strings(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    /** Parses a tree. Throws IllegalArgumentException with a readable message on bad input. */
    public static SkillTree read(String json) {
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(json);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Skill tree is not valid JSON: " + ex.getMessage(), ex);
        }
        if (!parsed.isJsonObject() || !parsed.getAsJsonObject().has("nodes")) {
            throw new IllegalArgumentException("Skill tree must be an object with a \"nodes\" array");
        }
        JsonObject root = parsed.getAsJsonObject();
        SkillTree tree = new SkillTree();
        if (root.has("name")) {
            tree.name = root.get("name").getAsString();
        }
        if (root.has("format")) {
            tree.formatVersion = root.get("format").getAsInt();
        }
        List<String[]> links = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("nodes")) {
            JsonObject o = element.getAsJsonObject();
            if (!o.has("id")) {
                throw new IllegalArgumentException("A node has no \"id\"");
            }
            String id = o.get("id").getAsString();
            try {
                SkillNode node = new SkillNode(id,
                        NodeType.byId(str(o, "type", "minor")),
                        School.byId(str(o, "school", "none")),
                        num(o, "x", 0), num(o, "y", 0),
                        str(o, "name", id),
                        str(o, "icon", "minecraft:amethyst_shard"));
                if (o.has("stats")) {
                    for (Map.Entry<String, JsonElement> stat : o.getAsJsonObject("stats").entrySet()) {
                        node.stat(stat.getKey(), stat.getValue().getAsDouble());
                    }
                }
                node.ability = o.has("ability") && !o.get("ability").isJsonNull() ? o.get("ability").getAsString() : null;
                node.keystone = o.has("keystone") && !o.get("keystone").isJsonNull() ? o.get("keystone").getAsString() : null;
                node.cost = (int) num(o, "cost", 1);
                node.flavor = str(o, "flavor", "");
                if (o.has("requires")) {
                    JsonObject requires = o.getAsJsonObject("requires");
                    node.requiredRasLevel = (int) num(requires, "ras_level", 0);
                    node.requiredPowerLevel = (int) num(requires, "power_level", 0);
                }
                if (o.has("on_allocate")) {
                    o.getAsJsonArray("on_allocate").forEach(e -> node.onAllocate.add(e.getAsString()));
                }
                if (o.has("on_refund")) {
                    o.getAsJsonArray("on_refund").forEach(e -> node.onRefund.add(e.getAsString()));
                }
                if (o.has("links")) {
                    for (JsonElement link : o.getAsJsonArray("links")) {
                        links.add(new String[]{id, link.getAsString()});
                    }
                }
                tree.add(node);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Node " + id + ": " + ex.getMessage(), ex);
            }
        }
        for (String[] link : links) {
            if (!tree.contains(link[1])) {
                throw new IllegalArgumentException("Node " + link[0] + " links to unknown node " + link[1]);
            }
            tree.link(link[0], link[1]);
        }
        return tree;
    }

    private static String str(JsonObject o, String key, String fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : fallback;
    }

    private static double num(JsonObject o, String key, double fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : fallback;
    }

    public static Gson pretty() {
        return PRETTY;
    }

    public static Gson compact() {
        return COMPACT;
    }
}
