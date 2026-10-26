package com.naizo.elementals.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Everything Remnants stores about one player. Serialized as JSON. */
public final class PowerProfile {
    public static final int SLOTS = 4;
    public static final int FORMAT = 2;

    public String origin = "";
    public int level;
    public double xp;
    public int bonusPoints;
    public int tomesUsed;
    /** Highest RPG Player Skills level that has already been turned into skill points. */
    public int rasLevelCredited;
    /** Last RPG Player Skills level seen on the server, sent to the client for display. */
    public int rasLevel;
    public final Set<String> allocated = new LinkedHashSet<>();
    public final String[] slots = new String[SLOTS];
    /** Ability id to the game time at which it is ready again. */
    public final Map<String, Long> cooldowns = new HashMap<>();
    /** Named timers for buffs and keystones, as game times. */
    public final Map<String, Long> timers = new HashMap<>();
    public double essence = -1;
    public int castCount;
    public boolean migrated;

    public PowerProfile() {
        for (int i = 0; i < SLOTS; i++) {
            slots[i] = "";
        }
    }

    public School originSchool() {
        return School.byId(origin);
    }

    public long cooldownReady(String ability) {
        return cooldowns.getOrDefault(ability, 0L);
    }

    public long timer(String key) {
        return timers.getOrDefault(key, 0L);
    }

    public boolean active(String key, long now) {
        return timer(key) > now;
    }

    public int slotOf(String ability) {
        for (int i = 0; i < SLOTS; i++) {
            if (ability.equals(slots[i])) {
                return i;
            }
        }
        return -1;
    }

    public JsonObject toJson(boolean forClient) {
        JsonObject o = new JsonObject();
        o.addProperty("format", FORMAT);
        o.addProperty("origin", origin);
        o.addProperty("level", level);
        o.addProperty("xp", xp);
        o.addProperty("bonus_points", bonusPoints);
        o.addProperty("tomes_used", tomesUsed);
        o.addProperty("ras_level_credited", rasLevelCredited);
        o.addProperty("ras_level", rasLevel);
        JsonArray nodes = new JsonArray();
        allocated.forEach(nodes::add);
        o.add("allocated", nodes);
        JsonArray slotArray = new JsonArray();
        for (String slot : slots) {
            slotArray.add(slot == null ? "" : slot);
        }
        o.add("slots", slotArray);
        JsonObject cd = new JsonObject();
        cooldowns.forEach(cd::addProperty);
        o.add("cooldowns", cd);
        JsonObject tm = new JsonObject();
        timers.forEach(tm::addProperty);
        o.add("timers", tm);
        o.addProperty("essence", essence);
        o.addProperty("cast_count", castCount);
        o.addProperty("migrated", migrated);
        return o;
    }

    public String toJsonString() {
        return TreeCodec.compact().toJson(toJson(false));
    }

    public static PowerProfile fromJson(String json) {
        PowerProfile profile = new PowerProfile();
        if (json == null || json.isBlank()) {
            return profile;
        }
        profile.read(JsonParser.parseString(json).getAsJsonObject());
        return profile;
    }

    public void read(JsonObject o) {
        origin = str(o, "origin", "");
        level = (int) num(o, "level", 0);
        xp = num(o, "xp", 0);
        bonusPoints = (int) num(o, "bonus_points", 0);
        tomesUsed = (int) num(o, "tomes_used", 0);
        rasLevelCredited = (int) num(o, "ras_level_credited", 0);
        rasLevel = (int) num(o, "ras_level", 0);
        allocated.clear();
        if (o.has("allocated")) {
            for (JsonElement e : o.getAsJsonArray("allocated")) {
                allocated.add(e.getAsString());
            }
        }
        for (int i = 0; i < SLOTS; i++) {
            slots[i] = "";
        }
        if (o.has("slots")) {
            JsonArray array = o.getAsJsonArray("slots");
            for (int i = 0; i < Math.min(SLOTS, array.size()); i++) {
                slots[i] = array.get(i).getAsString();
            }
        }
        cooldowns.clear();
        if (o.has("cooldowns")) {
            o.getAsJsonObject("cooldowns").entrySet().forEach(e -> cooldowns.put(e.getKey(), e.getValue().getAsLong()));
        }
        timers.clear();
        if (o.has("timers")) {
            o.getAsJsonObject("timers").entrySet().forEach(e -> timers.put(e.getKey(), e.getValue().getAsLong()));
        }
        essence = num(o, "essence", -1);
        castCount = (int) num(o, "cast_count", 0);
        migrated = o.has("migrated") && o.get("migrated").getAsBoolean();
    }

    public void copyFrom(PowerProfile other) {
        read(other.toJson(false));
    }

    private static String str(JsonObject o, String key, String fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : fallback;
    }

    private static double num(JsonObject o, String key, double fallback) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : fallback;
    }
}
