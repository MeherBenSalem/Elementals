package com.naizo.elementals.item;

import com.naizo.elementals.spell.FireSpells;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class InfernoWaveSpellBookItem extends Item {
    public InfernoWaveSpellBookItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("item.elementals.inferno_wave_spell_book.description_0"));
        tooltipAdder.accept(Component.translatable("item.elementals.inferno_wave_spell_book.description_1"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        FireSpells.castInfernoWave(level, player, stack);
        return InteractionResult.SUCCESS;
    }
}
