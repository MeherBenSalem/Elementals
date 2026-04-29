package com.naizo.elementals.forge.network;

import com.naizo.elementals.spell.FireSpells;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SignatureAbilityPacket {
    public static void encode(SignatureAbilityPacket packet, FriendlyByteBuf buffer) {
    }

    public static SignatureAbilityPacket decode(FriendlyByteBuf buffer) {
        return new SignatureAbilityPacket();
    }

    public static void handle(SignatureAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                FireSpells.castSignature(context.getSender().level(), context.getSender());
            }
        });
        context.setPacketHandled(true);
    }
}
