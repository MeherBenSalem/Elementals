package com.naizo.elementals;

import com.naizo.elementals.config.ElementalsConfig;
import com.naizo.elementals.platform.ElementalsPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Elementals {
    public static final String MOD_ID = "elementals";
    public static final Logger LOGGER = LoggerFactory.getLogger("Elementals");
    public static ElementalsPlatform PLATFORM;

    private Elementals() {
    }

    public static void init(ElementalsPlatform platform) {
        PLATFORM = platform;
        ElementalsConfig.load(platform.configDirectory());
    }
}
