package com.naizo.elementals.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Key bindings. Defaults avoid vanilla keys, voice chat (V) and RPG Player Skills (K). Minecraft 1.20.1 edition. */
public final class PowerKeys {
    public static final String CATEGORY = "key.categories.elementals";
    public static final KeyMapping OPEN_TREE = new KeyMapping("key.elementals.open_tree", GLFW.GLFW_KEY_O, CATEGORY);
    public static final KeyMapping[] CAST = {
            new KeyMapping("key.elementals.cast_1", GLFW.GLFW_KEY_R, CATEGORY),
            new KeyMapping("key.elementals.cast_2", GLFW.GLFW_KEY_G, CATEGORY),
            new KeyMapping("key.elementals.cast_3", GLFW.GLFW_KEY_Z, CATEGORY),
            new KeyMapping("key.elementals.cast_4", GLFW.GLFW_KEY_H, CATEGORY),
    };

    private PowerKeys() {
    }

    public static List<KeyMapping> all() {
        List<KeyMapping> keys = new ArrayList<>();
        keys.add(OPEN_TREE);
        keys.addAll(List.of(CAST));
        return keys;
    }

    public static String castKey(int slot) {
        return CAST[slot].getTranslatedKeyMessage().getString().toUpperCase(Locale.ROOT);
    }

    public static String treeKey() {
        return OPEN_TREE.getTranslatedKeyMessage().getString().toUpperCase(Locale.ROOT);
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }
        while (OPEN_TREE.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new SkillTreeScreen());
            }
        }
        for (int i = 0; i < CAST.length; i++) {
            while (CAST[i].consumeClick()) {
                if (minecraft.screen == null) {
                    ClientPowers.cast(i);
                }
            }
        }
    }
}
