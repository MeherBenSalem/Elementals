package com.naizo.elementals.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data about an ability that both sides need: name, school, icon, description and default
 * parameters. Server behavior lives in the ability package.
 */
public final class AbilityDef {
    public final String id;
    public final School school;
    public final String name;
    public final String icon;
    public final String description;
    public final boolean ultimate;
    public final Map<String, Double> defaults = new LinkedHashMap<>();

    AbilityDef(String id, School school, String name, String icon, boolean ultimate, String description) {
        this.id = id;
        this.school = school;
        this.name = name;
        this.icon = icon;
        this.ultimate = ultimate;
        this.description = description;
    }

    AbilityDef p(String key, double value) {
        defaults.put(key, value);
        return this;
    }

    /** Fills {key} placeholders with the given parameter values. */
    public String describe(Map<String, Double> params) {
        String text = description;
        for (Map.Entry<String, Double> entry : params.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", Stat.trim(entry.getValue(), 1));
        }
        return text;
    }
}
