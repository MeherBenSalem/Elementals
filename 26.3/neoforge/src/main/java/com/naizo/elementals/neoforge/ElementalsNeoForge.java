package com.naizo.elementals.neoforge;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.command.PowersCommands;
import com.naizo.elementals.effect.TidebinderEffect;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.neoforge.data.NeoForgePlayerData;
import com.naizo.elementals.power.CombatHooks;
import com.naizo.elementals.power.PowerManager;
import com.naizo.elementals.registry.ModContent;
import com.naizo.elementals.registry.ModEffects;
import com.naizo.elementals.registry.ModEntities;
import com.naizo.elementals.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(Elementals.MOD_ID)
public class ElementalsNeoForge {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Elementals.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Elementals.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Elementals.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Elementals.MOD_ID);

    public ElementalsNeoForge(IEventBus modBus) {
        Elementals.init(new NeoForgePlatform());
        registerContent();
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        EFFECTS.register(modBus);
        TABS.register(modBus);
        NeoForgePlayerData.ATTACHMENTS.register(modBus);
        modBus.addListener(NeoForgePayloads::register);
        modBus.addListener(EntityAttributeCreationEvent.class, event -> event.put(ModEntities.FIRE_GOLEM.get(), FireGolemEntity.createAttributes().build()));
        modBus.addListener(RegisterSpawnPlacementsEvent.class, event -> event.register(ModEntities.FIRE_GOLEM.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FireGolemEntity::checkSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE));

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(ServerStartedEvent.class, event -> PowerManager.serverStarted(event.getServer()));
        bus.addListener(ServerStoppingEvent.class, event -> PowerManager.serverStopping(event.getServer()));
        bus.addListener(ServerTickEvent.Post.class, event -> PowerManager.tick(event.getServer()));
        bus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.join(player);
            }
        });
        bus.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.leave(player);
            }
        });
        bus.addListener(PlayerEvent.PlayerRespawnEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.respawned(player);
            }
        });
        bus.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.respawned(player);
            }
        });
        bus.addListener(RegisterCommandsEvent.class, event -> PowersCommands.register(event.getDispatcher()));
        bus.addListener(LivingIncomingDamageEvent.class, event -> {
            if (!CombatHooks.allowDamage(event.getEntity(), event.getSource(), event.getAmount())) {
                event.setCanceled(true);
                return;
            }
            event.setAmount(CombatHooks.modifyDamage(event.getEntity(), event.getSource(), event.getAmount()));
        });
        bus.addListener(LivingDeathEvent.class, event -> {
            if (!CombatHooks.allowDeath(event.getEntity(), event.getSource())) {
                event.setCanceled(true);
                return;
            }
            CombatHooks.onDeath(event.getEntity(), event.getSource());
        });
    }

    private static void registerContent() {
        DeferredHolder<EntityType<?>, EntityType<FireSignatureEntity>> fireSignature = ENTITIES.register("fire_signature",
                () -> EntityType.Builder.<FireSignatureEntity>of(FireSignatureEntity::new, MobCategory.MISC).clientTrackingRange(8).updateInterval(1).sized(0.4F, 0.4F).build(key("fire_signature")));
        DeferredHolder<EntityType<?>, EntityType<ExplosiveFireOrbEntity>> orb = ENTITIES.register("explosive_fire_orb",
                () -> EntityType.Builder.<ExplosiveFireOrbEntity>of(ExplosiveFireOrbEntity::new, MobCategory.MISC).clientTrackingRange(8).updateInterval(1).sized(0.6F, 0.6F).build(key("explosive_fire_orb")));
        DeferredHolder<EntityType<?>, EntityType<FireGolemEntity>> golem = ENTITIES.register("fire_golem",
                () -> EntityType.Builder.<FireGolemEntity>of(FireGolemEntity::new, MobCategory.MONSTER).clientTrackingRange(10).fireImmune().sized(1.4F, 2.7F).build(key("fire_golem")));
        ModEntities.FIRE_SIGNATURE = fireSignature;
        ModEntities.EXPLOSIVE_FIRE_ORB = orb;
        ModEntities.FIRE_GOLEM = golem;
        ModEffects.TIDEBINDER_EFFECT = EFFECTS.register("tidebinder_effect", TidebinderEffect::new);
        ModContent.items(ElementalsNeoForge::item, p -> new Item(p.spawnEgg(golem.get())));
        TABS.register("main", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.elementals"))
                .icon(() -> new ItemStack(ModItems.REMNANT_TOME.get()))
                .displayItems((parameters, output) -> {
                    for (String name : ModItems.NAMES) {
                        output.accept(BuiltInRegistries.ITEM.getValue(com.naizo.elementals.compat.Compat.id(Elementals.MOD_ID, name)));
                    }
                })
                .build());
    }

    private static Supplier<Item> item(String name, java.util.function.Function<Item.Properties, Item> factory) {
        return ITEMS.registerItem(name, factory);
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, com.naizo.elementals.compat.Compat.id(Elementals.MOD_ID, name));
    }
}
