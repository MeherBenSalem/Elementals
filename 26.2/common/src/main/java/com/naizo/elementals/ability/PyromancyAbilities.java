package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.AbilityCatalog;
import com.naizo.elementals.entity.ExplosiveFireOrbEntity;
import com.naizo.elementals.entity.FireSignatureEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PyromancyAbilities {
    private PyromancyAbilities() {
    }

    static void register(Map<String, Ability> registry) {
        registry.put("ember_bolt", ctx -> {
            FireSignatureEntity.shoot(ctx.player, (float) ctx.p("speed"), ctx.empowered());
            Fx.sound(ctx.level, ctx.eye(), "minecraft:entity.blaze.shoot", 0.8f, 1.2f);
            return true;
        });
        registry.put("explosive_orb", ctx -> {
            ExplosiveFireOrbEntity.shoot(ctx.player, (float) ctx.p("speed"), ctx.empowered());
            Fx.sound(ctx.level, ctx.eye(), "minecraft:entity.ghast.shoot", 0.7f, 0.9f);
            return true;
        });
        registry.put("ember_shield", ctx -> {
            int ticks = ctx.ticks("duration");
            int amplifier = Math.max(0, (int) Math.ceil(ctx.heal(ctx.p("absorption")) / 4.0) - 1);
            Compat.addEffect(ctx.player, "minecraft:absorption", ticks, amplifier, false);
            ctx.profile.timers.put("ember_shield", ctx.time + ticks);
            Fx.sphere(ctx.level, ParticleTypes.FLAME, ctx.feet().add(0, 1, 0), 1.3, 60);
            Fx.at(ctx.level, ParticleTypes.LAVA, ctx.feet().add(0, 1, 0), 8, 0.5, 0.1);
            Fx.sound(ctx.level, ctx.feet(), "minecraft:item.firecharge.use", 1f, 0.7f);
            return true;
        });
        registry.put("inferno_wave", ctx -> {
            double radius = ctx.radius();
            double damage = ctx.damage();
            double burn = ctx.p("burn");
            Vec3 center = ctx.feet().add(0, 0.3, 0);
            Set<LivingEntity> hit = new HashSet<>();
            Fx.sound(ctx.level, center, "minecraft:entity.blaze.shoot", 1f, 0.5f);
            Fx.sound(ctx.level, center, "minecraft:item.firecharge.use", 1f, 0.6f);
            Zones.add(age -> {
                if (ctx.player.isRemoved()) {
                    return false;
                }
                double r = 1 + (radius - 1) * Math.min(1, age / 10.0);
                Fx.ring(ctx.level, ParticleTypes.FLAME, center, r, (int) (12 + r * 7), 0.02);
                Fx.ring(ctx.level, Fx.dust(0xFF8A3D, 1.6f), center.add(0, 0.4, 0), r, (int) (8 + r * 4), 0);
                if (age % 2 == 0) {
                    Fx.ring(ctx.level, ParticleTypes.SMOKE, center.add(0, 0.6, 0), r * 0.9, (int) (4 + r * 2), 0.02);
                }
                for (LivingEntity target : Targets.enemies(ctx.player, center, r)) {
                    if (hit.add(target)) {
                        ctx.hit(target, damage);
                        ctx.ignite(target, burn);
                        Vec3 away = target.position().subtract(center).multiply(1, 0, 1).normalize();
                        Compat.push(target, target.getDeltaMovement().add(away.x * 0.6, 0.25, away.z * 0.6));
                    }
                }
                return age < 10;
            });
            return true;
        });
        registry.put("meteor", ctx -> {
            Vec3 target = Targets.aimedPoint(ctx.player, ctx.p("range"));
            int delay = (int) Math.round(ctx.p("delay") * 20);
            double radius = ctx.radius();
            double damage = ctx.damage();
            double burn = ctx.p("burn");
            Vec3 sky = target.add(-7, 28, -7);
            Fx.sound(ctx.level, target, "minecraft:entity.blaze.ambient", 1.5f, 0.5f);
            Zones.add(age -> {
                if (ctx.player.isRemoved()) {
                    return false;
                }
                double t = Math.min(1, age / (double) delay);
                Vec3 head = sky.lerp(target, t);
                Fx.at(ctx.level, ParticleTypes.FLAME, head, 18, 0.5, 0.02);
                Fx.at(ctx.level, ParticleTypes.LARGE_SMOKE, head, 6, 0.6, 0.01);
                Fx.at(ctx.level, ParticleTypes.LAVA, head, 3, 0.4, 0);
                Fx.ring(ctx.level, Fx.dust(0xFF4A1A, 1.8f), target.add(0, 0.1, 0), radius, 30, 0);
                if (age < delay) {
                    return true;
                }
                Fx.at(ctx.level, ParticleTypes.EXPLOSION_EMITTER, target, 1, 0, 0);
                Fx.at(ctx.level, ParticleTypes.FLAME, target.add(0, 0.5, 0), 120, radius * 0.5, 0.15);
                Fx.at(ctx.level, ParticleTypes.LAVA, target, 30, radius * 0.4, 0.2);
                Fx.burstRing(ctx.level, ParticleTypes.CAMPFIRE_COSY_SMOKE, target.add(0, 0.3, 0), 40, 0.25);
                Fx.sound(ctx.level, target, "minecraft:entity.generic.explode", 2f, 0.7f);
                for (LivingEntity enemy : Targets.enemies(ctx.player, target, radius)) {
                    double falloff = 1 - 0.5 * Math.min(1, enemy.position().distanceTo(target) / radius);
                    ctx.hit(enemy, damage * falloff);
                    ctx.ignite(enemy, burn);
                    Vec3 away = enemy.position().subtract(target).normalize();
                    Compat.push(enemy, enemy.getDeltaMovement().add(away.x, 0.6, away.z));
                }
                return false;
            });
            return true;
        });
    }

    /** Ember Bolt reached a creature. */
    public static void emberBoltHit(ServerPlayer owner, LivingEntity target, boolean empowered) {
        AbilityContext ctx = new AbilityContext(owner, AbilityCatalog.get("ember_bolt"), empowered);
        if (!Targets.canHit(owner, target)) {
            return;
        }
        ctx.hit(target, ctx.damage());
        ctx.ignite(target, ctx.p("burn"));
        Fx.at(ctx.level, ParticleTypes.FLAME, target.getBoundingBox().getCenter(), 16, 0.3, 0.05);
        Fx.sound(ctx.level, target.position(), "minecraft:entity.blaze.hurt", 0.4f, 1.6f);
    }

    /** Explosive Orb burst. Never breaks blocks. */
    public static void orbBurst(ServerPlayer owner, ServerLevel level, Vec3 pos, boolean empowered) {
        AbilityContext ctx = new AbilityContext(owner, AbilityCatalog.get("explosive_orb"), empowered);
        double radius = ctx.radius();
        Fx.at(level, ParticleTypes.EXPLOSION, pos, 3, radius * 0.3, 0);
        Fx.sphere(level, ParticleTypes.FLAME, pos, radius * 0.8, 70);
        Fx.at(level, ParticleTypes.LAVA, pos, 10, 0.5, 0.1);
        Fx.sound(level, pos, "minecraft:entity.generic.explode", 1.2f, 1.1f);
        for (LivingEntity enemy : Targets.enemies(owner, pos, radius)) {
            ctx.hit(enemy, ctx.damage());
            ctx.ignite(enemy, 3);
            Vec3 away = enemy.position().subtract(pos).normalize();
            Compat.push(enemy, enemy.getDeltaMovement().add(away.x * 0.8, 0.4, away.z * 0.8));
        }
    }
}
