package com.naizo.elementals.item;

import com.naizo.elementals.ability.AbilityRunner;
import com.naizo.elementals.core.AbilityCatalog;
import com.naizo.elementals.core.AbilityDef;
import com.naizo.elementals.core.Stat;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.List;
import java.util.function.IntSupplier;

/** The 1.x spell books: cast a Pyromancy ability without the tree node, for Pyromancy origins. */
public class SpellBookItem extends PowerItem {
    private final String ability;
    private final IntSupplier requiredLevel;

    public SpellBookItem(String ability, IntSupplier requiredLevel, Properties properties) {
        super(properties.stacksTo(1).fireResistant().rarity(Rarity.UNCOMMON));
        this.ability = ability;
        this.requiredLevel = requiredLevel;
    }

    @Override
    protected boolean onUse(ServerPlayer player, ItemStack stack) {
        if (AbilityRunner.castFromBook(player, ability, requiredLevel.getAsInt())) {
            cooldown(player, stack, 20);
            return true;
        }
        return false;
    }

    @Override
    protected void tooltip(List<Component> lines) {
        AbilityDef def = AbilityCatalog.get(ability);
        lines.add(Component.literal(def.name).withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal(def.describe(PowerManager.CONFIG.params(ability))).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("item.elementals.spell_book.requires", requiredLevel.getAsInt()).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("item.elementals.spell_book.cost", Stat.trim(PowerManager.CONFIG.param(ability, "cost"), 1),
                Stat.trim(PowerManager.CONFIG.param(ability, "cooldown"), 1)).withStyle(ChatFormatting.DARK_AQUA));
    }
}
