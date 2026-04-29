package com.naizo.elementals.entity;

import com.naizo.elementals.registry.ModEntities;
import com.naizo.elementals.spell.FireSpells;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class FireSignatureEntity extends AbstractArrow implements ItemSupplier {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);

    public FireSignatureEntity(EntityType<? extends FireSignatureEntity> type, Level level) {
        super(type, level);
    }

    public FireSignatureEntity(EntityType<? extends FireSignatureEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
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
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(Math.max(0, entity.getArrowCount() - 1));
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        FireSpells.burnOnHit(player, getOwner());
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        FireSpells.burnOnHit(hit.getEntity(), getOwner());
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        FireSpells.projectileFlame(level(), getX(), getY(), getZ());
        if (inGround) {
            discard();
        }
    }

    public static FireSignatureEntity shoot(Level level, LivingEntity owner, RandomSource random) {
        FireSignatureEntity arrow = new FireSignatureEntity(ModEntities.FIRE_SIGNATURE.get(), owner, level);
        arrow.shoot(owner.getViewVector(1).x, owner.getViewVector(1).y, owner.getViewVector(1).z, 2, 0);
        arrow.setSilent(true);
        arrow.setCritArrow(false);
        arrow.setBaseDamage(5);
        arrow.setKnockback(5);
        level.addFreshEntity(arrow);
        level.playSound(null, owner.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1, 1F / (random.nextFloat() * 0.5F + 1) + 0.5F);
        return arrow;
    }
}
