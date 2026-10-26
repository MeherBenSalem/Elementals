package com.naizo.elementals.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Server configuration in config/remnants/skills_and_powers/. Missing keys are filled with defaults and
 * written back, so the files always list every option.
 */
public final class PowersConfig {
    // progression
    public int startingPoints = 2;
    public int pointsPerLevel = 1;
    public int maxLevel = 100;
    public double xpBase = 40;
    public double xpPerLevel = 12;
    public double xpPerMobHealth = 1.0;
    public double xpBossMultiplier = 3.0;
    public double xpPerCast = 1;
    // RPG Player Skills integration
    public String rasMode = "auto";
    public int pointsPerRasLevel = 1;
    public double abilityPowerPerRasLevel = 0.5;
    // respec
    public int refundCostLevels = 1;
    public int fullResetCostLevels = 10;
    // essence
    public double baseMaxEssence = 100;
    public double baseEssenceRegen = 4;
    // combat
    public boolean abilitiesHurtPlayers = false;
    public boolean abilitiesHurtTamed = false;
    public double maxCooldownReduction = 50;
    public int globalCooldownTicks = 6;
    // tree
    public boolean useCustomTree = false;
    // items
    public int tomePoints = 1;
    public int tomeCap = 20;
    public int explosiveOrbBookLevel = 0;
    public int emberShieldBookLevel = 10;
    public int infernoWaveBookLevel = 20;
    // mobs
    public double fireRuneDropChance = 0.2;
    public double waterRuneDropChance = 0.15;

    public final Map<String, Boolean> abilityEnabled = new LinkedHashMap<>();
    public final Map<String, Map<String, Double>> abilityParams = new LinkedHashMap<>();

    public PowersConfig() {
        for (AbilityDef def : AbilityCatalog.all().values()) {
            abilityEnabled.put(def.id, true);
            abilityParams.put(def.id, new LinkedHashMap<>(def.defaults));
        }
    }

    public String effectiveRasMode(boolean rasActive) {
        if (!rasActive) {
            return "off";
        }
        if ("auto".equals(rasMode)) {
            return "replace";
        }
        return rasMode;
    }

    public double param(String ability, String key) {
        Map<String, Double> params = abilityParams.get(ability);
        if (params != null && params.containsKey(key)) {
            return params.get(key);
        }
        AbilityDef def = AbilityCatalog.get(ability);
        return def == null ? 0 : def.defaults.getOrDefault(key, 0.0);
    }

    public Map<String, Double> params(String ability) {
        Map<String, Double> params = abilityParams.get(ability);
        if (params == null) {
            AbilityDef def = AbilityCatalog.get(ability);
            return def == null ? Map.of() : def.defaults;
        }
        return params;
    }

    public boolean enabled(String ability) {
        return abilityEnabled.getOrDefault(ability, true);
    }

    // ---------------------------------------------------------------- files

    public static PowersConfig load(Path dir, java.util.function.Consumer<String> warn) {
        PowersConfig config = new PowersConfig();
        try {
            Files.createDirectories(dir);
            config.migrateLegacy(dir.getParent().resolve("elementals"), warn);
            config.readGeneral(dir.resolve("general.json"), warn);
            config.readAbilities(dir.resolve("abilities.json"), warn);
        } catch (IOException ex) {
            warn.accept("Could not create config directory " + dir + ": " + ex.getMessage());
        }
        return config;
    }

