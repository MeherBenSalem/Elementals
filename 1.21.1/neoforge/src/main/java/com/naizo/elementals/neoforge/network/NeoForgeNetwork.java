package com.naizo.elementals.neoforge.network;

import com.naizo.elementals.neoforge.data.NeoForgePlayerData;
import com.naizo.elementals.spell.FireSpells;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NeoForgeNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private NeoForgeNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                .playToServer(SignatureAbilityPacket.TYPE, SignatureAbilityPacket.CODEC,
                        (packet, context) -> context.enqueueWork(() -> FireSpells.castSignature(context.player().level(), context.player())))
                .playToClient(PlayerDataSyncPacket.TYPE, PlayerDataSyncPacket.CODEC,
                        (packet, context) -> context.enqueueWork(() -> {
                            if (Minecraft.getInstance().player != null) {
                                NeoForgePlayerData.get(Minecraft.getInstance().player).copyFrom(packet.data());
                            }
                        }));
    }
}
