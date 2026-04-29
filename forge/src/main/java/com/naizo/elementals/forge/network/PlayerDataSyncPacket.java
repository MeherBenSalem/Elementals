package com.naizo.elementals.forge.network;

import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.forge.data.ForgePlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerDataSyncPacket {
    private final ElementalPlayerData data;

    public PlayerDataSyncPacket(ElementalPlayerData data) {
        this.data = data;
    }

    public static void encode(PlayerDataSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data.save());
    }

    public static PlayerDataSyncPacket decode(FriendlyByteBuf buffer) {
        ElementalPlayerData data = new ElementalPlayerData();
        CompoundTag tag = buffer.readNbt();
        if (tag != null) {
            data.load(tag);
        }
        return new PlayerDataSyncPacket(data);
    }

    public static void handle(PlayerDataSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ForgePlayerData.get(Minecraft.getInstance().player).copyFrom(packet.data);
            }
        });
        context.setPacketHandled(true);
    }
}
