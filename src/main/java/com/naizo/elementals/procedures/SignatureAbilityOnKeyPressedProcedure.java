package com.naizo.elementals.procedures;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkDirection;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Iterator;

import com.naizo.elementals.network.ElementalsModVariables;
import com.naizo.elementals.init.ElementalsModMobEffects;
import com.naizo.elementals.init.ElementalsModEntities;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.ElementalsMod;

public class SignatureAbilityOnKeyPressedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity.getPersistentData().getDouble("el_cdr") == 0) {
			if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).element == 1) {
				{
					Entity _shootFrom = entity;
					Level projectileLevel = _shootFrom.level();
					if (!projectileLevel.isClientSide()) {
						Projectile _entityToSpawn = new Object() {
							public Projectile getArrow(Level level, Entity shooter, float damage, int knockback) {
								AbstractArrow entityToSpawn = new FireSignatureEntity(ElementalsModEntities.FIRE_SIGNATURE.get(), level);
								entityToSpawn.setOwner(shooter);
								entityToSpawn.setBaseDamage(damage);
								entityToSpawn.setKnockback(knockback);
								entityToSpawn.setSilent(true);
								entityToSpawn.setSecondsOnFire(100);
								return entityToSpawn;
							}
						}.getArrow(projectileLevel, entity, 4, 1);
						_entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
						_entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 3, 0);
						projectileLevel.addFreshEntity(_entityToSpawn);
					}
				}
				if (world.isClientSide()) {
					SetupAnimationsProcedure.setAnimationClientside((Player) entity, "cast", false);
				}
				if (!world.isClientSide()) {
					if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
						List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
						synchronized (connections) {
							Iterator<Connection> iterator = connections.iterator();
							while (iterator.hasNext()) {
								Connection connection = iterator.next();
								if (!connection.isConnecting() && connection.isConnected())
									ElementalsMod.PACKET_HANDLER.sendTo(new SetupAnimationsProcedure.ElementalsModAnimationMessage(Component.literal("cast"), entity.getId(), false), connection, NetworkDirection.PLAY_TO_CLIENT);
							}
						}
					}
				}
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.blaze.shoot")), SoundSource.NEUTRAL, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.blaze.shoot")), SoundSource.NEUTRAL, 1, 1, false);
					}
				}
				entity.getPersistentData().putDouble("el_cdr", 100);
				HandleElementalXpLevelUpProcedure.execute(world, x, y, z, entity, 5);
			} else if ((entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).element == 2) {
				entity.clearFire();
				if (world.isClientSide()) {
					SetupAnimationsProcedure.setAnimationClientside((Player) entity, "fortify", false);
				}
				if (!world.isClientSide()) {
					if (entity instanceof Player && world instanceof ServerLevel srvLvl_) {
						List<Connection> connections = srvLvl_.getServer().getConnection().getConnections();
						synchronized (connections) {
							Iterator<Connection> iterator = connections.iterator();
							while (iterator.hasNext()) {
								Connection connection = iterator.next();
								if (!connection.isConnecting() && connection.isConnected())
									ElementalsMod.PACKET_HANDLER.sendTo(new SetupAnimationsProcedure.ElementalsModAnimationMessage(Component.literal("fortify"), entity.getId(), false), connection, NetworkDirection.PLAY_TO_CLIENT);
							}
						}
					}
				}
				if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
					_entity.addEffect(new MobEffectInstance(ElementalsModMobEffects.TIDEBINDER_EFFECT.get(), 250,
							(int) (entity.getCapability(ElementalsModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new ElementalsModVariables.PlayerVariables())).elementalLevel, false, false));
				if (world instanceof Level _level) {
					if (!_level.isClientSide()) {
						_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.use")), SoundSource.NEUTRAL, 1, 1);
					} else {
						_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.anvil.use")), SoundSource.NEUTRAL, 1, 1, false);
					}
				}
				entity.getPersistentData().putDouble("el_cdr", 300);
				HandleElementalXpLevelUpProcedure.execute(world, x, y, z, entity, 5);
			}
		}
	}
}
