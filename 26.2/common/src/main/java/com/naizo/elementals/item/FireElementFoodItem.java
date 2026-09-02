package com.naizo.elementals.item;

import com.naizo.elementals.spell.WaterAndFoodSpells;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class FireElementFoodItem extends Item {
    public FireElementFoodItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).alwaysEdible().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        WaterAndFoodSpells.consumeFireSeed(level, entity);
        return result;
    }
}
