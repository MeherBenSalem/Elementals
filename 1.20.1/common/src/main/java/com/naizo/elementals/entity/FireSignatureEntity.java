package com.naizo.elementals.entity;

import com.naizo.elementals.ability.Fx;
import com.naizo.elementals.ability.PyromancyAbilities;
import com.naizo.elementals.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** The Ember Bolt. Drawn only with particles. */
public class FireSignatureEntity extends AbstractArrow implements ItemSupplier {
    private static final ItemStack ITEM = new ItemStack(Items.FIRE_CHARGE);
    public boolean empowered;

    public FireSignatureEntity(EntityType<? extends FireSignatureEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public FireSignatureEntity(EntityType<? extends FireSignatureEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level);
        setNoGravity(true);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    public ItemStack getItem() {
        return ITEM;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide() && getOwner() instanceof ServerPlayer owner && hit.getEntity() instanceof LivingEntity target) {
            PyromancyAbilities.emberBoltHit(owner, target, empowered);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (level() instanceof ServerLevel server) {
            Fx.at(server, ParticleTypes.FLAME, hit.getLocation(), 10, 0.15, 0.05);
            Fx.at(server, ParticleTypes.SMOKE, hit.getLocation(), 4, 0.1, 0.02);
        }
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            Vec3 p = position();
            Fx.at(server, ParticleTypes.FLAME, p, 3, 0.06, 0.01);
            Fx.at(server, Fx.dust(0xFF8A3D, 1.2f), p, 2, 0.05, 0);
            if (tickCount % 2 == 0) {
                Fx.at(server, ParticleTypes.SMALL_FLAME, p, 1, 0.1, 0.01);
            }
        }
        if (tickCount > 60) {
            discard();
        }
    }

    public static void shoot(ServerPlayer owner, float speed, boolean empowered) {
        FireSignatureEntity bolt = new FireSignatureEntity(ModEntities.FIRE_SIGNATURE.get(), owner, owner.level());
        Vec3 look = owner.getLookAngle();
        bolt.setPos(owner.getX(), owner.getEyeY() - 0.15, owner.getZ());
        bolt.shoot(look.x, look.y, look.z, speed, 0);
        bolt.setBaseDamage(0);
        bolt.setSilent(true);
        bolt.empowered = empowered;
        owner.level().addFreshEntity(bolt);
    }
}
