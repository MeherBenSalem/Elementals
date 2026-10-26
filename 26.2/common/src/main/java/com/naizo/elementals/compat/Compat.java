package com.naizo.elementals.compat;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Every Minecraft call that differs between the supported versions lives here, so the rest of the
 * shared code is identical in each version folder. This file is the Minecraft 26.2 and 26.3 edition.
 */
public final class Compat {
    private Compat() {
    }

    public static Identifier id(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier parse(String id) {
        Identifier parsed = Identifier.tryParse(id);
        return parsed == null ? Identifier.withDefaultNamespace("air") : parsed;
    }

    public static Item item(String id) {
        return BuiltInRegistries.ITEM.getOptional(parse(id)).orElse(Items.AIR);
    }

    public static ItemStack stack(String id) {
        Item item = item(id);
        return new ItemStack(item == Items.AIR ? Items.BARRIER : item);
    }

    private static Optional<Holder.Reference<MobEffect>> effectHolder(String id) {
        return BuiltInRegistries.MOB_EFFECT.get(parse(id));
    }

    public static MobEffectInstance effect(String id, int ticks, int amplifier, boolean visible) {
        return effectHolder(id).map(h -> new MobEffectInstance(h, ticks, amplifier, false, visible, true)).orElse(null);
    }

    public static void addEffect(LivingEntity entity, String id, int ticks, int amplifier, boolean visible) {
        MobEffectInstance instance = effect(id, ticks, amplifier, visible);
        if (instance != null) {
            entity.addEffect(instance);
        }
    }

    public static boolean hasEffect(LivingEntity entity, String id) {
        return effectHolder(id).map(entity::hasEffect).orElse(false);
    }

    public static void removeEffect(LivingEntity entity, String id) {
        effectHolder(id).ifPresent(entity::removeEffect);
    }

    public static void cleanse(LivingEntity entity) {
        entity.getActiveEffects().stream()
                .filter(e -> !e.getEffect().value().isBeneficial())
                .map(MobEffectInstance::getEffect)
                .toList()
                .forEach(entity::removeEffect);
    }

    public static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.getValue(parse(id));
    }

    public static void playSound(Level level, double x, double y, double z, String id, float volume, float pitch) {
        SoundEvent sound = sound(id);
        if (sound != null) {
            level.playSound(null, x, y, z, sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    public static DamageSource abilityDamage(Player player) {
        return player.damageSources().indirectMagic(player, player);
    }

    public static void resetInvulnerability(LivingEntity target) {
        target.invulnerableTime = 0;
    }

    public static boolean hurt(LivingEntity target, DamageSource source, float amount) {
        return target.level() instanceof ServerLevel level && target.hurtServer(level, source, amount);
    }

    public static boolean isFall(DamageSource source) {
        return source.is(DamageTypeTags.IS_FALL);
    }

    public static boolean isBurning(DamageSource source) {
        return source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE);
    }

    public static boolean isBypassingInvulnerability(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    public static void ignite(Entity entity, float seconds) {
        entity.igniteForSeconds(seconds);
    }

    public static void overlay(ServerPlayer player, Component message) {
        player.sendOverlayMessage(message);
    }

    /** Text that copies a value to the clipboard when clicked. */
    public static Component copyable(String text, String value) {
        return Component.literal(text).withStyle(style -> style
                .withClickEvent(new ClickEvent.CopyToClipboard(value))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy")))
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true));
    }

    public static ParticleOptions dust(int rgb, float size) {
        return new DustParticleOptions(rgb & 0xFFFFFF, size);
    }

    public static ParticleOptions block(BlockState state) {
        return new BlockParticleOption(ParticleTypes.BLOCK, state);
    }

    public static ParticleOptions flash() {
        return ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFFFF);
    }

    public static ServerLevel serverLevel(Entity entity) {
        return entity.level() instanceof ServerLevel server ? server : null;
    }

    public static MinecraftServer server(ServerPlayer player) {
        return player.level().getServer();
    }

    public static Path worldDirectory(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT);
    }

    public static void visualLightning(ServerLevel level, double x, double y, double z) {
        LightningBolt bolt = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(x, y, z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }

    public static void teleport(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(x, y, z);
        player.resetFallDistance();
    }

    public static void teleportFacing(ServerPlayer player, double x, double y, double z, float yaw, float pitch) {
        player.teleportTo(player.level(), x, y, z, Set.<Relative>of(), yaw, pitch, true);
        player.resetFallDistance();
    }

    public static void push(Entity entity, Vec3 velocity) {
        entity.setDeltaMovement(velocity);
        entity.hurtMarked = true;
    }

    public static boolean isUndead(LivingEntity entity) {
        return entity.is(EntityTypeTags.UNDEAD);
    }

    public static boolean hasPermission(CommandSourceStack source, int level) {
        Permission permission = switch (level) {
            case 0, 1 -> Permissions.COMMANDS_MODERATOR;
            case 2 -> Permissions.COMMANDS_GAMEMASTER;
            case 3 -> Permissions.COMMANDS_ADMIN;
            default -> Permissions.COMMANDS_OWNER;
        };
        return source.permissions().hasPermission(permission);
    }

    public static String itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static String entityTypeId(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    public static void setFrozen(LivingEntity entity, int ticks) {
        entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), ticks));
    }

    public static float yRot(Entity entity) {
        return entity.getYRot();
    }

    public static boolean isNpc(Entity entity) {
        return entity instanceof net.minecraft.world.entity.npc.Npc;
    }

    public static boolean isArmorStand(Entity entity) {
        return entity instanceof net.minecraft.world.entity.decoration.ArmorStand;
    }

    public static boolean ownedBy(Entity entity, Player player) {
        return entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null
                && ownable.getOwnerReference().getUUID().equals(player.getUUID());
    }

    public static boolean isMeleeAttack(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK);
    }

    public static boolean isThorns(DamageSource source) {
        return source.is(DamageTypes.THORNS);
    }

    public static DamageSource thorns(Entity from) {
        return from.damageSources().thorns(from);
    }

    public static boolean isBlaze(Entity entity) {
        return entity instanceof net.minecraft.world.entity.monster.Blaze;
    }

    public static boolean isDrownedOrGuardian(Entity entity) {
        return entity instanceof net.minecraft.world.entity.monster.zombie.Drowned || entity instanceof net.minecraft.world.entity.monster.Guardian;
    }

    public static void drop(LivingEntity entity, ItemLike item) {
        if (entity.level() instanceof ServerLevel level) {
            entity.spawnAtLocation(level, item);
        }
    }

    public static BlockState blockBelow(Level level, BlockPos pos) {
        return level.getBlockState(pos.below());
    }
}
