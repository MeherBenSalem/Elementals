package com.naizo.elementals.item;

import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.PowerProfile;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.List;

/** Grants skill points, up to a configured number of tomes per player. */
public class RemnantTomeItem extends PowerItem {
    public RemnantTomeItem(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.RARE));
    }

    @Override
    protected boolean onUse(ServerPlayer player, ItemStack stack) {
        PowerProfile profile = PowerManager.profile(player);
        if (profile.tomesUsed >= PowerManager.CONFIG.tomeCap) {
            PowerManager.deny(player, "message.elementals.tome_cap", PowerManager.CONFIG.tomeCap);
            return false;
        }
        profile.tomesUsed++;
        profile.bonusPoints += PowerManager.CONFIG.tomePoints;
        consume(player, stack);
        Compat.playSound(player.level(), player.getX(), player.getY(), player.getZ(), "minecraft:item.book.page_turn", 1f, 0.8f);
        Compat.playSound(player.level(), player.getX(), player.getY(), player.getZ(), "minecraft:entity.player.levelup", 0.6f, 1.4f);
        PowerManager.notice(player, "points", Component.translatable(PowerManager.countKey("message.elementals.tome_used", PowerManager.CONFIG.tomePoints), PowerManager.CONFIG.tomePoints,
                PowerManager.CONFIG.tomeCap - profile.tomesUsed).getString());
        PowerManager.sendProfile(player);
        PowerManager.save(player);
        return true;
    }

    @Override
    protected void tooltip(List<Component> lines) {
        lines.add(Component.translatable(PowerManager.countKey("item.elementals.remnant_tome.desc", PowerManager.CONFIG.tomePoints), PowerManager.CONFIG.tomePoints).withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("item.elementals.remnant_tome.cap", PowerManager.CONFIG.tomeCap).withStyle(ChatFormatting.DARK_GRAY));
    }
}
