package com.naizo.elementals.client;

import com.naizo.elementals.spell.ElementalProgression;
import net.minecraft.world.entity.Entity;

public final class ElementalHudState {
    private ElementalHudState() {
    }

    public static String levelText(Entity entity) {
        return entity == null ? "" : String.valueOf((int) ElementalProgression.data(entity).elementalLevel);
    }

    public static String texture(Entity entity) {
        if (entity == null) {
            return null;
        }
        double element = ElementalProgression.data(entity).element;
        double cooldown = ElementalProgression.data(entity).cooldown;
        String prefix = element == 1 ? "fire" : element == 2 ? "water" : null;
        if (prefix == null) {
            return null;
        }
        if (cooldown <= 0) {
            return prefix;
        }
        if (cooldown <= (element == 1 ? 25 : 75)) {
            return prefix + "_2";
        }
        if (cooldown <= (element == 1 ? 50 : 150)) {
            return prefix + "_1";
        }
        return prefix + "_0";
    }
}
