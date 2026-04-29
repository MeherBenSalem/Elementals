package com.naizo.elementals.neoforge.network;

import com.naizo.elementals.Elementals;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SignatureAbilityPacket() implements CustomPacketPayload {
    public static final Type<SignatureAbilityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Elementals.MOD_ID, "signature_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SignatureAbilityPacket> CODEC = StreamCodec.unit(new SignatureAbilityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
