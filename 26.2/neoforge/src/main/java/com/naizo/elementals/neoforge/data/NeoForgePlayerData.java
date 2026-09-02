package com.naizo.elementals.neoforge.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.naizo.elementals.Elementals;
import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.neoforge.network.PlayerDataSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class NeoForgePlayerData {
    private static final com.mojang.serialization.MapCodec<ElementalPlayerData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("element", 0.0).forGetter(data -> data.element),
            Codec.DOUBLE.optionalFieldOf("elementalLevel", 0.0).forGetter(data -> data.elementalLevel),
            Codec.DOUBLE.optionalFieldOf("elementalExp", 0.0).forGetter(data -> data.elementalExp),
            Codec.DOUBLE.optionalFieldOf("cooldown", 0.0).forGetter(data -> data.cooldown)
    ).apply(instance, (element, elementalLevel, elementalExp, cooldown) -> {
        ElementalPlayerData data = new ElementalPlayerData();
        data.element = element;
        data.elementalLevel = elementalLevel;
        data.elementalExp = elementalExp;
        data.cooldown = cooldown;
        return data;
    }));

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Elementals.MOD_ID);
    public static final Supplier<AttachmentType<ElementalPlayerData>> PLAYER_DATA = ATTACHMENTS.register("player_data",
            () -> AttachmentType.builder(ElementalPlayerData::new).serialize(CODEC).copyOnDeath().build());

    private NeoForgePlayerData() {
    }

    public static ElementalPlayerData get(Entity entity) {
        return entity == null ? new ElementalPlayerData() : entity.getData(PLAYER_DATA.get());
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new PlayerDataSyncPacket(get(player)));
    }
}
