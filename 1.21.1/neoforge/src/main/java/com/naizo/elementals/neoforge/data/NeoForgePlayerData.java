package com.naizo.elementals.neoforge.data;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Elementals 1.x stored its data here. It is still registered so old saves load and can be migrated. */
public final class NeoForgePlayerData {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Elementals.MOD_ID);
    public static final Supplier<AttachmentType<ElementalPlayerData>> PLAYER_DATA = ATTACHMENTS.register("player_data",
            () -> AttachmentType.builder(ElementalPlayerData::new).serialize(new Serializer()).copyOnDeath().build());

    private NeoForgePlayerData() {
    }

    public static ElementalPlayerData legacy(Entity entity) {
        return entity != null && entity.hasData(PLAYER_DATA.get()) ? entity.getData(PLAYER_DATA.get()) : null;
    }

    private static final class Serializer implements IAttachmentSerializer<CompoundTag, ElementalPlayerData> {
        @Override
        public ElementalPlayerData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            ElementalPlayerData data = new ElementalPlayerData();
            data.load(tag);
            return data;
        }

        @Override
        public CompoundTag write(ElementalPlayerData data, HolderLookup.Provider provider) {
            return data.save();
        }
    }
}
