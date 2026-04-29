package com.naizo.elementals.config;

import com.naizo.elementals.Elementals;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class ElementalsConfig {
    private static final Map<String, Double> FIRE_DEFAULTS = new LinkedHashMap<>();
    private static final Map<String, Double> WATER_DEFAULTS = new LinkedHashMap<>();
    private static final Properties FIRE = new Properties();
    private static final Properties WATER = new Properties();

    static {
        FIRE_DEFAULTS.put("burn_timer_signature_level_0", 3D);
        FIRE_DEFAULTS.put("burn_timer_signature_level_1", 5D);
        FIRE_DEFAULTS.put("explosive_orb_explosion_power", 6D);
        FIRE_DEFAULTS.put("explosive_orb_cdr", 100D);
        FIRE_DEFAULTS.put("ember_shield_absorption_lvl", 3D);
        FIRE_DEFAULTS.put("ember_shield_cdr", 200D);
        FIRE_DEFAULTS.put("inferno_wave_damage", 3D);
        FIRE_DEFAULTS.put("inferno_wave_cdr", 300D);
        WATER_DEFAULTS.put("water_breathing_level", 1D);
        WATER_DEFAULTS.put("dolphin_grace_level", 1D);
    }

    private ElementalsConfig() {
    }

    public static void load(Path configRoot) {
        Path dir = configRoot.resolve(Elementals.MOD_ID);
        loadFile(dir.resolve("fire_spells.properties"), FIRE, FIRE_DEFAULTS);
        loadFile(dir.resolve("water_spells.properties"), WATER, WATER_DEFAULTS);
    }

    public static double fire(String key) {
        return number(FIRE, FIRE_DEFAULTS, key);
    }

    public static double water(String key) {
        return number(WATER, WATER_DEFAULTS, key);
    }

    private static double number(Properties properties, Map<String, Double> defaults, String key) {
        return Double.parseDouble(properties.getProperty(key, String.valueOf(defaults.getOrDefault(key, 0D))));
    }

    private static void loadFile(Path path, Properties properties, Map<String, Double> defaults) {
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    properties.load(reader);
                }
            }
            boolean changed = false;
            for (Map.Entry<String, Double> entry : defaults.entrySet()) {
                if (!properties.containsKey(entry.getKey())) {
                    properties.setProperty(entry.getKey(), strip(entry.getValue()));
                    changed = true;
                }
            }
            if (changed || !Files.exists(path)) {
                try (Writer writer = Files.newBufferedWriter(path)) {
                    properties.store(writer, "Elementals migrated config");
                }
            }
        } catch (IOException ex) {
            Elementals.LOGGER.warn("Could not load Elementals config {}", path, ex);
            defaults.forEach((key, value) -> properties.setProperty(key, strip(value)));
        }
    }

    private static String strip(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
    }
}