    private void readGeneral(Path file, java.util.function.Consumer<String> warn) throws IOException {
        JsonObject root = readObject(file, warn);
        Section s = new Section(root);
        s.help("_readme", "Remnants | Skills and Powers server config. Reload with /powers reload. See the docs folder on GitHub for every option.");
        JsonObject p = s.section("progression");
        Section ps = new Section(p);
        startingPoints = ps.i("starting_points", startingPoints);
        pointsPerLevel = ps.i("points_per_level", pointsPerLevel);
        maxLevel = ps.i("max_level", maxLevel);
        xpBase = ps.d("xp_base", xpBase);
        xpPerLevel = ps.d("xp_per_level", xpPerLevel);
        xpPerMobHealth = ps.d("xp_per_mob_max_health", xpPerMobHealth);
        xpBossMultiplier = ps.d("xp_boss_multiplier", xpBossMultiplier);
        xpPerCast = ps.d("xp_per_ability_cast", xpPerCast);
        Section rs = new Section(s.section("rpg_player_skills"));
        rs.help("_help", "Integration with Remnants | RPG Player Skills (rpg_attribute_system). mode: auto (replace when installed), replace (skill points come from RPG levels), add (RPG levels add points on top of Power levels), off.");
        rasMode = rs.s("mode", rasMode);
        pointsPerRasLevel = rs.i("skill_points_per_rpg_level", pointsPerRasLevel);
        abilityPowerPerRasLevel = rs.d("ability_damage_percent_per_rpg_level", abilityPowerPerRasLevel);
        Section re = new Section(s.section("respec"));
        refundCostLevels = re.i("refund_cost_xp_levels_per_node", refundCostLevels);
        fullResetCostLevels = re.i("full_reset_cost_xp_levels", fullResetCostLevels);
        Section es = new Section(s.section("essence"));
        baseMaxEssence = es.d("base_max_essence", baseMaxEssence);
        baseEssenceRegen = es.d("base_regen_per_second", baseEssenceRegen);
        Section cs = new Section(s.section("combat"));
        abilitiesHurtPlayers = cs.b("abilities_hurt_players", abilitiesHurtPlayers);
        abilitiesHurtTamed = cs.b("abilities_hurt_tamed_animals", abilitiesHurtTamed);
        maxCooldownReduction = cs.d("max_cooldown_reduction_percent", maxCooldownReduction);
        globalCooldownTicks = cs.i("global_cooldown_ticks", globalCooldownTicks);
        Section ts = new Section(s.section("tree"));
        ts.help("_help", "Set use_custom_tree to true to load skill_tree.json instead of the built-in tree. skill_tree.default.json is rewritten on every start for reference.");
        useCustomTree = ts.b("use_custom_tree", useCustomTree);
        Section is = new Section(s.section("items"));
        tomePoints = is.i("remnant_tome_skill_points", tomePoints);
        tomeCap = is.i("remnant_tome_max_uses", tomeCap);
        explosiveOrbBookLevel = is.i("explosive_orb_book_required_level", explosiveOrbBookLevel);
        emberShieldBookLevel = is.i("ember_shield_book_required_level", emberShieldBookLevel);
        infernoWaveBookLevel = is.i("inferno_wave_book_required_level", infernoWaveBookLevel);
        Section ms = new Section(s.section("mobs"));
        fireRuneDropChance = ms.d("blaze_fire_rune_drop_chance", fireRuneDropChance);
        waterRuneDropChance = ms.d("drowned_water_rune_drop_chance", waterRuneDropChance);
        if (s.changed || ps.changed || rs.changed || re.changed || es.changed || cs.changed || ts.changed || is.changed || ms.changed || !Files.exists(file)) {
            write(file, root);
        }
    }

    private void readAbilities(Path file, java.util.function.Consumer<String> warn) throws IOException {
        JsonObject root = readObject(file, warn);
        boolean changed = !Files.exists(file);
        for (AbilityDef def : AbilityCatalog.all().values()) {
            JsonObject entry = root.has(def.id) && root.get(def.id).isJsonObject() ? root.getAsJsonObject(def.id) : new JsonObject();
            if (!root.has(def.id)) {
                root.add(def.id, entry);
                changed = true;
            }
            Section section = new Section(entry);
            abilityEnabled.put(def.id, section.b("enabled", abilityEnabled.getOrDefault(def.id, true)));
            Map<String, Double> params = abilityParams.get(def.id);
            for (Map.Entry<String, Double> param : def.defaults.entrySet()) {
                params.put(param.getKey(), section.d(param.getKey(), params.getOrDefault(param.getKey(), param.getValue())));
            }
            changed |= section.changed;
        }
        if (changed) {
            write(file, root);
        }
    }

