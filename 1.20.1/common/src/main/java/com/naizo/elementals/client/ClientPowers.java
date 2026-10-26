package com.naizo.elementals.client;

import com.google.gson.JsonObject;
import com.naizo.elementals.Elementals;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.PowersConfig;
import com.naizo.elementals.core.Progression;
import com.naizo.elementals.core.SkillTree;
import com.naizo.elementals.core.StatSheet;
import com.naizo.elementals.core.TreeCodec;
import com.naizo.elementals.net.Net;

import java.util.ArrayDeque;
import java.util.Deque;

/** The client's copy of the server state: tree, config and this player's profile. */
public final class ClientPowers {
    public static SkillTree tree;
    public static PowersConfig config = new PowersConfig();
    public static PowerProfile profile = new PowerProfile();
    public static StatSheet sheet = new StatSheet();
    public static boolean rasLoaded;
    public static boolean ready;
    public static double essence;
    public static double maxEssence = 100;
    /** Bumped whenever the profile changes, so screens can refresh cached values. */
    public static int revision;
    public static final Deque<Notice> NOTICES = new ArrayDeque<>();

    public record Notice(String kind, String text, long created) {
    }

    private ClientPowers() {
    }

    public static void reset() {
        tree = null;
        config = new PowersConfig();
        profile = new PowerProfile();
        sheet = new StatSheet();
        ready = false;
        rasLoaded = false;
        NOTICES.clear();
        revision++;
    }

    public static void handle(String text) {
        JsonObject message;
        try {
            message = Net.read(text);
        } catch (RuntimeException ex) {
            Elementals.LOGGER.warn("Bad message from server", ex);
            return;
        }
        switch (Net.type(message)) {
            case Net.INIT -> {
                config = PowersConfig.fromClientJson(message.getAsJsonObject("config"));
                tree = TreeCodec.read(TreeCodec.compact().toJson(message.get("tree")));
                rasLoaded = message.get("ras").getAsBoolean();
                readProfile(message.getAsJsonObject("profile"));
                ready = true;
            }
            case Net.PROFILE -> {
                rasLoaded = message.has("ras") && message.get("ras").getAsBoolean();
                readProfile(message.getAsJsonObject("profile"));
            }
            case Net.ESSENCE -> {
                essence = message.get("e").getAsDouble();
                maxEssence = message.get("m").getAsDouble();
            }
            case Net.NOTICE -> {
                NOTICES.addLast(new Notice(message.get("kind").getAsString(), message.get("text").getAsString(), System.currentTimeMillis()));
                while (NOTICES.size() > 4) {
                    NOTICES.removeFirst();
                }
            }
            default -> {
            }
        }
    }

    private static void readProfile(JsonObject json) {
        PowerProfile next = new PowerProfile();
        next.read(json);
        profile = next;
        sheet = StatSheet.of(tree, profile.allocated);
        if (profile.essence >= 0) {
            essence = profile.essence;
        }
        maxEssence = sheet.maxEssence(config.baseMaxEssence);
        revision++;
    }

    public static int earned() {
        return Progression.earnedPoints(config, profile, rasLoaded);
    }

    public static int available() {
        return tree == null ? 0 : Progression.availablePoints(config, tree, profile, rasLoaded);
    }

    public static boolean replaceMode() {
        return "replace".equals(config.effectiveRasMode(rasLoaded));
    }

    public static double rasBonus() {
        return rasLoaded && !"off".equals(config.effectiveRasMode(true)) ? profile.rasLevel * config.abilityPowerPerRasLevel : 0;
    }

    public static void send(JsonObject message) {
        if (Elementals.PLATFORM != null) {
            Elementals.PLATFORM.sendToServer(Net.write(message));
        }
    }

    public static void cast(int slot) {
        if (!ready) {
            return;
        }
        JsonObject message = Net.message(Net.USE);
        message.addProperty("slot", slot);
        send(message);
    }
}
