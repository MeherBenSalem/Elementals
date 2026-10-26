package com.naizo.elementals.forge;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ClientPowers;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.net.Net;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public final class ForgeNetwork {
    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(Compat.id(Elementals.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public record ToServer(String json) {
    }

    public record ToClient(String json) {
    }

    private ForgeNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, ToServer.class, (m, buf) -> buf.writeUtf(m.json(), Net.MAX_LENGTH), buf -> new ToServer(buf.readUtf(Net.MAX_LENGTH)),
                ForgeNetwork::handleServer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, ToClient.class, (m, buf) -> buf.writeUtf(m.json(), Net.MAX_LENGTH), buf -> new ToClient(buf.readUtf(Net.MAX_LENGTH)),
                ForgeNetwork::handleClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static void handleServer(ToServer message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player != null) {
                PowerManager.handle(player, message.json());
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleClient(ToClient message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientPowers.handle(message.json()));
        context.setPacketHandled(true);
    }

    public static void sendToPlayer(ServerPlayer player, String json) {
        if (CHANNEL.isRemotePresent(player.connection.connection)) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ToClient(json));
        }
    }

    public static void sendToServer(String json) {
        CHANNEL.sendToServer(new ToServer(json));
    }

    public static FriendlyByteBuf unused() {
        return null;
    }
}
