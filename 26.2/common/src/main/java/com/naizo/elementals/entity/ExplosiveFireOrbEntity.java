package com.naizo.elementals.entity;

import com.naizo.elementals.registry.ModEntities;
import com.naizo.elementals.spell.FireSpells;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class ExplosiveFireOrbEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(Items.FIRE_CHARGE);

    public ExplosiveFireOrbEntity(EntityType<? extends ExplosiveFireOrbEntity> type, Level level) {
        super(type, level);
    }

    public ExplosiveFireOrbEntity(EntityType<? extends ExplosiveFireOrbEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level, PROJECTILE_ITEM, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getPickupItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        FireSpells.explode(level(), hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        FireSpells.projectileFlame(level(), getX(), getY(), getZ());
        if (isInGround()) {
            discard();
        }
    }

    public static ExplosiveFireOrbEntity shoot(Level level, LivingEntity owner, RandomSource random) {
        ExplosiveFireOrbEntity arrow = new ExplosiveFireOrbEntity(ModEntities.EXPLOSIVE_FIRE_ORB.get(), owner, level);
        arrow.shoot(owner.getViewVector(1).x, owner.getViewVector(1).y, owner.getViewVector(1).z, 2, 0);
        arrow.setSilent(true);
        arrow.setCritArrow(false);
        arrow.setBaseDamage(5);
        level.addFreshEntity(arrow);
        level.playSound(null, owner.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1, 1F / (random.nextFloat() * 0.5F + 1) + 0.5F);
        return arrow;
    }
}
