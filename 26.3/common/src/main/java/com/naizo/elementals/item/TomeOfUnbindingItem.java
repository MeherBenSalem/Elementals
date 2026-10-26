package com.naizo.elementals.item;

import com.naizo.elementals.power.PowerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.List;

/** Refunds the whole tree for free. */
public class TomeOfUnbindingItem extends PowerItem {
    public TomeOfUnbindingItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    protected boolean onUse(ServerPlayer player, ItemStack stack) {
        if (!PowerManager.reset(player, true)) {
            PowerManager.deny(player, "message.elementals.nothing_to_reset");
            return false;
        }
        consume(player, stack);
        return true;
    }

    @Override
    protected void tooltip(List<Component> lines) {
        lines.add(Component.translatable("item.elementals.tome_of_unbinding.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
