package com.naizo.elementals.fabric;

import com.naizo.elementals.fabric.client.FabricClientNet;
import com.naizo.elementals.platform.ElementalsPlatform;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public class FabricPlatform implements ElementalsPlatform {
    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, String message) {
        if (ServerPlayNetworking.canSend(player, FabricPayloads.ToClient.TYPE)) {
            ServerPlayNetworking.send(player, new FabricPayloads.ToClient(message));
        }
    }

    @Override
    public void sendToServer(String message) {
        FabricClientNet.send(message);
    }
}
