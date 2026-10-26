package com.naizo.elementals.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class RuneItem extends Item {
    private final String source;

    public RuneItem(String source, boolean fireResistant, Properties properties) {
        super(fireResistant ? properties.fireResistant().rarity(Rarity.UNCOMMON) : properties.rarity(Rarity.UNCOMMON));
        this.source = source;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("item.elementals.rune.source." + source).withStyle(ChatFormatting.DARK_GRAY));
    }
}
