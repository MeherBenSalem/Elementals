package com.naizo.elementals.power;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.naizo.elementals.Elementals;
import com.naizo.elementals.ability.AbilityRunner;
import com.naizo.elementals.ability.Zones;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.AbilityCatalog;
import com.naizo.elementals.core.DefaultTree;
import com.naizo.elementals.core.Keystone;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.PowersConfig;
import com.naizo.elementals.core.Progression;
import com.naizo.elementals.core.School;
import com.naizo.elementals.core.SkillNode;
import com.naizo.elementals.core.SkillTree;
import com.naizo.elementals.core.StatSheet;
import com.naizo.elementals.core.TreeCodec;
import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.integration.RasBridge;
import com.naizo.elementals.net.Net;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Server-side owner of the config, the skill tree and every online player's profile. */
public final class PowerManager {
    public static PowersConfig CONFIG = new PowersConfig();
    public static SkillTree TREE = DefaultTree.build();
    private static JsonObject treeJson;
    private static final Map<UUID, PowerProfile> PROFILES = new HashMap<>();
    private static final Map<UUID, StatSheet> SHEETS = new HashMap<>();
    private static final Map<UUID, Double> SENT_ESSENCE = new HashMap<>();
    private static MinecraftServer server;
    private static int autosaveTicks;

    private PowerManager() {
    }

    public static Path configDir() {
        return Elementals.PLATFORM.configDirectory().resolve("remnants").resolve("skills_and_powers");
    }

    // ---------------------------------------------------------------- config

