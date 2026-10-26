package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

final class UmbralAbilities {
    private static final int PURPLE = 0xB26BFF;

    private UmbralAbilities() {
    }

    static void register(Map<String, Ability> registry) {
        registry.put("shadow_bolt", ctx -> {
            LivingEntity target = Targets.aimed(ctx.player, ctx.p("range"));
            if (target == null) {
                return false;
            }
            Vec3 from = ctx.eye().add(ctx.player.getLookAngle().scale(1.4)).add(0, -0.35, 0);
            Vec3 to = target.getBoundingBox().getCenter();
            Fx.line(ctx.level, Fx.dust(PURPLE, 1.3f), from, to, 0.2);
            Fx.line(ctx.level, ParticleTypes.SQUID_INK, from, to, 0.8);
            Fx.at(ctx.level, ParticleTypes.REVERSE_PORTAL, to, 30, 0.3, 0.1);
            ctx.hit(target, ctx.damage());
            Compat.addEffect(target, "minecraft:wither", (int) Math.round(ctx.p("wither") * 20 * ctx.sheet.durationMultiplier()), 1, true);
            Fx.sound(ctx.level, from, "minecraft:entity.wither.shoot", 0.4f, 1.4f);
            return true;
        });
        registry.put("soul_harvest", ctx -> {
            double radius = ctx.radius();
            Vec3 center = ctx.feet().add(0, 1, 0);
            double dealt = 0;
            Fx.ring(ctx.level, ParticleTypes.SCULK_SOUL, ctx.feet().add(0, 0.2, 0), radius, 36, 0.05);
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                float before = target.getHealth();
                ctx.hit(target, ctx.damage());
                dealt += Math.max(0, before - target.getHealth());
                Fx.line(ctx.level, ParticleTypes.SOUL, target.getBoundingBox().getCenter(), center, 0.5);
                Fx.at(ctx.level, ParticleTypes.SCULK_SOUL, target.getBoundingBox().getCenter(), 6, 0.2, 0.05);
            }
            if (dealt > 0) {
                ctx.healTarget(ctx.player, dealt * ctx.p("leech") / 100.0);
                Fx.at(ctx.level, Fx.dust(PURPLE, 1.2f), center, 20, 0.5, 0);
            }
            Fx.sound(ctx.level, center, "minecraft:particle.soul_escape", 1.5f, 0.6f);
            Fx.sound(ctx.level, center, "minecraft:entity.vex.charge", 0.7f, 0.6f);
            return true;
        });
        registry.put("void_rift", ctx -> {
            Vec3 center = Targets.aimedPoint(ctx.player, ctx.p("range")).add(0, 1, 0);
            double radius = ctx.radius();
            double damage = ctx.damage();
            int ticks = ctx.ticks("duration");
            Fx.sound(ctx.level, center, "minecraft:block.end_portal.spawn", 0.5f, 1.4f);
            Zones.add(age -> {
                if (ctx.player.isRemoved() || age > ticks) {
                    if (age > ticks) {
                        Fx.at(ctx.level, ParticleTypes.REVERSE_PORTAL, center, 60, 0.4, 0.4);
                    }
                    return false;
                }
                double spin = age * 0.35;
                for (int i = 0; i < 3; i++) {
                    double a = spin + i * Math.PI * 2 / 3;
                    Fx.at(ctx.level, Fx.dust(PURPLE, 1.4f), center.add(Math.cos(a) * 0.9, Math.sin(age * 0.2) * 0.3, Math.sin(a) * 0.9), 1, 0, 0);
                }
                Fx.at(ctx.level, ParticleTypes.PORTAL, center, 6, radius * 0.4, 0.6);
                Fx.at(ctx.level, ParticleTypes.SQUID_INK, center, 1, 0.2, 0.01);
                for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                    Vec3 pull = center.subtract(target.position()).normalize().scale(0.12);
                    Compat.push(target, target.getDeltaMovement().scale(0.8).add(pull.x, pull.y * 0.5 + 0.02, pull.z));
                    if (age % 20 == 0) {
                        ctx.hit(target, damage);
                        Compat.addEffect(target, "minecraft:blindness", 40, 0, true);
                    }
                }
                if (age % 20 == 0) {
                    Fx.sound(ctx.level, center, "minecraft:block.portal.ambient", 0.4f, 1.6f);
                }
                return true;
            });
            return true;
        });
        registry.put("shadow_step", ctx -> {
            LivingEntity target = Targets.aimed(ctx.player, ctx.p("range"));
            if (target == null) {
                return false;
            }
            Vec3 facing = Vec3.directionFromRotation(0, target.getYRot());
            Vec3 start = ctx.feet();
            Vec3[] options = {
                    target.position().subtract(facing.scale(1.6)),
                    target.position().add(facing.yRot((float) Math.PI / 2).scale(1.6)),
                    target.position().add(facing.yRot((float) -Math.PI / 2).scale(1.6)),
                    target.position().add(facing.scale(1.6))
            };
            Vec3 destination = null;
            for (Vec3 option : options) {
                if (ctx.level.noCollision(ctx.player, ctx.player.getBoundingBox().move(option.subtract(start)))) {
                    destination = option;
                    break;
                }
            }
            if (destination == null) {
                return false;
            }
            Vec3 look = target.getEyePosition().subtract(destination.add(0, ctx.player.getEyeHeight(), 0));
            float yaw = (float) (Math.toDegrees(Math.atan2(-look.x, look.z)));
            float pitch = (float) (-Math.toDegrees(Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z))));
            Fx.at(ctx.level, ParticleTypes.REVERSE_PORTAL, start.add(0, 1, 0), 40, 0.3, 0.6);
            Fx.at(ctx.level, ParticleTypes.SQUID_INK, start.add(0, 1, 0), 12, 0.3, 0.05);
            Compat.teleportFacing(ctx.player, destination.x, destination.y, destination.z, yaw, pitch);
            Fx.at(ctx.level, ParticleTypes.PORTAL, destination.add(0, 1, 0), 40, 0.3, 0.4);
            Compat.addEffect(ctx.player, "minecraft:invisibility", ctx.ticks("duration"), 0, false);
            Fx.sound(ctx.level, destination, "minecraft:entity.enderman.teleport", 0.8f, 0.8f);
            return true;
        });
        registry.put("eclipse", ctx -> {
            int ticks = ctx.ticks("duration");
            double radius = ctx.radius();
            ctx.profile.timers.put("eclipse", ctx.time + ticks);
            Compat.addEffect(ctx.player, "minecraft:invisibility", ticks, 0, false);
            Compat.addEffect(ctx.player, "minecraft:speed", ticks, 1, false);
            Vec3 center = ctx.feet().add(0, 1, 0);
            Fx.sphere(ctx.level, Fx.dust(0x2A1040, 2.2f), center, radius * 0.5, 160);
            Fx.sphere(ctx.level, ParticleTypes.SQUID_INK, center, 1.5, 50);
            Fx.burstRing(ctx.level, ParticleTypes.SCULK_SOUL, center, 40, 0.3);
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                Compat.addEffect(target, "minecraft:darkness", ticks, 0, true);
                Compat.addEffect(target, "minecraft:weakness", ticks, 0, true);
            }
            Fx.sound(ctx.level, center, "minecraft:block.beacon.deactivate", 1f, 0.5f);
            Fx.sound(ctx.level, center, "minecraft:ambient.cave", 0.8f, 0.7f);
            return true;
        });
    }
}
