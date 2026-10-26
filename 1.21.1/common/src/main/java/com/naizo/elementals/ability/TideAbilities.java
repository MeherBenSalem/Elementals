package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class TideAbilities {
    private static final int ICE = 0x9FD8FF;
    private static final int DEEP = 0x2F6BFF;

    private TideAbilities() {
    }

    static void register(Map<String, Ability> registry) {
        registry.put("frost_lance", ctx -> {
            Vec3 eye = ctx.eye();
            Vec3 look = ctx.player.getLookAngle();
            Vec3 end = eye.add(look.scale(ctx.p("range")));
            BlockHitResult block = ctx.level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ctx.player));
            if (block.getType() != HitResult.Type.MISS) {
                end = block.getLocation();
            }
            Vec3 from = eye.add(look.scale(1.4)).add(0, -0.3, 0);
            final Vec3 lineEnd = end;
            List<LivingEntity> hit = ctx.level.getEntitiesOfClass(LivingEntity.class, new AABB(from, lineEnd).inflate(1.2),
                            e -> Targets.canHit(ctx.player, e) && e.getBoundingBox().inflate(0.6).clip(from, lineEnd).isPresent())
                    .stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(from))).limit((long) ctx.p("pierce")).toList();
            Fx.line(ctx.level, Fx.dust(ICE, 1.1f), from, end, 0.15);
            Fx.line(ctx.level, ParticleTypes.SNOWFLAKE, from, end, 0.5);
            Fx.at(ctx.level, ParticleTypes.ITEM_SNOWBALL, end, 12, 0.2, 0.1);
            int slow = (int) Math.round(ctx.p("slow") * 20 * ctx.sheet.durationMultiplier());
            for (LivingEntity target : hit) {
                ctx.hit(target, ctx.damage());
                Compat.addEffect(target, "minecraft:slowness", slow, 2, true);
                Compat.setFrozen(target, 140);
                Fx.at(ctx.level, ParticleTypes.SNOWFLAKE, target.getBoundingBox().getCenter(), 14, 0.3, 0.05);
            }
            Fx.sound(ctx.level, from, "minecraft:block.glass.break", 0.6f, 1.6f);
            Fx.sound(ctx.level, from, "minecraft:entity.player.hurt_freeze", 0.5f, 1.2f);
            return true;
        });
        registry.put("tidebinder", ctx -> {
            int ticks = ctx.ticks("duration");
            Compat.addEffect(ctx.player, "elementals:tidebinder_effect", ticks, 0, false);
            Compat.addEffect(ctx.player, "minecraft:regeneration", ticks, 0, false);
            Compat.addEffect(ctx.player, "minecraft:water_breathing", ticks, 0, false);
            Compat.addEffect(ctx.player, "minecraft:dolphins_grace", ticks, 0, false);
            ctx.player.clearFire();
            Fx.spiral(ctx.level, Fx.dust(DEEP, 1.3f), ctx.feet(), 0.9, 2.2, 50, 2.5);
            Fx.spiral(ctx.level, ParticleTypes.SPLASH, ctx.feet(), 1.1, 2.2, 30, -2);
            Fx.at(ctx.level, ParticleTypes.BUBBLE_POP, ctx.feet().add(0, 1, 0), 20, 0.5, 0.05);
            Fx.sound(ctx.level, ctx.feet(), "minecraft:item.trident.riptide_1", 0.8f, 1.2f);
            return true;
        });
        registry.put("glacial_prison", ctx -> {
            double radius = ctx.radius();
            int ticks = ctx.ticks("duration");
            Vec3 center = ctx.feet().add(0, 0.3, 0);
            Fx.ring(ctx.level, Fx.dust(ICE, 1.6f), center, radius, 60, 0);
            Fx.burstRing(ctx.level, ParticleTypes.SNOWFLAKE, center.add(0, 0.5, 0), 50, 0.5);
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                ctx.hit(target, ctx.damage());
                Compat.addEffect(target, "minecraft:slowness", ticks, 6, true);
                Compat.addEffect(target, "minecraft:mining_fatigue", ticks, 2, false);
                Compat.addEffect(target, "minecraft:weakness", ticks, 1, false);
                Compat.setFrozen(target, 300);
                Compat.push(target, Vec3.ZERO);
                Vec3 c = target.getBoundingBox().getCenter();
                Fx.sphere(ctx.level, ParticleTypes.SNOWFLAKE, c, Math.max(0.8, target.getBbWidth()), 30);
                Fx.at(ctx.level, Compat.block(net.minecraft.world.level.block.Blocks.ICE.defaultBlockState()), c, 20, 0.4, 0.1);
            }
            Fx.sound(ctx.level, center, "minecraft:block.glass.break", 1f, 0.6f);
            Fx.sound(ctx.level, center, "minecraft:block.powder_snow.place", 1f, 0.8f);
            return true;
        });
        registry.put("tidal_surge", ctx -> {
            Vec3 start = ctx.feet();
            Vec3 end = Targets.dashTarget(ctx.player, ctx.p("range"));
            if (end.distanceTo(start) < 1.5) {
                return false;
            }
            Vec3 path = end.subtract(start);
            Vec3 side = new Vec3(-path.z, 0, path.x).normalize();
            Set<LivingEntity> hit = new HashSet<>();
            for (LivingEntity target : ctx.level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(2),
                    e -> Targets.isEnemy(ctx.player, e) && e.getBoundingBox().inflate(1.2).clip(start.add(0, 0.8, 0), end.add(0, 0.8, 0)).isPresent())) {
                if (hit.add(target)) {
                    ctx.hit(target, ctx.damage());
                    double sign = target.position().subtract(start).dot(side) >= 0 ? 1 : -1;
                    Compat.push(target, side.scale(sign * 1.1).add(0, 0.45, 0));
                }
            }
            Fx.line(ctx.level, ParticleTypes.SPLASH, start.add(0, 0.3, 0), end.add(0, 0.3, 0), 0.15);
            Fx.line(ctx.level, Fx.dust(DEEP, 1.6f), start.add(0, 0.6, 0), end.add(0, 0.6, 0), 0.3);
            Fx.line(ctx.level, ParticleTypes.BUBBLE_POP, start.add(0, 1, 0), end.add(0, 1, 0), 0.4);
            Compat.teleport(ctx.player, end.x, end.y, end.z);
            Fx.burstRing(ctx.level, ParticleTypes.SPLASH, end.add(0, 0.3, 0), 30, 0.3);
            Fx.sound(ctx.level, end, "minecraft:item.trident.riptide_3", 0.8f, 1.1f);
            return true;
        });
        registry.put("maelstrom", ctx -> {
            Vec3 center = Targets.aimedPoint(ctx.player, ctx.p("range")).add(0, 0.2, 0);
            double radius = ctx.radius();
            double damage = ctx.damage();
            int ticks = ctx.ticks("duration");
            Fx.sound(ctx.level, center, "minecraft:entity.generic.splash", 1.2f, 0.6f);
            Zones.add(age -> {
                if (ctx.player.isRemoved() || age > ticks) {
                    return false;
                }
                double spin = age * 0.3;
                for (int arm = 0; arm < 4; arm++) {
                    for (int i = 0; i < 6; i++) {
                        double r = radius * (i + 1) / 6.0;
                        double a = spin + arm * Math.PI / 2 + r * 0.45;
                        Fx.at(ctx.level, i % 2 == 0 ? Fx.dust(DEEP, 1.4f) : ParticleTypes.SPLASH,
                                center.add(Math.cos(a) * r, 0.2 + (6 - i) * 0.08, Math.sin(a) * r), 1, 0, 0);
                    }
                }
                Fx.at(ctx.level, ParticleTypes.BUBBLE_COLUMN_UP, center, 4, 0.3, 0.3, 0.3, 0.2);
                for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                    Vec3 toCenter = center.subtract(target.position()).multiply(1, 0, 1);
                    double distance = Math.max(0.5, toCenter.length());
                    Vec3 inward = toCenter.normalize().scale(Math.min(0.25, distance * 0.05));
                    Vec3 swirl = new Vec3(-toCenter.z, 0, toCenter.x).normalize().scale(0.15);
                    Compat.push(target, new Vec3(inward.x + swirl.x, target.getDeltaMovement().y, inward.z + swirl.z));
                    if (age % 20 == 0) {
                        ctx.hit(target, damage);
                        Compat.addEffect(target, "minecraft:slowness", 30, 1, false);
                    }
                }
                if (age % 15 == 0) {
                    Fx.sound(ctx.level, center, "minecraft:entity.player.swim", 0.8f, 0.5f);
                }
                return true;
            });
            return true;
        });
    }
}
