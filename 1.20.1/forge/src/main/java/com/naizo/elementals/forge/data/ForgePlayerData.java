package com.naizo.elementals.forge.data;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.forge.network.ForgeNetwork;
import com.naizo.elementals.forge.network.PlayerDataSyncPacket;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Elementals.MOD_ID)
public final class ForgePlayerData {
    public static final Capability<ElementalPlayerData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ForgePlayerData() {
    }

    public static ElementalPlayerData get(Entity entity) {
        return entity == null ? new ElementalPlayerData() : entity.getCapability(CAPABILITY).orElseGet(ElementalPlayerData::new);
    }

    public static void sync(ServerPlayer player) {
        ForgeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PlayerDataSyncPacket(get(player)));
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ElementalPlayerData.class);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer)) {
            event.addCapability(new ResourceLocation(Elementals.MOD_ID, "player_data"), new Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        event.getOriginal().revive();
        get(event.getEntity()).copyFrom(get(event.getOriginal()));
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    private static class Provider implements ICapabilitySerializable<Tag> {
        private final ElementalPlayerData data = new ElementalPlayerData();
        private final LazyOptional<ElementalPlayerData> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == CAPABILITY ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public Tag serializeNBT() {
            return data.save();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            data.load((CompoundTag) nbt);
        }
    }
}
