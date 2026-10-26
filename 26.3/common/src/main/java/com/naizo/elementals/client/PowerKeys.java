package com.naizo.elementals.client;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.compat.Compat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Key bindings. Defaults avoid vanilla keys, voice chat (V) and RPG Player Skills (K). Minecraft 26.3 edition (SDL key codes). */
public final class PowerKeys {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Compat.id(Elementals.MOD_ID, "powers"));
    public static final KeyMapping OPEN_TREE = new KeyMapping("key.elementals.open_tree", InputConstants.KEY_O, CATEGORY);
    public static final KeyMapping[] CAST = {
            new KeyMapping("key.elementals.cast_1", InputConstants.KEY_R, CATEGORY),
            new KeyMapping("key.elementals.cast_2", InputConstants.KEY_G, CATEGORY),
            new KeyMapping("key.elementals.cast_3", InputConstants.KEY_Z, CATEGORY),
            new KeyMapping("key.elementals.cast_4", InputConstants.KEY_H, CATEGORY),
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
            if (minecraft.gui.screen() == null) {
                minecraft.gui.setScreen(new SkillTreeScreen());
            }
        }
        for (int i = 0; i < CAST.length; i++) {
            while (CAST[i].consumeClick()) {
                if (minecraft.gui.screen() == null) {
                    ClientPowers.cast(i);
                }
            }
        }
    }
}
