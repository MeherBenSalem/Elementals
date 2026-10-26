package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.AbilityCatalog;
import com.naizo.elementals.core.AbilityDef;
import com.naizo.elementals.core.Keystone;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.core.School;
import com.naizo.elementals.core.StatSheet;
import com.naizo.elementals.power.PowerManager;
import com.naizo.elementals.power.StatApplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** Validates and pays for casts, then runs the ability. */
public final class AbilityRunner {
    private static final Map<String, Ability> REGISTRY = new HashMap<>();

    static {
        PyromancyAbilities.register(REGISTRY);
        StormAbilities.register(REGISTRY);
        UmbralAbilities.register(REGISTRY);
        TideAbilities.register(REGISTRY);
        EarthAbilities.register(REGISTRY);
        VerdanceAbilities.register(REGISTRY);
    }

    private AbilityRunner() {
    }

    public static boolean implemented(String id) {
        return REGISTRY.containsKey(id);
    }

    public static void cast(ServerPlayer player, int slot) {
        PowerProfile profile = PowerManager.profile(player);
        if (slot < 0 || slot >= PowerProfile.SLOTS) {
            return;
        }
        String id = profile.slots[slot];
        if (id.isEmpty()) {
            Compat.overlay(player, Component.translatable("message.elementals.empty_slot", slot + 1).withStyle(ChatFormatting.GRAY));
            return;
        }
        if (!PowerManager.sheet(player).abilities.contains(id)) {
            Compat.overlay(player, Component.translatable("message.elementals.not_unlocked").withStyle(ChatFormatting.RED));
            return;
        }
        perform(player, id);
    }

    /** Casts from a spell book: no tree node needed, but the Pyromancy origin and a level are. */
    public static boolean castFromBook(ServerPlayer player, String id, int requiredLevel) {
        PowerProfile profile = PowerManager.profile(player);
        if (profile.originSchool() != School.PYROMANCY) {
            Compat.overlay(player, Component.translatable("message.elementals.book_origin").withStyle(ChatFormatting.RED));
            return false;
        }
        if (PowerManager.progressLevel(player) < requiredLevel) {
            Compat.overlay(player, Component.translatable("message.elementals.requires_level_short", requiredLevel).withStyle(ChatFormatting.RED));
            return false;
        }
        return perform(player, id);
    }

    /** Admin and test casting: ignores unlocks, cooldowns and cost. */
    public static boolean forceCast(ServerPlayer player, String id) {
        AbilityDef def = AbilityCatalog.get(id);
        Ability ability = REGISTRY.get(id);
        if (def == null || ability == null) {
            return false;
        }
        return ability.cast(new AbilityContext(player, def, false));
    }

    private static boolean perform(ServerPlayer player, String id) {
        AbilityDef def = AbilityCatalog.get(id);
        Ability ability = REGISTRY.get(id);
        if (def == null || ability == null || player.isSpectator()) {
            return false;
        }
        if (!PowerManager.CONFIG.enabled(id)) {
            Compat.overlay(player, Component.translatable("message.elementals.disabled", def.name).withStyle(ChatFormatting.RED));
            return false;
        }
        PowerProfile profile = PowerManager.profile(player);
        StatSheet sheet = PowerManager.sheet(player);
        ServerLevel level = Compat.serverLevel(player);
        long time = level.getGameTime();
        if (profile.active("gcd", time)) {
            return false;
        }
        long ready = profile.cooldownReady(id);
        if (ready > time) {
            Compat.overlay(player, Component.translatable("message.elementals.cooldown", def.name, String.format(java.util.Locale.ROOT, "%.1f", (ready - time) / 20.0)).withStyle(ChatFormatting.GRAY));
            return false;
        }
        boolean conduit = sheet.has(Keystone.CONDUIT) && (profile.castCount + 1) % 4 == 0;
        double cost = conduit ? 0 : PowerManager.CONFIG.param(id, "cost");
        boolean bloodPact = sheet.has(Keystone.BLOOD_PACT);
        boolean creative = player.getAbilities().instabuild;
        if (!creative) {
            if (bloodPact) {
                if (player.getHealth() <= cost / 10.0 + 1) {
                    Compat.overlay(player, Component.translatable("message.elementals.no_health").withStyle(ChatFormatting.RED));
                    return false;
                }
            } else if (profile.essence < cost) {
                Compat.overlay(player, Component.translatable("message.elementals.no_essence", def.name).withStyle(ChatFormatting.AQUA));
                return false;
            }
        }
        AbilityContext ctx = new AbilityContext(player, def, conduit);
        if (!ability.cast(ctx)) {
            Compat.overlay(player, Component.translatable("message.elementals.no_target").withStyle(ChatFormatting.GRAY));
            return false;
        }
        if (!creative) {
            if (bloodPact) {
                player.setHealth((float) Math.max(1, player.getHealth() - cost / 10.0));
            } else {
                profile.essence = Math.max(0, profile.essence - cost);
            }
        }
        double cooldown = PowerManager.CONFIG.param(id, "cooldown") * sheet.cooldownMultiplier(PowerManager.CONFIG.maxCooldownReduction);
        profile.cooldowns.put(id, time + Math.max(1, Math.round(cooldown * 20)));
        profile.timers.put("gcd", time + PowerManager.CONFIG.globalCooldownTicks);
        profile.castCount++;
        if (conduit) {
            Fx.at(level, ParticleTypes.ELECTRIC_SPARK, player.position().add(0, 1, 0), 30, 0.6, 0.2);
            Fx.sound(level, player.position(), "minecraft:block.beacon.power_select", 0.6f, 1.8f);
        }
        if (PowerManager.CONFIG.xpPerCast > 0 && !"replace".equals(PowerManager.CONFIG.effectiveRasMode(com.naizo.elementals.integration.RasBridge.isLoaded()))) {
            PowerManager.addXp(player, PowerManager.CONFIG.xpPerCast);
        } else {
            PowerManager.sendProfile(player);
        }
        PowerManager.essenceChanged(player);
        return true;
    }

    /** Per-tick upkeep for buffs started by abilities. */
    public static void tickPlayer(ServerPlayer player, PowerProfile profile, StatSheet sheet, long time) {
        ServerLevel level = Compat.serverLevel(player);
        if (profile.timers.isEmpty()) {
            return;
        }
        Vec3 center = player.position().add(0, 1, 0);
        if (profile.active("ember_shield", time) && time % 3 == 0) {
            Fx.ring(level, ParticleTypes.FLAME, player.position().add(0, 0.2 + (time % 20) / 10.0, 0), 0.9, 6, 0);
        }
        if (profile.active("static_field", time) && time % 4 == 0) {
            Fx.at(level, ParticleTypes.ELECTRIC_SPARK, center, 4, 0.8, 0.1);
        }
        if (profile.active("eclipse", time) && time % 4 == 0) {
            Fx.at(level, ParticleTypes.SQUID_INK, center, 2, 0.4, 0.01);
        }
        if (profile.active("rebirth", time) && time % 6 == 0) {
            Fx.at(level, ParticleTypes.HAPPY_VILLAGER, center, 2, 0.5, 0);
        }
        expire(player, profile, time, "stone_skin", "stone_skin");
        expire(player, profile, time, "mountains_wrath", "mountains_wrath");
    }

    private static void expire(ServerPlayer player, PowerProfile profile, long time, String timer, String modifier) {
        Long end = profile.timers.get(timer);
        if (end != null && end <= time) {
            profile.timers.remove(timer);
            StatApplier.tempModifier(player, modifier, "knockback_resistance", 0, false);
        }
    }
}
