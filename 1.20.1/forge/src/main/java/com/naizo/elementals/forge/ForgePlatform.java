package com.naizo.elementals.forge;

import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.forge.data.ForgePlayerData;
import com.naizo.elementals.platform.ElementalsPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatform implements ElementalsPlatform {
    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, String message) {
        ForgeNetwork.sendToPlayer(player, message);
    }

    @Override
    public void sendToServer(String message) {
        ForgeNetwork.sendToServer(message);
    }

    @Override
    public ElementalPlayerData legacyData(ServerPlayer player) {
        return ForgePlayerData.legacy(player);
    }
}
