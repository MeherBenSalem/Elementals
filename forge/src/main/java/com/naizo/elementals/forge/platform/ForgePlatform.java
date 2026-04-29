package com.naizo.elementals.forge.platform;

import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.forge.data.ForgePlayerData;
import com.naizo.elementals.forge.network.ForgeNetwork;
import com.naizo.elementals.forge.network.SignatureAbilityPacket;
import com.naizo.elementals.platform.ElementalsPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatform implements ElementalsPlatform {
    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public ElementalPlayerData playerData(Entity entity) {
        return ForgePlayerData.get(entity);
    }

    @Override
    public void syncPlayerData(ServerPlayer player) {
        ForgePlayerData.sync(player);
    }

    @Override
    public void sendSignatureAbility() {
        ForgeNetwork.CHANNEL.sendToServer(new SignatureAbilityPacket());
    }
}
