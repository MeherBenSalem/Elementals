package com.naizo.elementals.neoforge.platform;

import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.neoforge.data.NeoForgePlayerData;
import com.naizo.elementals.neoforge.network.SignatureAbilityPacket;
import com.naizo.elementals.platform.ElementalsPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.nio.file.Path;

public class NeoForgePlatform implements ElementalsPlatform {
    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public ElementalPlayerData playerData(Entity entity) {
        return NeoForgePlayerData.get(entity);
    }

    @Override
    public void syncPlayerData(ServerPlayer player) {
        NeoForgePlayerData.sync(player);
    }

    @Override
    public void sendSignatureAbility() {
        ClientPacketDistributor.sendToServer(new SignatureAbilityPacket());
    }
}
