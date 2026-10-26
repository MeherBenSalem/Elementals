package com.naizo.elementals.item;

import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.School;
import com.naizo.elementals.power.PowerManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/** Eating a seed awakens its school: the origin node is allocated for free. Minecraft 26.2 and 26.3 edition. */
public class ElementSeedItem extends Item {
    private final School school;

    public ElementSeedItem(School school, boolean fireResistant, Properties properties) {
        super(withFire(properties.stacksTo(16).rarity(Rarity.EPIC)
                .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).alwaysEdible().build()), fireResistant));
        this.school = school;
    }

    private static Properties withFire(Properties properties, boolean fireResistant) {
        return fireResistant ? properties.fireResistant() : properties;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer player) {
            if (PowerManager.grantOrigin(PowerManager.profile(player), school)) {
                PowerManager.refresh(player);
                PowerManager.sendProfile(player);
                PowerManager.save(player);
                Compat.playSound(level, player.getX(), player.getY(), player.getZ(), "minecraft:block.beacon.power_select", 1f, 1.2f);
                PowerManager.notice(player, "origin", Component.translatable("message.elementals.origin_awakened", school.displayName).getString());
            } else {
                PowerManager.deny(player, "message.elementals.origin_taken");
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("item.elementals.seed.desc", school.displayName).withStyle(ChatFormatting.GRAY));
    }
}
