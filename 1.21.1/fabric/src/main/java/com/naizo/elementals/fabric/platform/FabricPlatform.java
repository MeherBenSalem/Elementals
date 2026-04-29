package com.naizo.elementals.fabric.platform;

import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.fabric.ElementalsFabric;
import com.naizo.elementals.platform.ElementalsPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricPlatform implements ElementalsPlatform {
    private static final Map<UUID, ElementalPlayerData> DATA = new ConcurrentHashMap<>();

    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public ElementalPlayerData playerData(Entity entity) {
        if (entity == null) {
            return new ElementalPlayerData();
        }
        return DATA.computeIfAbsent(entity.getUUID(), id -> new ElementalPlayerData());
    }

    @Override
    public void syncPlayerData(ServerPlayer player) {
        // The Fabric port keeps data server-authoritative for this migration pass.
    }

    @Override
    public void sendSignatureAbility() {
        if (clientPlayerPresent()) {
            ClientPlayNetworking.send(new ElementalsFabric.SignaturePayload());
        }
    }

    private static boolean clientPlayerPresent() {
        return Minecraft.getInstance().player != null;
    }
}
