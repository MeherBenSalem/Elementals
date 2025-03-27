
package com.naizo.elementals.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

import com.naizo.elementals.procedures.FireElementFoodPlayerFinishesUsingItemProcedure;

public class FireElementFoodItem extends Item {
	public FireElementFoodItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.3f).alwaysEat().build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		FireElementFoodPlayerFinishesUsingItemProcedure.execute(entity);
		return retval;
	}
}
