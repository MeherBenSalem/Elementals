package com.naizo.elementals.neoforge;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.effect.TidebinderEffect;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.item.*;
import com.naizo.elementals.neoforge.client.ElementalsNeoForgeClient;
import com.naizo.elementals.neoforge.data.NeoForgePlayerData;
import com.naizo.elementals.neoforge.network.NeoForgeNetwork;
import com.naizo.elementals.neoforge.platform.NeoForgePlatform;
import com.naizo.elementals.registry.ModEffects;
import com.naizo.elementals.registry.ModEntities;
import com.naizo.elementals.registry.ModItems;
import com.naizo.elementals.spell.ElementalLogic;
import com.naizo.elementals.spell.FireRuneDrops;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Elementals.MOD_ID)
public class ElementalsNeoForge {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Elementals.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Elementals.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Elementals.MOD_ID);

    public ElementalsNeoForge(IEventBus modBus) {
        Elementals.init(new NeoForgePlatform());
        registerContent();
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        EFFECTS.register(modBus);
        ElementalsNeoForgeTabs.REGISTRY.register(modBus);
        NeoForgePlayerData.ATTACHMENTS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::attributes);
        modBus.addListener(NeoForgeNetwork::register);
        modBus.addListener(ElementalsNeoForgeClient::registerKeys);
        NeoForge.EVENT_BUS.register(this);
    }

    private static void registerContent() {
        DeferredHolder<EntityType<?>, EntityType<FireSignatureEntity>> fireSignature = registerEntity("fire_signature",
                EntityType.Builder.<FireSignatureEntity>of(FireSignatureEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5F, 0.5F));
        DeferredHolder<EntityType<?>, EntityType<ExplosiveFireOrbEntity>> explosiveFireOrb = registerEntity("explosive_fire_orb",
                EntityType.Builder.<ExplosiveFireOrbEntity>of(ExplosiveFireOrbEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5F, 0.5F));
        DeferredHolder<EntityType<?>, EntityType<FireGolemEntity>> fireGolem = registerEntity("fire_golem",
                EntityType.Builder.<FireGolemEntity>of(FireGolemEntity::new, MobCategory.MONSTER).clientTrackingRange(64).updateInterval(3).fireImmune().sized(0.6F, 1.8F));
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
        ModItems.FIRE_GOLEM_SPAWN_EGG = ITEMS.register("fire_golem_spawn_egg", () -> new Item(new Item.Properties().spawnEgg(ModEntities.FIRE_GOLEM.get())));
    }

    private static <T extends net.minecraft.world.entity.Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Elementals.MOD_ID, name));
        return ENTITIES.register(name, () -> builder.build(key));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FIRE_GOLEM.get(), FireGolemEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Post event) {
        ElementalLogic.tickPlayer(event.getEntity());
    }

    @SubscribeEvent
    public void livingDeath(LivingDeathEvent event) {
        FireRuneDrops.onLivingDeath(event.getEntity());
    }
}
