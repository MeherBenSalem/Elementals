
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package com.naizo.elementals.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.entity.FireGolemEntity;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.ElementalsMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ElementalsModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ElementalsMod.MODID);
	public static final RegistryObject<EntityType<FireSignatureEntity>> FIRE_SIGNATURE = register("fire_signature",
			EntityType.Builder.<FireSignatureEntity>of(FireSignatureEntity::new, MobCategory.MISC).setCustomClientFactory(FireSignatureEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<FireGolemEntity>> FIRE_GOLEM = register("fire_golem", EntityType.Builder.<FireGolemEntity>of(FireGolemEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64)
			.setUpdateInterval(3).setCustomClientFactory(FireGolemEntity::new).fireImmune().sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<ExplosiveFireOrbEntity>> EXPLOSIVE_FIRE_ORB = register("explosive_fire_orb", EntityType.Builder.<ExplosiveFireOrbEntity>of(ExplosiveFireOrbEntity::new, MobCategory.MISC)
			.setCustomClientFactory(ExplosiveFireOrbEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			FireGolemEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(FIRE_GOLEM.get(), FireGolemEntity.createAttributes().build());
	}
}
