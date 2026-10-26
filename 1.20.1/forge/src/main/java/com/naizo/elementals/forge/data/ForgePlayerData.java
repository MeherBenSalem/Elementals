package com.naizo.elementals.forge.data;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Elementals 1.x stored its data here. It is still attached so old saves load and can be migrated. */
@Mod.EventBusSubscriber(modid = Elementals.MOD_ID)
public final class ForgePlayerData {
    public static final Capability<ElementalPlayerData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ForgePlayerData() {
    }

    public static ElementalPlayerData legacy(Entity entity) {
        return entity == null ? null : entity.getCapability(CAPABILITY).resolve().orElse(null);
    }

    @Mod.EventBusSubscriber(modid = Elementals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void register(RegisterCapabilitiesEvent event) {
            event.register(ElementalPlayerData.class);
        }
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer)) {
            event.addCapability(Compat.id(Elementals.MOD_ID, "player_data"), new Provider());
        }
    }

    private static final class Provider implements ICapabilitySerializable<Tag> {
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
            if (nbt instanceof CompoundTag tag) {
                data.load(tag);
            }
        }
    }
}
