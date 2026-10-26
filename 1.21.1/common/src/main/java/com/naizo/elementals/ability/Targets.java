package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/** Who an ability may hit, and where the player is aiming. */
public final class Targets {
    private Targets() {
    }

    /** Anything the player could deliberately aim at. */
    public static boolean canHit(ServerPlayer player, Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity == player || !living.isAlive() || entity.isSpectator()) {
            return false;
        }
        if (Compat.isArmorStand(entity) || player.isAlliedTo(entity)) {
            return false;
        }
        if (entity instanceof Player other) {
            return PowerManager.CONFIG.abilitiesHurtPlayers && !other.isCreative() && player.canHarmPlayer(other);
        }
        if (Compat.ownedBy(entity, player)) {
            return PowerManager.CONFIG.abilitiesHurtTamed;
        }
        return true;
    }

    /** Area abilities only hit hostile creatures, things fighting the player, and the player's last target. */
    public static boolean isEnemy(ServerPlayer player, Entity entity) {
        if (!canHit(player, entity)) {
            return false;
        }
        if (entity instanceof Player) {
            return true;
        }
        if (entity instanceof Enemy) {
            return true;
        }
        if (entity instanceof Mob mob && mob.getTarget() == player) {
            return true;
        }
        if (player.getLastHurtMob() == entity || player.getLastHurtByMob() == entity) {
            return true;
        }
        return !Compat.isNpc(entity) && entity instanceof Mob mob && mob.getTarget() instanceof Player;
    }

    public static List<LivingEntity> enemies(ServerPlayer player, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> isEnemy(player, e) && e.getBoundingBox().getCenter().distanceTo(center) <= radius + e.getBbWidth() / 2)
                .stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(center))).toList();
    }

    /** The player and friendly players and pets nearby. */
    public static List<LivingEntity> allies(ServerPlayer player, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive()
                && e.distanceToSqr(center) <= radius * radius
                && (e == player || (e instanceof Player other && !other.isSpectator() && (!PowerManager.CONFIG.abilitiesHurtPlayers || player.isAlliedTo(other)))
                || Compat.ownedBy(e, player)));
    }

    /** The living thing under the crosshair within range, or null. Blocks stop the search. */
    public static LivingEntity aimed(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box, e -> canHit(player, e) && e.isPickable(), eye.distanceToSqr(end));
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        // Forgiving aim: the closest enemy near the line of sight.
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, box.inflate(1.5), e -> isEnemy(player, e))) {
            Vec3 to = candidate.getBoundingBox().getCenter().subtract(eye);
            double along = to.dot(look);
            if (along <= 0 || along > eye.distanceTo(end) + 0.5) {
                continue;
            }
            double off = to.subtract(look.scale(along)).length();
            if (off < 1.2 + candidate.getBbWidth() / 2 && off < bestScore) {
                bestScore = off;
                best = candidate;
            }
        }
        return best;
    }

    /** The block surface the player is looking at within range, or the point at full range dropped to the ground. */
    public static Vec3 aimedPoint(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        BlockHitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 point = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        return ground(player, point);
    }

    /** Drops a point straight down onto the first solid surface (up to 24 blocks). */
    public static Vec3 ground(ServerPlayer player, Vec3 point) {
        BlockHitResult down = player.level().clip(new ClipContext(point.add(0, 0.5, 0), point.add(0, -24, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return down.getType() == HitResult.Type.MISS ? point : down.getLocation();
    }

    /** Furthest free spot along the look direction, up to range, where the player fits. */
    public static Vec3 dashTarget(ServerPlayer player, double range) {
        Vec3 look = player.getLookAngle();
        Vec3 dir = new Vec3(look.x, Math.max(-0.2, Math.min(0.35, look.y)), look.z).normalize();
        Vec3 start = player.position();
        Vec3 best = start;
        for (double d = 0.5; d <= range; d += 0.5) {
            Vec3 candidate = start.add(dir.scale(d));
            AABB moved = player.getBoundingBox().move(candidate.subtract(start));
            if (!player.level().noCollision(player, moved)) {
                break;
            }
            best = candidate;
        }
        return best;
    }
}