    public static List<String> loadConfig() {
        List<String> warnings = new ArrayList<>();
        Path dir = configDir();
        CONFIG = PowersConfig.load(dir, warnings::add);
        SkillTree builtIn = DefaultTree.build();
        SkillTree tree = builtIn;
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("skill_tree.default.json"), TreeCodec.write(builtIn, true), StandardCharsets.UTF_8);
            Path custom = dir.resolve("skill_tree.json");
            if (!Files.exists(custom)) {
                Files.writeString(custom, TreeCodec.write(builtIn, true), StandardCharsets.UTF_8);
            }
            if (CONFIG.useCustomTree) {
                SkillTree loaded = TreeCodec.read(Files.readString(custom, StandardCharsets.UTF_8));
                List<String> problems = loaded.validate(AbilityCatalog.all().keySet(), Keystone.all().keySet());
                if (problems.stream().anyMatch(p -> p.contains("no start nodes") || p.contains("unknown node"))) {
                    warnings.add("skill_tree.json is unusable (" + problems.get(0) + "); using the built-in tree");
                } else {
                    problems.forEach(p -> warnings.add("skill_tree.json: " + p));
                    tree = loaded;
                }
            }
        } catch (IOException | RuntimeException ex) {
            warnings.add("Could not load skill_tree.json: " + ex.getMessage() + "; using the built-in tree");
        }
        TREE = tree;
        treeJson = JsonParser.parseString(TreeCodec.write(TREE, false)).getAsJsonObject();
        warnings.forEach(w -> Elementals.LOGGER.warn(w));
        Elementals.LOGGER.info("Loaded skill tree \"{}\" with {} nodes", TREE.name, TREE.size());
        return warnings;
    }

    /** Re-reads config files and resyncs everyone. */
    public static List<String> reload() {
        List<String> warnings = loadConfig();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PowerProfile profile = profile(player);
                sanitize(player, profile);
                refresh(player);
                sendInit(player);
            }
        }
        return warnings;
    }

    // ---------------------------------------------------------------- lifecycle

    public static void serverStarted(MinecraftServer started) {
        server = started;
    }

    public static void serverStopping(MinecraftServer stopping) {
        for (ServerPlayer player : stopping.getPlayerList().getPlayers()) {
            save(player);
        }
        PROFILES.clear();
        SHEETS.clear();
        SENT_ESSENCE.clear();
        Zones.clear();
        server = null;
    }

    public static void join(ServerPlayer player) {
        if (server == null) {
            server = Compat.server(player);
        }
        PowerProfile profile = PowerStorage.load(Compat.server(player), player.getUUID());
        if (profile == null) {
            profile = new PowerProfile();
        }
        PROFILES.put(player.getUUID(), profile);
        migrateLegacy(player, profile);
        sanitize(player, profile);
        refresh(player);
        sendInit(player);
    }

    public static void leave(ServerPlayer player) {
        save(player);
        PROFILES.remove(player.getUUID());
        SHEETS.remove(player.getUUID());
        SENT_ESSENCE.remove(player.getUUID());
        StatApplier.clear(player);
    }

    /** After respawn or a dimension change the player entity may be new: reapply attributes. */
    public static void respawned(ServerPlayer player) {
        refresh(player);
        sendProfile(player);
    }

    public static void save(ServerPlayer player) {
        PowerProfile profile = PROFILES.get(player.getUUID());
        if (profile != null) {
            PowerStorage.save(Compat.server(player), player.getUUID(), profile);
        }
    }

    // ---------------------------------------------------------------- access

    public static PowerProfile profile(Player player) {
        return PROFILES.computeIfAbsent(player.getUUID(), id -> {
            PowerProfile loaded = server == null ? null : PowerStorage.load(server, id);
            return loaded == null ? new PowerProfile() : loaded;
        });
    }

    public static StatSheet sheet(Player player) {
        return SHEETS.computeIfAbsent(player.getUUID(), id -> StatSheet.of(TREE, profile(player).allocated));
    }

    /** Stats for any entity: players get theirs, everything else an empty sheet. */
    public static StatSheet sheetOf(Entity entity) {
        if (entity instanceof ServerPlayer player && PROFILES.containsKey(player.getUUID())) {
            return sheet(player);
        }
        return EMPTY;
    }

    private static final StatSheet EMPTY = new StatSheet();

    public static boolean rasActive() {
        return RasBridge.isLoaded() && !"off".equals(CONFIG.effectiveRasMode(true));
    }

    public static double rasBonusPercent(Player player) {
        return rasActive() ? profile(player).rasLevel * CONFIG.abilityPowerPerRasLevel : 0;
    }

    public static int available(Player player) {
        return Progression.availablePoints(CONFIG, TREE, profile(player), RasBridge.isLoaded());
    }

    public static double maxEssence(Player player) {
        return sheet(player).maxEssence(CONFIG.baseMaxEssence);
    }

    /** Level used for level requirements: the RPG level in replace mode, otherwise the Power level. */
    public static int progressLevel(Player player) {
        PowerProfile profile = profile(player);
        return "replace".equals(CONFIG.effectiveRasMode(RasBridge.isLoaded())) ? profile.rasLevel : profile.level;
    }

    // ---------------------------------------------------------------- ticking

    public static void tick(MinecraftServer current) {
        server = current;
        for (ServerPlayer player : current.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
        Zones.tick();
        if (++autosaveTicks >= 6000) {
            autosaveTicks = 0;
            current.getPlayerList().getPlayers().forEach(PowerManager::save);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        PowerProfile profile = PROFILES.get(player.getUUID());
        if (profile == null || !player.isAlive()) {
            return;
        }
        StatSheet sheet = sheet(player);
        double max = sheet.maxEssence(CONFIG.baseMaxEssence);
        if (profile.essence < 0 || profile.essence > max) {
            profile.essence = max;
        } else if (profile.essence < max) {
            profile.essence = Math.min(max, profile.essence + sheet.essenceRegenPerSecond(CONFIG.baseEssenceRegen) / 20.0);
        }
        long time = player.level().getGameTime();
        if (time % 20 == 0) {
            double regen = sheet.healthRegenPerSecond();
            if (regen > 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal((float) regen);
            }
            pollRas(player, profile);
        }
        if (time % 5 == 0) {
            Double sent = SENT_ESSENCE.get(player.getUUID());
            if (sent == null || Math.abs(sent - profile.essence) >= 0.5 || (profile.essence >= max && sent < max)) {
                sendEssence(player, profile, max);
            }
        }
        AbilityRunner.tickPlayer(player, profile, sheet, time);
    }

    private static void pollRas(ServerPlayer player, PowerProfile profile) {
        if (!RasBridge.isLoaded()) {
            return;
        }
        int level = RasBridge.level(player);
        boolean changed = level != profile.rasLevel;
        profile.rasLevel = level;
        String mode = CONFIG.effectiveRasMode(true);
        if (!"off".equals(mode) && level > profile.rasLevelCredited) {
            int gained = (level - profile.rasLevelCredited) * CONFIG.pointsPerRasLevel;
            profile.rasLevelCredited = level;
            if (gained > 0) {
                notice(player, "points", Component.translatable("message.elementals.ras_points", gained, level).getString());
            }
            changed = true;
        }
        if (changed) {
            sendProfile(player);
        }
    }

    // ---------------------------------------------------------------- progression

    public static void killed(LivingEntity victim, Player killer) {
        if (!(killer instanceof ServerPlayer player) || victim == killer) {
            return;
        }
        StatSheet sheet = sheet(player);
        PowerProfile profile = profile(player);
        double onKill = sheet.get("essence_on_kill");
        if (sheet.has(Keystone.SOUL_EATER)) {
            onKill += sheet.maxEssence(CONFIG.baseMaxEssence) * 0.15;
            player.heal(2);
        }
        if (onKill > 0) {
            profile.essence = Math.min(sheet.maxEssence(CONFIG.baseMaxEssence), Math.max(0, profile.essence) + onKill);
        }
        if ("replace".equals(CONFIG.effectiveRasMode(RasBridge.isLoaded())) || victim instanceof Player) {
            return;
        }
        double xp = victim.getMaxHealth() * CONFIG.xpPerMobHealth;
        if (victim.getMaxHealth() >= 100) {
            xp *= CONFIG.xpBossMultiplier;
        }
        addXp(player, xp * (1 + sheet.get("power_xp") / 100.0));
    }

    public static void addXp(ServerPlayer player, double amount) {
        PowerProfile profile = profile(player);
        int gained = Progression.addXp(CONFIG, profile, amount);
        if (gained > 0) {
            Compat.playSound(player.level(), player.getX(), player.getY(), player.getZ(), "minecraft:entity.player.levelup", 0.8f, 1.2f);
            notice(player, "level", Component.translatable("message.elementals.level_up", profile.level, gained * CONFIG.pointsPerLevel).getString());
        }
        sendProfile(player);
    }

    // ---------------------------------------------------------------- allocation

    /** Drops nodes that no longer exist and repairs an allocation broken by a tree change. */
    private static void sanitize(ServerPlayer player, PowerProfile profile) {
        Set<String> before = new LinkedHashSet<>(profile.allocated);
        profile.allocated.removeIf(id -> !TREE.contains(id));
        if (!TREE.isValidAllocation(profile.allocated)) {
            Elementals.LOGGER.warn("{}'s skill tree allocation no longer fits the tree; refunding every point", player.getScoreboardName());
            profile.allocated.clear();
        }
        if (Progression.availablePoints(CONFIG, TREE, profile, RasBridge.isLoaded()) < 0) {
            Elementals.LOGGER.warn("{} spent more skill points than they have; refunding every point", player.getScoreboardName());
            profile.allocated.clear();
        }
        if (!before.equals(profile.allocated)) {
            notice(player, "warning", Component.translatable("message.elementals.tree_changed").getString());
        }
        fixSlots(profile);
        updateOrigin(profile);
    }

    private static void migrateLegacy(ServerPlayer player, PowerProfile profile) {
        if (profile.migrated) {
            return;
        }
        profile.migrated = true;
        ElementalPlayerData legacy = Elementals.PLATFORM.legacyData(player);
        if (legacy == null || legacy.element <= 0) {
            return;
        }
        School school = legacy.element == 2 ? School.TIDECALLING : School.PYROMANCY;
        grantOrigin(profile, school);
        profile.bonusPoints += (int) Math.round(legacy.elementalLevel) * 3;
        Elementals.LOGGER.info("Migrated {}'s Elementals 1.x element ({}) to the skill tree", player.getScoreboardName(), school.displayName);
    }

    /** Gives the start node of a school for free when nothing is allocated yet. */
    public static boolean grantOrigin(PowerProfile profile, School school) {
        if (!profile.allocated.isEmpty()) {
            return false;
        }
        for (SkillNode start : TREE.starts()) {
            if (start.school == school) {
                profile.allocated.add(start.id);
                profile.bonusPoints += Math.max(0, start.cost);
                profile.origin = school.id;
                return true;
            }
        }
        return false;
    }

    private static void updateOrigin(PowerProfile profile) {
        School current = profile.originSchool();
        boolean keep = false;
        String fallback = "";
        for (String id : profile.allocated) {
            SkillNode node = TREE.get(id);
            if (node != null && node.isStart()) {
                if (node.school == current) {
                    keep = true;
                }
                if (fallback.isEmpty()) {
                    fallback = node.school.id;
                }
            }
        }
        if (!keep) {
            profile.origin = fallback;
        }
    }

    private static void fixSlots(PowerProfile profile) {
        StatSheet sheet = StatSheet.of(TREE, profile.allocated);
        for (int i = 0; i < PowerProfile.SLOTS; i++) {
            if (!profile.slots[i].isEmpty() && !sheet.abilities.contains(profile.slots[i])) {
                profile.slots[i] = "";
            }
        }
        for (String ability : sheet.abilities) {
            if (profile.slotOf(ability) < 0) {
                for (int i = 0; i < PowerProfile.SLOTS; i++) {
                    if (profile.slots[i].isEmpty()) {
                        profile.slots[i] = ability;
                        break;
                    }
                }
            }
        }
    }

    /** Applies a staged allocation sent by the client. Everything is checked before anything changes. */
    public static void commit(ServerPlayer player, List<String> requested) {
        PowerProfile profile = profile(player);
        Set<String> target = new LinkedHashSet<>();
        for (String id : profile.allocated) {
            if (requested.contains(id)) {
                target.add(id);
            }
        }
        for (String id : requested) {
            if (!TREE.contains(id)) {
                deny(player, "message.elementals.unknown_node", id);
                return;
            }
            target.add(id);
        }
        if (target.equals(profile.allocated)) {
            return;
        }
        if (!TREE.isValidAllocation(target)) {
            deny(player, "message.elementals.not_connected");
            return;
        }
        int earned = Progression.earnedPoints(CONFIG, profile, RasBridge.isLoaded());
        if (TREE.cost(target) > earned) {
            deny(player, "message.elementals.not_enough_points");
            return;
        }
        List<String> removed = new ArrayList<>();
        for (String id : profile.allocated) {
            if (!target.contains(id)) {
                removed.add(id);
            }
        }
        List<String> added = new ArrayList<>();
        for (String id : target) {
            if (!profile.allocated.contains(id)) {
                added.add(id);
            }
        }
        int level = progressLevel(player);
        for (String id : added) {
            SkillNode node = TREE.get(id);
            if (node.requiredRasLevel > 0 && RasBridge.isLoaded() && profile.rasLevel < node.requiredRasLevel) {
                deny(player, "message.elementals.requires_ras_level", node.name, node.requiredRasLevel);
                return;
            }
            if (node.requiredPowerLevel > level) {
                deny(player, "message.elementals.requires_level", node.name, node.requiredPowerLevel);
                return;
            }
        }
        int refundCost = removed.size() * CONFIG.refundCostLevels;
        if (refundCost > 0 && !player.getAbilities().instabuild) {
            if (player.experienceLevel < refundCost) {
                deny(player, "message.elementals.refund_cost", refundCost);
                return;
            }
            player.giveExperienceLevels(-refundCost);
        }
        profile.allocated.clear();
        profile.allocated.addAll(target);
        updateOrigin(profile);
        fixSlots(profile);
        for (String id : removed) {
            runCommands(player, TREE.get(id).onRefund);
        }
        for (String id : added) {
            runCommands(player, TREE.get(id).onAllocate);
        }
        refresh(player);
        sendProfile(player);
        save(player);
        if (!added.isEmpty()) {
            Compat.playSound(player.level(), player.getX(), player.getY(), player.getZ(), "minecraft:block.amethyst_block.chime", 1f, 0.8f);
        }
        notice(player, "commit", Component.translatable("message.elementals.committed", added.size(), removed.size()).getString());
    }

    /** Refunds every node. Charges the configured XP levels unless free is set. */
    public static boolean reset(ServerPlayer player, boolean free) {
        PowerProfile profile = profile(player);
        if (profile.allocated.isEmpty()) {
            return false;
        }
        int cost = CONFIG.fullResetCostLevels;
        if (!free && cost > 0 && !player.getAbilities().instabuild) {
            if (player.experienceLevel < cost) {
                deny(player, "message.elementals.reset_cost", cost);
                return false;
            }
            player.giveExperienceLevels(-cost);
        }
        for (String id : profile.allocated) {
            SkillNode node = TREE.get(id);
            if (node != null) {
                runCommands(player, node.onRefund);
            }
        }
        profile.allocated.clear();
        profile.origin = "";
        fixSlots(profile);
        refresh(player);
        sendProfile(player);
        save(player);
        Compat.playSound(player.level(), player.getX(), player.getY(), player.getZ(), "minecraft:block.respawn_anchor.deplete", 1f, 1f);
        notice(player, "reset", Component.translatable("message.elementals.reset_done").getString());
        return true;
    }

    public static void setSlot(ServerPlayer player, int slot, String ability) {
        PowerProfile profile = profile(player);
        if (slot < 0 || slot >= PowerProfile.SLOTS) {
            return;
        }
        if (ability.isEmpty()) {
            profile.slots[slot] = "";
        } else {
            if (!sheet(player).abilities.contains(ability)) {
                return;
            }
            int existing = profile.slotOf(ability);
            if (existing >= 0) {
                profile.slots[existing] = profile.slots[slot];
            }
            profile.slots[slot] = ability;
        }
        sendProfile(player);
    }

    private static void runCommands(ServerPlayer player, List<String> commands) {
        if (commands.isEmpty() || server == null) {
            return;
        }
        for (String command : commands) {
            String line = command.replace("{player}", player.getScoreboardName()).replace("{uuid}", player.getStringUUID());
            try {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), line.startsWith("/") ? line.substring(1) : line);
            } catch (RuntimeException ex) {
                Elementals.LOGGER.warn("Node command failed: {}", line, ex);
            }
        }
    }

    /** Recomputes the stat sheet and reapplies vanilla attributes. */
    public static void refresh(ServerPlayer player) {
        SHEETS.put(player.getUUID(), StatSheet.of(TREE, profile(player).allocated));
        StatApplier.apply(player, sheet(player));
    }

    // ---------------------------------------------------------------- messages

    public static void handle(ServerPlayer player, String text) {
        JsonObject message;
        try {
            message = Net.read(text);
        } catch (RuntimeException ex) {
            return;
        }
        switch (Net.type(message)) {
            case Net.HELLO -> sendInit(player);
            case Net.COMMIT -> {
                List<String> nodes = new ArrayList<>();
                if (message.has("nodes")) {
                    for (JsonElement element : message.getAsJsonArray("nodes")) {
                        nodes.add(element.getAsString());
                    }
                }
                commit(player, nodes);
            }
            case Net.RESET -> reset(player, false);
            case Net.SLOT -> setSlot(player, message.get("slot").getAsInt(), message.has("ability") ? message.get("ability").getAsString() : "");
            case Net.USE -> AbilityRunner.cast(player, message.get("slot").getAsInt());
            default -> {
            }
        }
    }

    public static void sendInit(ServerPlayer player) {
        JsonObject message = Net.message(Net.INIT);
        message.add("config", CONFIG.toClientJson());
        message.add("tree", treeJson);
        message.addProperty("ras", RasBridge.isLoaded());
        message.add("profile", profileJson(player));
        Elementals.PLATFORM.sendToPlayer(player, Net.write(message));
    }

    public static void sendProfile(ServerPlayer player) {
        if (!PROFILES.containsKey(player.getUUID())) {
            return;
        }
        JsonObject message = Net.message(Net.PROFILE);
        message.add("profile", profileJson(player));
        message.addProperty("ras", RasBridge.isLoaded());
        Elementals.PLATFORM.sendToPlayer(player, Net.write(message));
    }

    private static JsonObject profileJson(ServerPlayer player) {
        JsonObject profile = profile(player).toJson(true);
        profile.addProperty("time", player.level().getGameTime());
        return profile;
    }

    private static void sendEssence(ServerPlayer player, PowerProfile profile, double max) {
        SENT_ESSENCE.put(player.getUUID(), profile.essence);
        JsonObject message = Net.message(Net.ESSENCE);
        message.addProperty("e", Math.round(profile.essence * 10) / 10.0);
        message.addProperty("m", Math.round(max * 10) / 10.0);
        Elementals.PLATFORM.sendToPlayer(player, Net.write(message));
    }

    public static void essenceChanged(ServerPlayer player) {
        PowerProfile profile = profile(player);
        sendEssence(player, profile, maxEssence(player));
    }

    public static void notice(ServerPlayer player, String kind, String text) {
        JsonObject message = Net.message(Net.NOTICE);
        message.addProperty("kind", kind);
        message.addProperty("text", text);
        Elementals.PLATFORM.sendToPlayer(player, Net.write(message));
    }

    public static void deny(ServerPlayer player, String key, Object... args) {
        Compat.overlay(player, Component.translatable(key, args).withStyle(ChatFormatting.RED));
        notice(player, "error", Component.translatable(key, args).getString());
    }

    public static JsonArray idArray(Iterable<String> ids) {
        JsonArray array = new JsonArray();
        ids.forEach(array::add);
        return array;
    }
}
