package com.naizo.elementals.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Base for right-click items. Keeps the version-specific Item API in one place. Minecraft 1.21.1 edition. */
public abstract class PowerItem extends Item {
    protected PowerItem(Properties properties) {
        super(properties);
    }

    /** Runs on the server. Return true when the use did something. */
    protected abstract boolean onUse(ServerPlayer player, ItemStack stack);

    protected abstract void tooltip(List<Component> lines);

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            return onUse(serverPlayer, stack) ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        tooltip(lines);
    }

    protected static void cooldown(Player player, ItemStack stack, int ticks) {
        player.getCooldowns().addCooldown(stack.getItem(), ticks);
    }

    protected static void consume(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
