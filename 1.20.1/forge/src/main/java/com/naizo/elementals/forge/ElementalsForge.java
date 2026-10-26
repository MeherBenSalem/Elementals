package com.naizo.elementals.forge;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

@Mod(Elementals.MOD_ID)
public class ElementalsForge {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Elementals.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Elementals.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Elementals.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Elementals.MOD_ID);

    public ElementalsForge() {
        Elementals.init(new ForgePlatform());
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        registerContent();
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        EFFECTS.register(modBus);
        TABS.register(modBus);
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(ForgeNetwork::register));
        modBus.addListener((EntityAttributeCreationEvent event) -> event.put(ModEntities.FIRE_GOLEM.get(), FireGolemEntity.createAttributes().build()));
        modBus.addListener((SpawnPlacementRegisterEvent event) -> event.register(ModEntities.FIRE_GOLEM.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FireGolemEntity::checkSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE));

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener((ServerStartedEvent event) -> PowerManager.serverStarted(event.getServer()));
        bus.addListener((ServerStoppingEvent event) -> PowerManager.serverStopping(event.getServer()));
        bus.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                PowerManager.tick(event.getServer());
            }
        });
        bus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.join(player);
            }
        });
        bus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.leave(player);
            }
        });
        bus.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.respawned(player);
            }
        });
        bus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PowerManager.respawned(player);
            }
        });
        bus.addListener((RegisterCommandsEvent event) -> PowersCommands.register(event.getDispatcher()));
        bus.addListener((LivingAttackEvent event) -> {
            if (!CombatHooks.allowDamage(event.getEntity(), event.getSource(), event.getAmount())) {
                event.setCanceled(true);
            }
        });
        bus.addListener((LivingHurtEvent event) -> event.setAmount(CombatHooks.modifyDamage(event.getEntity(), event.getSource(), event.getAmount())));
        bus.addListener((LivingDeathEvent event) -> {
            if (!CombatHooks.allowDeath(event.getEntity(), event.getSource())) {
                event.setCanceled(true);
                return;
            }
            CombatHooks.onDeath(event.getEntity(), event.getSource());
        });
    }

    private static void registerContent() {
        RegistryObject<EntityType<FireSignatureEntity>> fireSignature = ENTITIES.register("fire_signature",
                () -> EntityType.Builder.<FireSignatureEntity>of(FireSignatureEntity::new, MobCategory.MISC).clientTrackingRange(8).updateInterval(1).sized(0.4F, 0.4F).build("fire_signature"));
        RegistryObject<EntityType<ExplosiveFireOrbEntity>> orb = ENTITIES.register("explosive_fire_orb",
                () -> EntityType.Builder.<ExplosiveFireOrbEntity>of(ExplosiveFireOrbEntity::new, MobCategory.MISC).clientTrackingRange(8).updateInterval(1).sized(0.6F, 0.6F).build("explosive_fire_orb"));
        RegistryObject<EntityType<FireGolemEntity>> golem = ENTITIES.register("fire_golem",
                () -> EntityType.Builder.<FireGolemEntity>of(FireGolemEntity::new, MobCategory.MONSTER).clientTrackingRange(10).fireImmune().sized(1.4F, 2.7F).build("fire_golem"));
        ModEntities.FIRE_SIGNATURE = fireSignature;
        ModEntities.EXPLOSIVE_FIRE_ORB = orb;
        ModEntities.FIRE_GOLEM = golem;
        ModEffects.TIDEBINDER_EFFECT = EFFECTS.register("tidebinder_effect", TidebinderEffect::new);
        ModContent.items(ElementalsForge::item, p -> new ForgeSpawnEggItem(golem, 0x2A1A14, 0xFF7A2E, p));
        TABS.register("main", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.elementals"))
                .icon(() -> new ItemStack(ModItems.REMNANT_TOME.get()))
                .displayItems((parameters, output) -> {
                    for (String name : ModItems.NAMES) {
                        output.accept(BuiltInRegistries.ITEM.get(Compat.id(Elementals.MOD_ID, name)));
                    }
                })
                .build());
    }

    private static Supplier<Item> item(String name, Function<Item.Properties, Item> factory) {
        return ITEMS.register(name, () -> factory.apply(new Item.Properties()));
    }
}
