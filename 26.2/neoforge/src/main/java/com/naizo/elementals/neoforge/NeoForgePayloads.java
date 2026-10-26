package com.naizo.elementals.neoforge;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.client.ClientPowers;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.net.Net;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NeoForgePayloads {
    private NeoForgePayloads() {
    }

    public record ToServer(String json) implements CustomPacketPayload {
        public static final Type<ToServer> TYPE = new Type<>(Compat.id(Elementals.MOD_ID, "c2s"));
        public static final StreamCodec<FriendlyByteBuf, ToServer> CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeUtf(payload.json(), Net.MAX_LENGTH), buf -> new ToServer(buf.readUtf(Net.MAX_LENGTH)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ToClient(String json) implements CustomPacketPayload {
        public static final Type<ToClient> TYPE = new Type<>(Compat.id(Elementals.MOD_ID, "s2c"));
        public static final StreamCodec<FriendlyByteBuf, ToClient> CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeUtf(payload.json(), Net.MAX_LENGTH), buf -> new ToClient(buf.readUtf(Net.MAX_LENGTH)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2")
                .playToServer(ToServer.TYPE, ToServer.CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        PowerManager.handle(player, payload.json());
                    }
                }))
                .playToClient(ToClient.TYPE, ToClient.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPowers.handle(payload.json())));
    }
}
