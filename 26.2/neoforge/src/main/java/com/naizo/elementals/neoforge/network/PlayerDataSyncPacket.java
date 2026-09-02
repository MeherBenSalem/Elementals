package com.naizo.elementals.neoforge.network;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PlayerDataSyncPacket(ElementalPlayerData data) implements CustomPacketPayload {
    public static final Type<PlayerDataSyncPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Elementals.MOD_ID, "player_data_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerDataSyncPacket> CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeNbt(packet.data.save()),
            buffer -> {
                ElementalPlayerData data = new ElementalPlayerData();
                CompoundTag tag = buffer.readNbt();
                if (tag != null) {
                    data.load(tag);
                }
                return new PlayerDataSyncPacket(data);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
