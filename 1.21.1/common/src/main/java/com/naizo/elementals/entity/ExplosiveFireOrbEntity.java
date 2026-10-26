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

/** The Explosive Orb. Bursts on contact without breaking blocks. */
public class ExplosiveFireOrbEntity extends AbstractArrow implements ItemSupplier {
    private static final ItemStack ITEM = new ItemStack(Items.FIRE_CHARGE);
    public boolean empowered;

    public ExplosiveFireOrbEntity(EntityType<? extends ExplosiveFireOrbEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public ExplosiveFireOrbEntity(EntityType<? extends ExplosiveFireOrbEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level, ITEM.copy(), null);
        setNoGravity(true);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    public ItemStack getItem() {
        return ITEM;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    private void burst(Vec3 at) {
        if (level() instanceof ServerLevel server && getOwner() instanceof ServerPlayer owner) {
            PyromancyAbilities.orbBurst(owner, server, at, empowered);
        }
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        burst(hit.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        burst(hit.getLocation());
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            Vec3 p = position();
            Fx.at(server, ParticleTypes.FLAME, p, 4, 0.15, 0.01);
            Fx.at(server, ParticleTypes.LAVA, p, 1, 0.1, 0);
            Fx.at(server, ParticleTypes.SMOKE, p, 1, 0.1, 0.01);
        }
        if (tickCount > 80) {
            burst(position());
        }
    }

    public static void shoot(ServerPlayer owner, float speed, boolean empowered) {
        ExplosiveFireOrbEntity orb = new ExplosiveFireOrbEntity(ModEntities.EXPLOSIVE_FIRE_ORB.get(), owner, owner.level());
        Vec3 look = owner.getLookAngle();
        orb.setPos(owner.getX(), owner.getEyeY() - 0.2, owner.getZ());
        orb.shoot(look.x, look.y, look.z, speed, 0);
        orb.setBaseDamage(0);
        orb.setSilent(true);
        orb.empowered = empowered;
        owner.level().addFreshEntity(orb);
    }
}