    /** Carries values over from the 1.x elementals/*.properties files the first time. */
    private void migrateLegacy(Path legacyDir, java.util.function.Consumer<String> warn) {
        Path fire = legacyDir.resolve("fire_spells.properties");
        if (!Files.exists(fire)) {
            return;
        }
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(fire, StandardCharsets.UTF_8)) {
            properties.load(reader);
            copy(properties, "explosive_orb_cdr", "explosive_orb", "cooldown", 1 / 20.0);
            copy(properties, "ember_shield_cdr", "ember_shield", "cooldown", 1 / 20.0);
            copy(properties, "inferno_wave_cdr", "inferno_wave", "cooldown", 1 / 20.0);
            copy(properties, "inferno_wave_damage", "inferno_wave", "damage", 1);
        } catch (IOException | RuntimeException ex) {
            warn.accept("Could not read legacy config " + fire + ": " + ex.getMessage());
        }
    }

    private void copy(Properties properties, String key, String ability, String param, double scale) {
        String value = properties.getProperty(key);
        if (value != null) {
            abilityParams.get(ability).put(param, Double.parseDouble(value.trim()) * scale);
        }
    }

    private static JsonObject readObject(Path file, java.util.function.Consumer<String> warn) {
        if (Files.exists(file)) {
            try {
                JsonElement element = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                if (element.isJsonObject()) {
                    return element.getAsJsonObject();
                }
                warn.accept(file + " is not a JSON object; using defaults");
            } catch (IOException | RuntimeException ex) {
                warn.accept("Could not read " + file + ": " + ex.getMessage() + "; using defaults");
            }
        }
        return new JsonObject();
    }

    public static void write(Path file, JsonObject root) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, TreeCodec.pretty().toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- client sync

    /** The subset of the config the client needs for tooltips, bars and the tree screen. */
    public JsonObject toClientJson() {
        JsonObject o = new JsonObject();
        o.addProperty("starting_points", startingPoints);
        o.addProperty("points_per_level", pointsPerLevel);
        o.addProperty("max_level", maxLevel);
        o.addProperty("xp_base", xpBase);
        o.addProperty("xp_per_level", xpPerLevel);
        o.addProperty("ras_mode", rasMode);
        o.addProperty("points_per_ras_level", pointsPerRasLevel);
        o.addProperty("ability_power_per_ras_level", abilityPowerPerRasLevel);
        o.addProperty("refund_cost", refundCostLevels);
        o.addProperty("full_reset_cost", fullResetCostLevels);
        o.addProperty("base_max_essence", baseMaxEssence);
        o.addProperty("base_essence_regen", baseEssenceRegen);
        o.addProperty("max_cdr", maxCooldownReduction);
        JsonObject abilities = new JsonObject();
        for (Map.Entry<String, Map<String, Double>> entry : abilityParams.entrySet()) {
            JsonObject params = new JsonObject();
            entry.getValue().forEach(params::addProperty);
            params.addProperty("enabled", enabled(entry.getKey()) ? 1 : 0);
            abilities.add(entry.getKey(), params);
        }
        o.add("abilities", abilities);
        return o;
    }

    public static PowersConfig fromClientJson(JsonObject o) {
        PowersConfig config = new PowersConfig();
        Section s = new Section(o);
        config.startingPoints = s.i("starting_points", config.startingPoints);
        config.pointsPerLevel = s.i("points_per_level", config.pointsPerLevel);
        config.maxLevel = s.i("max_level", config.maxLevel);
        config.xpBase = s.d("xp_base", config.xpBase);
        config.xpPerLevel = s.d("xp_per_level", config.xpPerLevel);
        config.rasMode = s.s("ras_mode", config.rasMode);
        config.pointsPerRasLevel = s.i("points_per_ras_level", config.pointsPerRasLevel);
        config.abilityPowerPerRasLevel = s.d("ability_power_per_ras_level", config.abilityPowerPerRasLevel);
        config.refundCostLevels = s.i("refund_cost", config.refundCostLevels);
        config.fullResetCostLevels = s.i("full_reset_cost", config.fullResetCostLevels);
        config.baseMaxEssence = s.d("base_max_essence", config.baseMaxEssence);
        config.baseEssenceRegen = s.d("base_essence_regen", config.baseEssenceRegen);
        config.maxCooldownReduction = s.d("max_cdr", config.maxCooldownReduction);
        if (o.has("abilities")) {
            for (Map.Entry<String, JsonElement> entry : o.getAsJsonObject("abilities").entrySet()) {
                Map<String, Double> params = config.abilityParams.computeIfAbsent(entry.getKey(), k -> new LinkedHashMap<>());
                for (Map.Entry<String, JsonElement> param : entry.getValue().getAsJsonObject().entrySet()) {
                    if (param.getKey().equals("enabled")) {
                        config.abilityEnabled.put(entry.getKey(), param.getValue().getAsDouble() > 0);
                    } else {
                        params.put(param.getKey(), param.getValue().getAsDouble());
                    }
                }
            }
        }
        return config;
    }

    /** Reads values from a JSON object, inserting defaults for missing keys. */
    static final class Section {
        final JsonObject o;
        boolean changed;

        Section(JsonObject o) {
            this.o = o;
        }

        JsonObject section(String key) {
            if (!o.has(key) || !o.get(key).isJsonObject()) {
                o.add(key, new JsonObject());
                changed = true;
            }
            return o.getAsJsonObject(key);
        }

        void help(String key, String text) {
            if (!o.has(key)) {
                o.addProperty(key, text);
                changed = true;
            }
        }

        double d(String key, double fallback) {
            if (o.has(key) && o.get(key).isJsonPrimitive()) {
                try {
                    return o.get(key).getAsDouble();
                } catch (RuntimeException ignored) {
                    // fall through to the default
                }
            }
            o.addProperty(key, fallback);
            changed = true;
            return fallback;
        }

        int i(String key, int fallback) {
            if (o.has(key) && o.get(key).isJsonPrimitive()) {
                try {
                    return (int) Math.round(o.get(key).getAsDouble());
                } catch (RuntimeException ignored) {
                    // fall through to the default
                }
            }
            o.addProperty(key, fallback);
            changed = true;
            return fallback;
        }

        boolean b(String key, boolean fallback) {
            if (o.has(key) && o.get(key).isJsonPrimitive()) {
                JsonElement e = o.get(key);
                if (e.getAsJsonPrimitive().isBoolean()) {
                    return e.getAsBoolean();
                }
                if (e.getAsJsonPrimitive().isNumber()) {
                    return e.getAsDouble() != 0;
                }
            }
            o.addProperty(key, fallback);
            changed = true;
            return fallback;
        }

        String s(String key, String fallback) {
            if (o.has(key) && o.get(key).isJsonPrimitive()) {
                return o.get(key).getAsString();
            }
            o.addProperty(key, fallback);
            changed = true;
            return fallback;
        }
    }
}
