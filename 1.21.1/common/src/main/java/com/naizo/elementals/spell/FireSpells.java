package com.naizo.elementals.spell;

import com.naizo.elementals.config.ElementalsConfig;
import com.naizo.elementals.data.ElementalPlayerData;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import com.naizo.elementals.registry.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public final class FireSpells {
    private FireSpells() {
    }

    public static void castSignature(Level level, Player player) {
        if (player == null || !level.hasChunkAt(player.blockPosition())) {
            return;
        }
        ElementalPlayerData data = ElementalProgression.data(player);
        if (data.element == 1) {
            if (!level.isClientSide()) {
                FireSignatureEntity.shoot(level, player, player.getRandom());
                data.cooldown = 100;
                ElementalProgression.addExperience(level, player, 5);
            }
        } else if (data.element == 2) {
            if (!level.isClientSide()) {
                player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.TIDEBINDER_EFFECT.get()), 100, (int) data.elementalLevel, false, false));
                data.cooldown = 300;
                level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.NEUTRAL, 1, 1);
                ElementalProgression.addExperience(level, player, 5);
            }
        }
    }

    public static void castExplosiveOrb(Level level, Player player, ItemStack stack) {
        ElementalPlayerData data = ElementalProgression.data(player);
        if (data.element != 1) {
            deny(player, "Requires Fire element");
            return;
        }
        if (!level.isClientSide()) {
            ExplosiveFireOrbEntity.shoot(level, player, player.getRandom());
            player.getCooldowns().addCooldown(stack.getItem(), (int) ElementalsConfig.fire("explosive_orb_cdr"));
            ElementalProgression.addExperience(level, player, 10);
        }
    }

    public static void castEmberShield(Level level, Player player, ItemStack stack) {
        ElementalPlayerData data = ElementalProgression.data(player);
        if (data.element != 1 || data.elementalLevel < 1) {
            deny(player, "Requires Fire level 1");
            return;
        }
        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, (int) ElementalsConfig.fire("ember_shield_absorption_lvl"), false, false));
            player.getCooldowns().addCooldown(stack.getItem(), (int) ElementalsConfig.fire("ember_shield_cdr"));
            level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 1, 1);
            ElementalProgression.addExperience(level, player, 10);
        }
    }

    public static void castInfernoWave(Level level, Player player, ItemStack stack) {
        ElementalPlayerData data = ElementalProgression.data(player);
        if (data.element != 1 || data.elementalLevel < 2) {
            deny(player, "Requires Fire level 2");
            return;
        }
        if (!level.isClientSide()) {
            AABB area = player.getBoundingBox().inflate(6);
            level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player).stream()
                    .sorted(Comparator.comparingDouble(player::distanceToSqr))
                    .forEach(entity -> entity.hurt(player.damageSources().playerAttack(player), (float) ElementalsConfig.fire("inferno_wave_damage")));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 80, 4, 1, 4, 0.05);
            }
            player.getCooldowns().addCooldown(stack.getItem(), (int) ElementalsConfig.fire("inferno_wave_cdr"));
            level.playSound(null, BlockPos.containing(player.position()), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 1, 1);
            ElementalProgression.addExperience(level, player, 15);
        }
    }

    public static void burnOnHit(Entity target, Entity owner) {
        if (target == null || owner == null) {
            return;
        }
        double level = ElementalProgression.data(owner).elementalLevel;
        target.igniteForSeconds((float) ElementalsConfig.fire(level <= 1 ? "burn_timer_signature_level_0" : "burn_timer_signature_level_1"));
    }

    public static void projectileFlame(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    public static void explode(Level level, double x, double y, double z) {
        if (!level.isClientSide()) {
            level.explode(null, x, y, z, (float) ElementalsConfig.fire("explosive_orb_explosion_power"), Level.ExplosionInteraction.NONE);
        }
    }

    private static void deny(Player player, String message) {
        if (!player.level().isClientSide()) {
            player.displayClientMessage(Component.literal(message), true);
        }
    }
}
