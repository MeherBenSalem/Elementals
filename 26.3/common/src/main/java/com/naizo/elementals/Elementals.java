package com.naizo.elementals;

import com.naizo.elementals.platform.ElementalsPlatform;
import com.naizo.elementals.power.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Remnants | Skills and Powers. The mod id stays "elementals" so 1.x worlds keep their items and data. */
public final class Elementals {
    public static final String MOD_ID = "elementals";
    public static final String MOD_NAME = "Remnants | Skills and Powers";
    public static final Logger LOGGER = LoggerFactory.getLogger("Remnants Skills and Powers");
    public static ElementalsPlatform PLATFORM;

    private Elementals() {
    }

    public static void init(ElementalsPlatform platform) {
        PLATFORM = platform;
        PowerManager.loadConfig();
    }
}
