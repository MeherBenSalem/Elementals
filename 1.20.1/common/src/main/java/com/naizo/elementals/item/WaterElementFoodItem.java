package com.naizo.elementals.item;

import com.naizo.elementals.spell.WaterAndFoodSpells;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class WaterElementFoodItem extends Item {
    public WaterElementFoodItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).food(new FoodProperties.Builder().nutrition(4).saturationMod(0.3f).alwaysEat().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        WaterAndFoodSpells.consumeWaterSeed(level, entity);
        return result;
    }
}
