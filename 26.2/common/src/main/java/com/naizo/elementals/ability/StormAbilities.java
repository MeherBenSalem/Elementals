package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class StormAbilities {
    private static final int CYAN = 0x6FE7FF;

    private StormAbilities() {
    }

    static void bolt(AbilityContext ctx, Vec3 from, Vec3 to) {
        ParticleOptions dust = Fx.dust(CYAN, 1.4f);
        Fx.arc(ctx.level, dust, from, to, 0.8);
        Fx.arc(ctx.level, Fx.dust(0xFFFFFF, 0.7f), from, to, 0.4);
        Fx.arc(ctx.level, ParticleTypes.ELECTRIC_SPARK, from, to, 0.5);
        Fx.at(ctx.level, ParticleTypes.ELECTRIC_SPARK, to, 18, 0.3, 0.35);
    }

    static void register(Map<String, Ability> registry) {
        registry.put("chain_lightning", ctx -> {
            LivingEntity first = Targets.aimed(ctx.player, ctx.p("range"));
            if (first == null) {
                return false;
            }
            Set<LivingEntity> hit = new HashSet<>();
            Vec3 from = ctx.eye().add(ctx.player.getLookAngle().scale(1.4)).add(0, -0.35, 0);
            LivingEntity current = first;
            double damage = ctx.damage();
            int chains = (int) ctx.p("chains");
            for (int i = 0; i <= chains && current != null; i++) {
                Vec3 to = current.getBoundingBox().getCenter();
                bolt(ctx, from, to);
                ctx.hit(current, damage);
                hit.add(current);
                damage *= 0.85;
                from = to;
                LivingEntity next = null;
                for (LivingEntity candidate : Targets.enemies(ctx.player, to, ctx.p("chain_range") * ctx.sheet.areaMultiplier())) {
                    if (!hit.contains(candidate)) {
                        next = candidate;
                        break;
                    }
                }
                current = next;
            }
            Fx.sound(ctx.level, first.position(), "minecraft:entity.lightning_bolt.impact", 0.8f, 1.6f);
            return true;
        });
        registry.put("static_field", ctx -> {
            int ticks = ctx.ticks("duration");
            double radius = ctx.radius();
            double damage = ctx.damage();
            ctx.profile.timers.put("static_field", ctx.time + ticks);
            Fx.ring(ctx.level, ParticleTypes.ELECTRIC_SPARK, ctx.feet().add(0, 0.2, 0), radius, 40, 0.05);
            Fx.sound(ctx.level, ctx.feet(), "minecraft:block.beacon.activate", 0.7f, 1.8f);
            Zones.add(age -> {
                if (ctx.player.isRemoved() || !ctx.player.isAlive() || age > ticks) {
                    return false;
                }
                if (age % 10 == 0) {
                    Vec3 center = ctx.player.position().add(0, 1, 0);
                    Fx.ring(ctx.level, Fx.dust(CYAN, 0.7f), ctx.player.position().add(0, 0.15, 0), radius, 24, 0);
                    for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                        bolt(ctx, center, target.getBoundingBox().getCenter());
                        ctx.hit(target, damage);
                        Fx.sound(ctx.level, target.position(), "minecraft:entity.bee.hurt", 0.4f, 2f);
                        break;
                    }
                }
                return true;
            });
            return true;
        });
        registry.put("thunderclap", ctx -> {
            double radius = ctx.radius();
            Vec3 center = ctx.feet().add(0, 0.8, 0);
            Fx.burstRing(ctx.level, ParticleTypes.ELECTRIC_SPARK, center, 60, 0.9);
            Fx.burstRing(ctx.level, ParticleTypes.CLOUD, ctx.feet().add(0, 0.2, 0), 40, 0.45);
            Fx.ring(ctx.level, Fx.dust(CYAN, 1.4f), ctx.feet().add(0, 0.2, 0), radius, 50, 0);
            Fx.sound(ctx.level, center, "minecraft:entity.lightning_bolt.thunder", 0.7f, 1.6f);
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                ctx.hit(target, ctx.damage());
                Vec3 away = target.position().subtract(ctx.feet()).multiply(1, 0, 1).normalize().scale(ctx.p("knockback"));
                Compat.push(target, new Vec3(away.x, 0.55, away.z));
                bolt(ctx, center, target.getBoundingBox().getCenter());
            }
            return true;
        });
        registry.put("gale_step", ctx -> {
            Vec3 start = ctx.feet();
            Vec3 end = Targets.dashTarget(ctx.player, ctx.p("range"));
            if (end.distanceTo(start) < 1.5) {
                return false;
            }
            Fx.line(ctx.level, ParticleTypes.CLOUD, start.add(0, 0.6, 0), end.add(0, 0.6, 0), 0.4);
            Fx.arc(ctx.level, ParticleTypes.ELECTRIC_SPARK, start.add(0, 1, 0), end.add(0, 1, 0), 0.6);
            Compat.teleport(ctx.player, end.x, end.y, end.z);
            ctx.profile.timers.put("gale_fall", ctx.time + 100);
            Fx.burstRing(ctx.level, ParticleTypes.CLOUD, end.add(0, 0.3, 0), 16, 0.2);
            Fx.sound(ctx.level, end, "minecraft:entity.ender_dragon.flap", 0.8f, 1.5f);
            return true;
        });
        registry.put("tempest", ctx -> {
            Vec3 center = Targets.aimedPoint(ctx.player, ctx.p("range"));
            double radius = ctx.radius();
            double damage = ctx.damage();
            int strikes = (int) ctx.p("strikes");
            int total = Math.max(strikes, ctx.ticks("duration"));
            int every = Math.max(1, total / strikes);
            Fx.sound(ctx.level, center, "minecraft:entity.lightning_bolt.thunder", 1.2f, 0.8f);
            Zones.add(age -> {
                if (ctx.player.isRemoved()) {
                    return false;
                }
                if (age % 3 == 0) {
                    Fx.ring(ctx.level, ParticleTypes.LARGE_SMOKE, center.add(0, 9, 0), radius * 0.8, 24, 0);
                    Fx.ring(ctx.level, Fx.dust(0x9AA6B8, 2.5f), center.add(0, 8.5, 0), radius * 0.6, 20, 0);
                    Fx.ring(ctx.level, Fx.dust(CYAN, 1.0f), center.add(0, 0.1, 0), radius, 30, 0);
                }
                if (age % every == 0 && age / every < strikes) {
                    List<LivingEntity> enemies = new ArrayList<>(Targets.enemies(ctx.player, center, radius));
                    Vec3 strike;
                    if (!enemies.isEmpty()) {
                        strike = enemies.get(ctx.level.getRandom().nextInt(enemies.size())).position();
                    } else {
                        double a = ctx.level.getRandom().nextDouble() * Math.PI * 2;
                        double d = ctx.level.getRandom().nextDouble() * radius;
                        strike = Targets.ground(ctx.player, center.add(Math.cos(a) * d, 4, Math.sin(a) * d));
                    }
                    Compat.visualLightning(ctx.level, strike.x, strike.y, strike.z);
                    Fx.at(ctx.level, ParticleTypes.ELECTRIC_SPARK, strike.add(0, 0.5, 0), 30, 0.6, 0.4);
                    for (LivingEntity enemy : Targets.enemies(ctx.player, strike, 2.5)) {
                        ctx.hit(enemy, damage);
                    }
                }
                return age < total + 2;
            });
            return true;
        });
    }
}
