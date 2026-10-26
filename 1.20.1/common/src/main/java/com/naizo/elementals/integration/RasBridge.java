package com.naizo.elementals.integration;

import com.naizo.elementals.Elementals;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

/**
 * Optional link to Remnants | RPG Player Skills (mod id rpg_attribute_system). It is reached through
 * its public RasApi by reflection, so neither mod needs the other to run.
 */
public final class RasBridge {
    public static final String MOD_ID = "rpg_attribute_system";
    private static Boolean present;
    private static Method getLevel;

    private RasBridge() {
    }

    public static boolean isLoaded() {
        if (present == null) {
            present = false;
            try {
                if (Elementals.PLATFORM == null || Elementals.PLATFORM.isModLoaded(MOD_ID)) {
                    Class<?> api = Class.forName("tn.nightbeam.ras.api.RasApi");
                    getLevel = api.getMethod("getLevel", Player.class);
                    present = true;
                    Elementals.LOGGER.info("RPG Player Skills found: RPG levels now grant skill points");
                }
            } catch (ReflectiveOperationException | LinkageError ex) {
                Elementals.LOGGER.warn("RPG Player Skills is installed but its API could not be reached; integration disabled", ex);
            }
        }
        return present;
    }

    /** The player's RPG level, or 0 when the mod is absent or the call fails. Server side only. */
    public static int level(Player player) {
        if (!isLoaded()) {
            return 0;
        }
        try {
            Object value = getLevel.invoke(null, player);
            return value instanceof Number number ? number.intValue() : 0;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Elementals.LOGGER.debug("RasApi.getLevel failed", ex);
            return 0;
        }
    }
}
