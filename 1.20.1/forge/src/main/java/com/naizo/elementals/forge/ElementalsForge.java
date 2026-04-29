package com.naizo.elementals.forge;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.effect.TidebinderEffect;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.forge.network.ForgeNetwork;
import com.naizo.elementals.forge.platform.ForgePlatform;
import com.naizo.elementals.item.*;
import com.naizo.elementals.registry.ModEffects;
import com.naizo.elementals.registry.ModEntities;
import com.naizo.elementals.registry.ModItems;
import com.naizo.elementals.spell.ElementalLogic;
import com.naizo.elementals.spell.FireRuneDrops;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Elementals.MOD_ID)
public class ElementalsForge {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Elementals.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Elementals.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Elementals.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.item.CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Elementals.MOD_ID);

    public ElementalsForge() {
        Elementals.init(new ForgePlatform());
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        registerContent();
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        EFFECTS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::attributes);
        modBus.addListener(this::creativeTabs);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static void registerContent() {
        RegistryObject<EntityType<FireSignatureEntity>> fireSignature = ENTITIES.register("fire_signature",
                () -> EntityType.Builder.<FireSignatureEntity>of(FireSignatureEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5F, 0.5F).build("fire_signature"));
        RegistryObject<EntityType<ExplosiveFireOrbEntity>> explosiveFireOrb = ENTITIES.register("explosive_fire_orb",
                () -> EntityType.Builder.<ExplosiveFireOrbEntity>of(ExplosiveFireOrbEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5F, 0.5F).build("explosive_fire_orb"));
        RegistryObject<EntityType<FireGolemEntity>> fireGolem = ENTITIES.register("fire_golem",
                () -> EntityType.Builder.<FireGolemEntity>of(FireGolemEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3).fireImmune().sized(0.6F, 1.8F).build("fire_golem"));
        ModEntities.FIRE_SIGNATURE = fireSignature;
        ModEntities.EXPLOSIVE_FIRE_ORB = explosiveFireOrb;
        ModEntities.FIRE_GOLEM = fireGolem;
        ModEffects.TIDEBINDER_EFFECT = EFFECTS.register("tidebinder_effect", TidebinderEffect::new);
        ModItems.FIRE_ELEMENT_FOOD = ITEMS.register("fire_element_food", FireElementFoodItem::new);
        ModItems.WATER_ELEMENT_FOOD = ITEMS.register("water_element_food", WaterElementFoodItem::new);
        ModItems.FIRE_RUNE = ITEMS.register("fire_rune", FireRuneItem::new);
        ModItems.EXPLOSIVE_ORB_SPELL_BOOK = ITEMS.register("explosive_orb_spell_book", ExplosiveOrbSpellBookItem::new);
        ModItems.EMBER_FIRE_SHIELD_SPELL_BOOK = ITEMS.register("ember_fire_shield_spell_book", EmberFireShieldSpellBookItem::new);
        ModItems.INFERNO_WAVE_SPELL_BOOK = ITEMS.register("inferno_wave_spell_book", InfernoWaveSpellBookItem::new);
        ModItems.FIRE_GOLEM_SPAWN_EGG = ITEMS.register("fire_golem_spawn_egg", () -> new SpawnEggItem(ModEntities.FIRE_GOLEM.get(), 0x990000, 0xFFFF00, new Item.Properties()));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ForgeNetwork.register();
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FIRE_GOLEM.get(), FireGolemEntity.createAttributes().build());
    }

    private void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.FIRE_ELEMENT_FOOD.get());
            event.accept(ModItems.WATER_ELEMENT_FOOD.get());
        } else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.FIRE_GOLEM_SPAWN_EGG.get());
        } else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModItems.FIRE_RUNE.get());
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.EXPLOSIVE_ORB_SPELL_BOOK.get());
            event.accept(ModItems.EMBER_FIRE_SHIELD_SPELL_BOOK.get());
            event.accept(ModItems.INFERNO_WAVE_SPELL_BOOK.get());
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ElementalLogic.tickPlayer(event.player);
        }
    }

    @SubscribeEvent
    public void livingDeath(LivingDeathEvent event) {
        FireRuneDrops.onLivingDeath(event.getEntity());
    }
}
