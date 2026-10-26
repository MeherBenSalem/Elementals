package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.power.StatApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class EarthAbilities {
    private static final int EARTH = 0x8A6A3F;
    private static final int STONE = 0x9A9488;

    private EarthAbilities() {
    }

    /** Block particles that match the ground at a position, or stone. */
    static ParticleOptions ground(AbilityContext ctx, Vec3 pos) {
        BlockState state = Compat.blockBelow(ctx.level, BlockPos.containing(pos.x, pos.y + 0.1, pos.z));
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            state = Blocks.STONE.defaultBlockState();
        }
        return Compat.block(state);
    }

    static void register(Map<String, Ability> registry) {
        registry.put("stone_spear", ctx -> {
            LivingEntity target = Targets.aimed(ctx.player, ctx.p("range"));
            if (target == null) {
                return false;
            }
            Vec3 base = target.position();
            Fx.spike(ctx.level, Fx.dust(STONE, 2.6f), base, 2.6, 0.75);
            Fx.spike(ctx.level, Fx.dust(EARTH, 1.8f), base, 1.6, 0.55);
            for (int i = 0; i < 6; i++) {
                Fx.at(ctx.level, Compat.block(Blocks.DRIPSTONE_BLOCK.defaultBlockState()), base.add(0, i * 0.35, 0), 8, 0.15, 0.1);
            }
            Fx.at(ctx.level, ground(ctx, base), base.add(0, 0.2, 0), 45, 0.6, 0.25);
            Fx.at(ctx.level, ParticleTypes.POOF, base.add(0, 0.2, 0), 10, 0.5, 0.05);
            Fx.at(ctx.level, ParticleTypes.CRIT, base.add(0, 1.5, 0), 12, 0.2, 0.3);
            ctx.hit(target, ctx.damage());
            Compat.push(target, new Vec3(target.getDeltaMovement().x, 0.95, target.getDeltaMovement().z));
            Fx.sound(ctx.level, base, "minecraft:block.pointed_dripstone.land", 1f, 0.7f);
            Fx.sound(ctx.level, base, "minecraft:block.stone.break", 1f, 0.6f);
            return true;
        });
        registry.put("stone_skin", ctx -> {
            int ticks = ctx.ticks("duration");
            Compat.addEffect(ctx.player, "minecraft:resistance", ticks, (int) ctx.p("amplifier"), false);
            StatApplier.tempModifier(ctx.player, "stone_skin", "knockback_resistance", 1.0, false);
            ctx.profile.timers.put("stone_skin", ctx.time + ticks);
            Fx.sphere(ctx.level, Compat.block(Blocks.STONE.defaultBlockState()), ctx.feet().add(0, 1, 0), 1.2, 60);
            Fx.at(ctx.level, Fx.dust(0xC9A05A, 1.5f), ctx.feet().add(0, 1, 0), 30, 0.5, 0);
            Fx.sound(ctx.level, ctx.feet(), "minecraft:item.armor.equip_netherite", 1f, 0.7f);
            Fx.sound(ctx.level, ctx.feet(), "minecraft:block.stone.place", 1f, 0.5f);
            return true;
        });
        registry.put("tremor", ctx -> {
            double radius = ctx.radius();
            Vec3 center = ctx.feet();
            ParticleOptions dirt = ground(ctx, center);
            Fx.burstRing(ctx.level, dirt, center.add(0, 0.2, 0), 80, 0.6);
            Fx.ring(ctx.level, dirt, center.add(0, 0.1, 0), radius, 50, 0.3);
            Fx.burstRing(ctx.level, ParticleTypes.CAMPFIRE_COSY_SMOKE, center.add(0, 0.2, 0), 20, 0.1);
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * 2 * i / 8;
                Vec3 spot = center.add(Math.cos(a) * radius * 0.6, 0, Math.sin(a) * radius * 0.6);
                Fx.spike(ctx.level, Fx.dust(EARTH, 2.2f), spot, 1.3, 0.4);
                if (i % 2 == 0) {
                    Fx.at(ctx.level, ParticleTypes.EXPLOSION, spot.add(0, 0.4, 0), 1, 0, 0);
                }
            }
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                ctx.hit(target, ctx.damage());
                Vec3 away = target.position().subtract(center).multiply(1, 0, 1).normalize().scale(0.4);
                Compat.push(target, new Vec3(away.x, 0.85, away.z));
            }
            Fx.sound(ctx.level, center, "minecraft:entity.generic.explode", 0.7f, 0.6f);
            Fx.sound(ctx.level, center, "minecraft:block.anvil.land", 0.6f, 0.5f);
            return true;
        });
        registry.put("fissure", ctx -> {
            Vec3 look = ctx.player.getLookAngle();
            Vec3 dir = new Vec3(look.x, 0, look.z).normalize();
            Vec3 origin = ctx.feet();
            int length = (int) Math.round(ctx.p("range") * ctx.sheet.areaMultiplier());
            double damage = ctx.damage();
            Set<LivingEntity> hit = new HashSet<>();
            Zones.add(age -> {
                if (ctx.player.isRemoved()) {
                    return false;
                }
                int step = age + 1;
                Vec3 point = Targets.ground(ctx.player, origin.add(dir.scale(step)).add(0, 2, 0));
                ParticleOptions dirt = ground(ctx, point);
                Fx.at(ctx.level, dirt, point.add(0, 0.6, 0), 40, 0.3, 0.6, 0.3, 0.2);
                Fx.spike(ctx.level, Fx.dust(step % 2 == 0 ? STONE : EARTH, 2.2f), point, 1.2 + (step % 3) * 0.35, 0.45);
                Fx.at(ctx.level, ParticleTypes.POOF, point.add(0, 0.2, 0), 4, 0.3, 0.03);
                Fx.at(ctx.level, ParticleTypes.CAMPFIRE_COSY_SMOKE, point.add(0, 0.2, 0), 1, 0.2, 0.05);
                Fx.at(ctx.level, ParticleTypes.LAVA, point.add(0, 0.3, 0), 1, 0.2, 0);
                for (LivingEntity target : Targets.enemies(ctx.player, point.add(0, 0.8, 0), 1.6)) {
                    if (hit.add(target)) {
                        ctx.hit(target, damage);
                        Compat.push(target, new Vec3(target.getDeltaMovement().x, 0.7, target.getDeltaMovement().z));
                    }
                }
                if (step % 2 == 0) {
                    Fx.sound(ctx.level, point, "minecraft:block.stone.break", 0.8f, 0.6f + step * 0.03f);
                }
                return step < length;
            });
            return true;
        });
        registry.put("mountains_wrath", ctx -> {
            int ticks = ctx.ticks("duration");
            double radius = ctx.radius();
            Compat.addEffect(ctx.player, "minecraft:resistance", ticks, 1, false);
            Compat.addEffect(ctx.player, "minecraft:strength", ticks, 0, false);
            StatApplier.tempModifier(ctx.player, "mountains_wrath", "knockback_resistance", 1.0, false);
            ctx.profile.timers.put("mountains_wrath", ctx.time + ticks);
            Vec3 center = ctx.feet();
            ParticleOptions dirt = ground(ctx, center);
            for (int ring = 1; ring <= 3; ring++) {
                Fx.ring(ctx.level, dirt, center.add(0, 0.2, 0), radius * ring / 3.0, 30 * ring, 0.4);
            }
            // Blasts sit on the outer ring so they never fill the caster's own view.
            for (int i = 0; i < 6; i++) {
                double a = Math.PI * 2 * i / 6 + 0.5;
                Fx.at(ctx.level, ParticleTypes.EXPLOSION, center.add(Math.cos(a) * radius * 0.8, 0.4, Math.sin(a) * radius * 0.8), 1, 0, 0);
            }
            Fx.spiral(ctx.level, Fx.dust(0xC9A05A, 2f), center, 1.2, 3, 60, 3);
            for (int i = 0; i < 10; i++) {
                double a = Math.PI * 2 * i / 10;
                double d = radius * (0.45 + 0.25 * (i % 2));
                Fx.spike(ctx.level, Fx.dust(i % 2 == 0 ? STONE : EARTH, 2.6f), center.add(Math.cos(a) * d, 0, Math.sin(a) * d), 2.0 + (i % 3) * 0.6, 0.6);
            }
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                ctx.hit(target, ctx.damage());
                Vec3 away = target.position().subtract(center).multiply(1, 0, 1).normalize().scale(1.2);
                Compat.push(target, new Vec3(away.x, 0.6, away.z));
            }
            Fx.sound(ctx.level, center, "minecraft:entity.ravager.roar", 1f, 0.7f);
            Fx.sound(ctx.level, center, "minecraft:entity.generic.explode", 0.8f, 0.5f);
            return true;
        });
    }
}
