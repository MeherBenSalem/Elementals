package com.naizo.elementals.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class FireRuneItem extends Item {
    public FireRuneItem() {
        super(new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.UNCOMMON));
    }
}
