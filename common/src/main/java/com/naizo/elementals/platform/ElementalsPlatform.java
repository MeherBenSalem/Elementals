package com.naizo.elementals.platform;

import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.nio.file.Path;

public interface ElementalsPlatform {
    Path configDirectory();

    ElementalPlayerData playerData(Entity entity);

    void syncPlayerData(ServerPlayer player);

    void sendSignatureAbility();
}
