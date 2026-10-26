package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

final class VerdanceAbilities {
    private static final int GREEN = 0x6EE07A;
    private static final int GOLD = 0xFFD966;

    private VerdanceAbilities() {
    }

    static void register(Map<String, Ability> registry) {
        registry.put("mending_light", ctx -> {
            double radius = ctx.radius();
            Vec3 center = ctx.feet().add(0, 1, 0);
            Fx.ring(ctx.level, Fx.dust(GOLD, 1.4f), ctx.feet().add(0, 0.1, 0), radius, 50, 0);
            Fx.spiral(ctx.level, ParticleTypes.HAPPY_VILLAGER, ctx.feet(), 0.8, 2.4, 30, 2);
            for (LivingEntity ally : Targets.allies(ctx.player, center, radius)) {
                ctx.healTarget(ally, ctx.p("heal"));
                Fx.at(ctx.level, ParticleTypes.HEART, ally.position().add(0, ally.getBbHeight() + 0.3, 0), 3, 0.3, 0);
                Fx.at(ctx.level, ParticleTypes.END_ROD, ally.getBoundingBox().getCenter(), 8, 0.3, 0.03);
            }
            Fx.sound(ctx.level, center, "minecraft:block.amethyst_block.chime", 1.2f, 1.4f);
            Fx.sound(ctx.level, center, "minecraft:entity.player.levelup", 0.3f, 1.8f);
            return true;
        });
        registry.put("thorn_burst", ctx -> {
            double radius = ctx.radius();
            Vec3 center = ctx.feet().add(0, 0.8, 0);
            int poison = (int) Math.round(ctx.p("poison") * 20 * ctx.sheet.durationMultiplier());
            Fx.burstRing(ctx.level, ParticleTypes.COMPOSTER, center, 60, 0.6);
            Fx.burstRing(ctx.level, Fx.dust(GREEN, 1.3f), center.add(0, 0.3, 0), 40, 0.5);
            Fx.at(ctx.level, Compat.block(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH.defaultBlockState()), center, 30, radius * 0.3, 0.1);
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                ctx.hit(target, ctx.damage());
                Compat.addEffect(target, "minecraft:poison", poison, 1, true);
                Fx.at(ctx.level, ParticleTypes.CRIT, target.getBoundingBox().getCenter(), 8, 0.3, 0.2);
            }
            Fx.sound(ctx.level, center, "minecraft:block.sweet_berry_bush.break", 1f, 0.6f);
            Fx.sound(ctx.level, center, "minecraft:enchant.thorns.hit", 1f, 1f);
            return true;
        });
        registry.put("sanctuary", ctx -> {
            Vec3 center = ctx.feet();
            double radius = ctx.radius();
            double damage = ctx.damage();
            int ticks = ctx.ticks("duration");
            Fx.sound(ctx.level, center, "minecraft:block.beacon.activate", 1f, 1.3f);
            Zones.add(age -> {
                if (ctx.player.isRemoved() || age > ticks) {
                    return false;
                }
                if (age % 4 == 0) {
                    Fx.ring(ctx.level, Fx.dust(GOLD, 1.2f), center.add(0, 0.1, 0), radius, 40, 0);
                    Fx.ring(ctx.level, ParticleTypes.END_ROD, center.add(0, 0.2, 0), radius * 0.95, 8, 0.05);
                    Fx.disk(ctx.level, ParticleTypes.HAPPY_VILLAGER, center.add(0, 0.2, 0), radius, 3);
                }
                if (age % 20 == 0) {
                    for (LivingEntity ally : Targets.allies(ctx.player, center.add(0, 1, 0), radius)) {
                        Compat.addEffect(ally, "minecraft:regeneration", 50, 1, true);
                    }
                    for (LivingEntity enemy : Targets.enemies(ctx.player, center.add(0, 1, 0), radius)) {
                        if (Compat.isUndead(enemy)) {
                            ctx.hit(enemy, damage);
                            Fx.at(ctx.level, ParticleTypes.END_ROD, enemy.getBoundingBox().getCenter(), 10, 0.3, 0.05);
                        }
                    }
                }
                return true;
            });
            return true;
        });
        registry.put("radiant_nova", ctx -> {
            double radius = ctx.radius();
            Vec3 center = ctx.feet().add(0, 1, 0);
            int glow = ctx.ticks("duration");
            Fx.sphere(ctx.level, ParticleTypes.END_ROD, center, radius * 0.4, 80);
            Fx.burstRing(ctx.level, ParticleTypes.END_ROD, center, 60, 0.7);
            Fx.burstRing(ctx.level, Fx.dust(GOLD, 1.8f), center.add(0, -0.5, 0), 50, 0.6);
            Fx.at(ctx.level, Compat.flash(), center, 1, 0, 0);
            for (LivingEntity target : Targets.enemies(ctx.player, center, radius)) {
                double damage = ctx.damage() * (Compat.isUndead(target) ? 2 : 1);
                ctx.hit(target, damage);
                Compat.addEffect(target, "minecraft:glowing", glow, 0, true);
            }
            Fx.sound(ctx.level, center, "minecraft:block.beacon.power_select", 1.2f, 1.6f);
            Fx.sound(ctx.level, center, "minecraft:entity.firework_rocket.blast", 0.8f, 1.2f);
            return true;
        });
        registry.put("verdant_rebirth", ctx -> {
            int ticks = ctx.ticks("duration");
            ctx.player.setHealth(ctx.player.getMaxHealth());
            Compat.cleanse(ctx.player);
            int amplifier = Math.max(0, (int) Math.ceil(ctx.heal(ctx.p("absorption")) / 4.0) - 1);
            Compat.addEffect(ctx.player, "minecraft:absorption", 600, amplifier, false);
            ctx.profile.timers.put("rebirth", ctx.time + ticks);
            Vec3 center = ctx.feet().add(0, 1, 0);
            Fx.at(ctx.level, ParticleTypes.TOTEM_OF_UNDYING, center, 80, 0.6, 0.5);
            Fx.spiral(ctx.level, ParticleTypes.CHERRY_LEAVES, ctx.feet(), 1.2, 3, 60, 3);
            Fx.ring(ctx.level, Fx.dust(GREEN, 1.6f), ctx.feet().add(0, 0.1, 0), 2.2, 40, 0);
            Fx.sound(ctx.level, center, "minecraft:item.totem.use", 0.8f, 1.2f);
            return true;
        });
    }
}
