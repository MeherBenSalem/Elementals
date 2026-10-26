package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.AbilityDef;
import com.naizo.elementals.core.Keystone;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.Stat;
import com.naizo.elementals.core.StatSheet;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** One cast of an ability: the caster, their stats and the ability's parameters, already scaled. */
public final class AbilityContext {
    public final ServerPlayer player;
    public final ServerLevel level;
    public final AbilityDef def;
    public final StatSheet sheet;
    public final PowerProfile profile;
    public final long time;
    private final double damageMultiplier;
    private final boolean empowered;

    public AbilityContext(ServerPlayer player, AbilityDef def, boolean empowered) {
        this.player = player;
        this.level = Compat.serverLevel(player);
        this.def = def;
        this.sheet = PowerManager.sheet(player);
        this.profile = PowerManager.profile(player);
        this.time = level.getGameTime();
        this.empowered = empowered;
        double multiplier = sheet.damageMultiplier(def.school, PowerManager.rasBonusPercent(player));
        if (profile.active("eclipse", time)) {
            multiplier *= 1 + PowerManager.CONFIG.param("eclipse", "bonus") / 100.0;
        }
        if (empowered) {
            multiplier *= 2;
        }
        this.damageMultiplier = multiplier;
    }

    public double p(String key) {
        return PowerManager.CONFIG.param(def.id, key);
    }

    public double damage() {
        return p("damage") * damageMultiplier;
    }

    public double radius() {
        return p("radius") * sheet.areaMultiplier();
    }

    /** A "seconds" parameter as ticks, stretched by Effect Duration. */
    public int ticks(String key) {
        return (int) Math.round(p(key) * 20 * sheet.durationMultiplier());
    }

    public double heal(double amount) {
        return amount * sheet.healMultiplier();
    }

    public int color() {
        return def.school.color;
    }

    public Vec3 eye() {
        return player.getEyePosition();
    }

    public Vec3 feet() {
        return player.position();
    }

    public boolean empowered() {
        return empowered;
    }

    /** Deals ability damage with crits and leech. Returns true when the target was hurt. */
    public boolean hit(LivingEntity target, double amount) {
        if (amount <= 0 || !target.isAlive()) {
            return false;
        }
        boolean crit = level.getRandom().nextDouble() < sheet.critChance();
        if (crit) {
            amount *= sheet.critMultiplier();
        }
        float before = target.getHealth();
        Compat.resetInvulnerability(target);
        boolean hurt = Compat.hurt(target, Compat.abilityDamage(player), (float) amount);
        if (hurt) {
            if (crit) {
                Fx.at(level, ParticleTypes.ENCHANTED_HIT, target.getBoundingBox().getCenter(), 12, 0.3, 0.2);
                Fx.sound(level, target.position(), "minecraft:entity.player.attack.crit", 0.8f, 1.2f);
            }
            double dealt = Math.max(0, before - target.getHealth());
            double leech = sheet.get(Stat.LIFESTEAL);
            if (leech > 0 && dealt > 0) {
                player.heal((float) (dealt * leech / 100.0));
            }
        }
        return hurt;
    }

    public void ignite(LivingEntity target, double seconds) {
        if (sheet.has(Keystone.PYRE_HEART)) {
            seconds *= 2;
        }
        Compat.ignite(target, (float) seconds);
    }

    public void healTarget(LivingEntity target, double amount) {
        target.heal((float) heal(amount));
    }
}
