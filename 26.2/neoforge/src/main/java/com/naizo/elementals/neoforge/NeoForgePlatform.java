package com.naizo.elementals.neoforge;

import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.neoforge.data.NeoForgePlayerData;
import com.naizo.elementals.platform.ElementalsPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;

public class NeoForgePlatform implements ElementalsPlatform {
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
        if (player.connection != null && player.connection.hasChannel(NeoForgePayloads.ToClient.TYPE.id())) {
            PacketDistributor.sendToPlayer(player, new NeoForgePayloads.ToClient(message));
        }
    }

    @Override
    public void sendToServer(String message) {
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new NeoForgePayloads.ToServer(message));
    }

    @Override
    public ElementalPlayerData legacyData(ServerPlayer player) {
        return NeoForgePlayerData.legacy(player);
    }
}
