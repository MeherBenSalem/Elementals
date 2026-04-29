package com.naizo.elementals.item;

import com.naizo.elementals.spell.FireSpells;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class EmberFireShieldSpellBookItem extends Item {
    public EmberFireShieldSpellBookItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.elementals.ember_fire_shield_spell_book.description_0"));
        lines.add(Component.translatable("item.elementals.ember_fire_shield_spell_book.description_1"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        FireSpells.castEmberShield(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
