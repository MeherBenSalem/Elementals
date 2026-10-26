package com.naizo.elementals.fabric.mixin;

import com.naizo.elementals.power.CombatHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fabric has no event that changes damage amounts, so damage reduction and procs hook in here. */
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float elementals$modifyDamage(float amount, ServerLevel level, DamageSource source) {
        return CombatHooks.modifyDamage((LivingEntity) (Object) this, source, amount);
    }
}
