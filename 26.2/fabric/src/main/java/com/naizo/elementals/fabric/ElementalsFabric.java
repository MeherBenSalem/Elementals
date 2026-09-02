package com.naizo.elementals.fabric;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.fabric.init.ElementalsFabricTabs;
import com.naizo.elementals.effect.TidebinderEffect;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.fabric.platform.FabricPlatform;
import com.naizo.elementals.item.*;
import com.naizo.elementals.registry.ModEffects;
import com.naizo.elementals.registry.ModEntities;
import com.naizo.elementals.registry.ModItems;
import com.naizo.elementals.spell.ElementalLogic;
import com.naizo.elementals.spell.FireRuneDrops;
import com.naizo.elementals.spell.FireSpells;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ElementalsFabric implements ModInitializer {
    public record SignaturePayload() implements CustomPacketPayload {
        public static final Type<SignaturePayload> TYPE = new Type<>(id("signature_ability"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SignaturePayload> CODEC = StreamCodec.unit(new SignaturePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @Override
    public void onInitialize() {
        FabricPlatform platform = new FabricPlatform();
        Elementals.init(platform);
        registerContent();
        ElementalsFabricTabs.register();
        FabricDefaultAttributeRegistry.register(ModEntities.FIRE_GOLEM.get(), FireGolemEntity.createAttributes());
        ServerTickEvents.START_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(ElementalLogic::tickPlayer));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> FireRuneDrops.onLivingDeath(entity));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Elementals.PLATFORM.syncPlayerData(handler.player));
        PayloadTypeRegistry.serverboundPlay().register(SignaturePayload.TYPE, SignaturePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SignaturePayload.TYPE, (payload, context) -> context.server().execute(() -> FireSpells.castSignature(context.player().level(), context.player())));
    }

    private static void registerContent() {
        ModEntities.FIRE_SIGNATURE = entity("fire_signature", EntityType.Builder.<FireSignatureEntity>of(FireSignatureEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5F, 0.5F));
        ModEntities.EXPLOSIVE_FIRE_ORB = entity("explosive_fire_orb", EntityType.Builder.<ExplosiveFireOrbEntity>of(ExplosiveFireOrbEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5F, 0.5F));
        ModEntities.FIRE_GOLEM = entity("fire_golem", EntityType.Builder.<FireGolemEntity>of(FireGolemEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3).fireImmune().sized(0.6F, 1.8F));
        ModEffects.TIDEBINDER_EFFECT = register("tidebinder_effect", () -> Registry.register(BuiltInRegistries.MOB_EFFECT, id("tidebinder_effect"), new TidebinderEffect()));
        ModItems.FIRE_ELEMENT_FOOD = item("fire_element_food", FireElementFoodItem::new);
        ModItems.WATER_ELEMENT_FOOD = item("water_element_food", WaterElementFoodItem::new);
        ModItems.FIRE_RUNE = item("fire_rune", FireRuneItem::new);
        ModItems.EXPLOSIVE_ORB_SPELL_BOOK = item("explosive_orb_spell_book", ExplosiveOrbSpellBookItem::new);
        ModItems.EMBER_FIRE_SHIELD_SPELL_BOOK = item("ember_fire_shield_spell_book", EmberFireShieldSpellBookItem::new);
        ModItems.INFERNO_WAVE_SPELL_BOOK = item("inferno_wave_spell_book", InfernoWaveSpellBookItem::new);
        ModItems.FIRE_GOLEM_SPAWN_EGG = item("fire_golem_spawn_egg", () -> new Item(new Item.Properties().spawnEgg(ModEntities.FIRE_GOLEM.get())));
    }

    private static <T extends Item> Supplier<T> item(String name, Supplier<T> factory) {
        return register(name, () -> Registry.register(BuiltInRegistries.ITEM, id(name), factory.get()));
    }

    private static <T extends net.minecraft.world.entity.Entity> Supplier<EntityType<T>> entity(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id(name));
        return register(name, () -> Registry.register(BuiltInRegistries.ENTITY_TYPE, id(name), builder.build(key)));
    }

    private static <T> Supplier<T> register(String name, Supplier<T> supplier) {
        T value = supplier.get();
        return () -> value;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Elementals.MOD_ID, path);
    }
}
