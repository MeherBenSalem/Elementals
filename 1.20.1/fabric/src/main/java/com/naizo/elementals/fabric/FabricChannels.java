package com.naizo.elementals.fabric;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.compat.Compat;
import net.minecraft.resources.ResourceLocation;

public final class FabricChannels {
    public static final ResourceLocation TO_SERVER = Compat.id(Elementals.MOD_ID, "c2s");
    public static final ResourceLocation TO_CLIENT = Compat.id(Elementals.MOD_ID, "s2c");

    private FabricChannels() {
    }
}
