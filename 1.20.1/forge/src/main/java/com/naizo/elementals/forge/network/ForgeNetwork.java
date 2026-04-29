package com.naizo.elementals.forge.network;

import com.naizo.elementals.Elementals;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Elementals.MOD_ID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int id;

    private ForgeNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(id++, SignatureAbilityPacket.class, SignatureAbilityPacket::encode, SignatureAbilityPacket::decode, SignatureAbilityPacket::handle);
        CHANNEL.registerMessage(id++, PlayerDataSyncPacket.class, PlayerDataSyncPacket::encode, PlayerDataSyncPacket::decode, PlayerDataSyncPacket::handle);
    }
}
