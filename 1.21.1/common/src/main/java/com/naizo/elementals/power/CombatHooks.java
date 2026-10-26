package com.naizo.elementals.power;

import com.naizo.elementals.ability.Fx;
import com.naizo.elementals.ability.Targets;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.Keystone;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.Stat;
import com.naizo.elementals.core.StatSheet;
import com.naizo.elementals.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Damage and death hooks called by every loader. */
public final class CombatHooks {
    private static final long UNDYING_COOLDOWN = 20 * 60 * 5;

    private CombatHooks() {
    }

    /** False cancels the damage: evasion, burn immunity and fall immunity. */
    public static boolean allowDamage(LivingEntity target, DamageSource source, float amount) {
        if (!(target instanceof ServerPlayer player) || target.level().isClientSide() || Compat.isBypassingInvulnerability(source)) {
            return true;
        }
        StatSheet sheet = PowerManager.sheet(player);
        PowerProfile profile = PowerManager.profile(player);
        long time = player.level().getGameTime();
        if (Compat.isFall(source) && (sheet.has(Keystone.WINDBORNE) || profile.active("gale_fall", time))) {
            profile.timers.remove("gale_fall");
            return false;
        }
        if (Compat.isBurning(source) && sheet.has(Keystone.PYRE_HEART)) {
            return false;
        }
        Entity attacker = source.getEntity();
        double dodge = sheet.get(Stat.DODGE);
        if (attacker instanceof LivingEntity && attacker != player && dodge > 0 && player.getRandom().nextDouble() * 100 < dodge) {
            ServerLevel level = Compat.serverLevel(player);
            Fx.at(level, ParticleTypes.CLOUD, player.position().add(0, 1, 0), 8, 0.3, 0.05);
            Fx.sound(level, player.position(), "minecraft:entity.ender_dragon.flap", 0.4f, 2f);
            Compat.overlay(player, Component.translatable("message.elementals.evaded").withStyle(ChatFormatting.GRAY));
            return false;
        }
        return true;
    }

    /** Changes the amount of damage: reductions for players with stats, and melee procs for attackers. */
    public static float modifyDamage(LivingEntity target, DamageSource source, float amount) {
        if (target.level().isClientSide()) {
            return amount;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof ServerPlayer player && attacker != target && Compat.isMeleeAttack(source) && source.getDirectEntity() == player) {
            meleeProcs(player, target);
        }
        if (!(target instanceof ServerPlayer player)) {
            return amount;
        }
        StatSheet sheet = PowerManager.sheet(player);
        PowerProfile profile = PowerManager.profile(player);
        long time = player.level().getGameTime();
        if (Compat.isFall(source)) {
            amount *= (float) (1 - sheet.get(Stat.FALL_REDUCTION) / 100.0);
        }
        if (!Compat.isBypassingInvulnerability(source)) {
            amount *= (float) sheet.incomingDamageMultiplier(player.getHealth() / player.getMaxHealth());
        }
        if (attacker instanceof LivingEntity living && attacker != player && source.getDirectEntity() == attacker && !Compat.isThorns(source)) {
            double thorns = sheet.get(Stat.THORNS);
            if (thorns > 0) {
                Compat.hurt(living, Compat.thorns(player), (float) (amount * thorns / 100.0));
            }
            if (profile.active("ember_shield", time)) {
                Compat.ignite(living, (float) (PowerManager.CONFIG.param("ember_shield", "burn") * (sheet.has(Keystone.PYRE_HEART) ? 2 : 1)));
            }
            if (sheet.has(Keystone.GLACIAL_BULWARK)) {
                Compat.addEffect(living, "minecraft:slowness", 60, 1, true);
                Compat.setFrozen(living, 100);
            }
        }
        return Math.max(0, amount);
    }

    private static void meleeProcs(ServerPlayer player, LivingEntity target) {
        if (!Targets.canHit(player, target)) {
            return;
        }
        StatSheet sheet = PowerManager.sheet(player);
        double burn = sheet.get(Stat.BURN_ON_HIT);
        if (burn > 0 && player.getRandom().nextDouble() * 100 < burn) {
            Compat.ignite(target, sheet.has(Keystone.PYRE_HEART) ? 6 : 3);
        }
        double chill = sheet.get(Stat.CHILL_ON_HIT);
        if (chill > 0 && player.getRandom().nextDouble() * 100 < chill) {
            Compat.addEffect(target, "minecraft:slowness", 50, 1, true);
            Compat.setFrozen(target, 100);
            ServerLevel level = Compat.serverLevel(player);
            Fx.at(level, ParticleTypes.SNOWFLAKE, target.getBoundingBox().getCenter(), 8, 0.3, 0.02);
        }
    }

    /** False cancels the death: Verdant Rebirth and the Undying Grove keystone. */
    public static boolean allowDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayer player) || Compat.isBypassingInvulnerability(source)) {
            return true;
        }
        PowerProfile profile = PowerManager.profile(player);
        StatSheet sheet = PowerManager.sheet(player);
        long time = player.level().getGameTime();
        ServerLevel level = Compat.serverLevel(player);
        if (profile.active("rebirth", time)) {
            profile.timers.remove("rebirth");
            player.setHealth(Math.max(1, player.getMaxHealth() * 0.4f));
            Fx.at(level, ParticleTypes.TOTEM_OF_UNDYING, player.position().add(0, 1, 0), 60, 0.5, 0.5);
            Fx.sound(level, player.position(), "minecraft:item.totem.use", 1f, 1.3f);
            return false;
        }
        if (sheet.has(Keystone.UNDYING_GROVE) && profile.timer("undying_grove") <= time) {
            profile.timers.put("undying_grove", time + UNDYING_COOLDOWN);
            player.setHealth(Math.max(1, player.getMaxHealth() * 0.3f));
            for (LivingEntity enemy : Targets.enemies(player, player.position(), 6)) {
                Compat.addEffect(enemy, "minecraft:slowness", 80, 9, true);
                Fx.at(level, ParticleTypes.COMPOSTER, enemy.position().add(0, 0.5, 0), 20, 0.4, 0.1);
            }
            Fx.spiral(level, ParticleTypes.CHERRY_LEAVES, player.position(), 1.4, 3, 80, 3);
            Fx.at(level, ParticleTypes.TOTEM_OF_UNDYING, player.position().add(0, 1, 0), 40, 0.5, 0.4);
            Fx.sound(level, player.position(), "minecraft:item.totem.use", 1f, 0.8f);
            Compat.overlay(player, Component.translatable("message.elementals.undying").withStyle(ChatFormatting.GREEN));
            return false;
        }
        return true;
    }

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (entity.level().isClientSide()) {
            return;
        }
        Entity killer = source.getEntity();
        if (killer instanceof Player player) {
            PowerManager.killed(entity, player);
        }
        if (Compat.isBlaze(entity) && entity.getRandom().nextDouble() < PowerManager.CONFIG.fireRuneDropChance) {
            Compat.drop(entity, ModItems.FIRE_RUNE.get());
        }
        if (Compat.isDrownedOrGuardian(entity) && killer instanceof Player && entity.getRandom().nextDouble() < PowerManager.CONFIG.waterRuneDropChance) {
            Compat.drop(entity, ModItems.WATER_RUNE.get());
        }
    }
}
