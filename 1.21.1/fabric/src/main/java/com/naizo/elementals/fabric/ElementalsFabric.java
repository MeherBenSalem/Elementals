package com.naizo.elementals.fabric;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.command.PowersCommands;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.effect.TidebinderEffect;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.power.CombatHooks;
import com.naizo.elementals.power.PowerManager;
import com.naizo.elementals.registry.ModContent;
import com.naizo.elementals.registry.ModEffects;
import com.naizo.elementals.registry.ModEntities;
import com.naizo.elementals.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Supplier;

public class ElementalsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Elementals.init(new FabricPlatform());
        registerContent();
        FabricDefaultAttributeRegistry.register(ModEntities.FIRE_GOLEM.get(), FireGolemEntity.createAttributes());
        BiomeModifications.addSpawn(BiomeSelectors.includeByKey(Biomes.NETHER_WASTES, Biomes.CRIMSON_FOREST, Biomes.BASALT_DELTAS),
                MobCategory.MONSTER, ModEntities.FIRE_GOLEM.get(), 12, 1, 2);

        PayloadTypeRegistry.playC2S().register(FabricPayloads.ToServer.TYPE, FabricPayloads.ToServer.CODEC);
        PayloadTypeRegistry.playS2C().register(FabricPayloads.ToClient.TYPE, FabricPayloads.ToClient.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FabricPayloads.ToServer.TYPE,
                (payload, context) -> context.server().execute(() -> PowerManager.handle(context.player(), payload.json())));

        ServerLifecycleEvents.SERVER_STARTED.register(PowerManager::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(PowerManager::serverStopping);
        ServerTickEvents.END_SERVER_TICK.register(PowerManager::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PowerManager.join(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PowerManager.leave(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PowerManager.respawned(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> PowerManager.respawned(player));
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(CombatHooks::allowDamage);
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> CombatHooks.allowDeath(entity, source));
        ServerLivingEntityEvents.AFTER_DEATH.register(CombatHooks::onDeath);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> PowersCommands.register(dispatcher));
    }

    private static void registerContent() {
        ModEntities.FIRE_SIGNATURE = entity("fire_signature", EntityType.Builder.<FireSignatureEntity>of(FireSignatureEntity::new, MobCategory.MISC)
                .clientTrackingRange(8).updateInterval(1).sized(0.4F, 0.4F));
        ModEntities.EXPLOSIVE_FIRE_ORB = entity("explosive_fire_orb", EntityType.Builder.<ExplosiveFireOrbEntity>of(ExplosiveFireOrbEntity::new, MobCategory.MISC)
                .clientTrackingRange(8).updateInterval(1).sized(0.6F, 0.6F));
        ModEntities.FIRE_GOLEM = entity("fire_golem", FabricEntityType.Builder.createMob(FireGolemEntity::new, MobCategory.MONSTER,
                        mob -> mob.spawnRestriction(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FireGolemEntity::checkSpawn))
                .clientTrackingRange(10).fireImmune().sized(1.4F, 2.7F));
        TidebinderEffect effect = Registry.register(BuiltInRegistries.MOB_EFFECT, Compat.id(Elementals.MOD_ID, "tidebinder_effect"), new TidebinderEffect());
        ModEffects.TIDEBINDER_EFFECT = () -> effect;
        ModContent.items(ElementalsFabric::item, p -> new SpawnEggItem(ModEntities.FIRE_GOLEM.get(), 0x2A1A14, 0xFF7A2E, p));
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.elementals"))
                .icon(() -> new ItemStack(ModItems.REMNANT_TOME.get()))
                .displayItems((parameters, output) -> {
                    for (String name : ModItems.NAMES) {
                        output.accept(BuiltInRegistries.ITEM.get(Compat.id(Elementals.MOD_ID, name)));
                    }
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Compat.id(Elementals.MOD_ID, "main"), tab);
    }

    private static Supplier<Item> item(String name, java.util.function.Function<Item.Properties, Item> factory) {
        Item item = Registry.register(BuiltInRegistries.ITEM, Compat.id(Elementals.MOD_ID, name), factory.apply(new Item.Properties()));
        return () -> item;
    }

    private static <T extends net.minecraft.world.entity.Entity> Supplier<EntityType<T>> entity(String name, EntityType.Builder<T> builder) {
        EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, Compat.id(Elementals.MOD_ID, name), builder.build(name));
        return () -> type;
    }
}
