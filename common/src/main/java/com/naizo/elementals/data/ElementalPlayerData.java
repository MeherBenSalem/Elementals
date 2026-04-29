package com.naizo.elementals.data;

import net.minecraft.nbt.CompoundTag;

public class ElementalPlayerData {
    public double element;
    public double elementalLevel;
    public double elementalExp;
    public double cooldown;

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("element", element);
        tag.putDouble("elementalLevel", elementalLevel);
        tag.putDouble("elementalExp", elementalExp);
        tag.putDouble("cooldown", cooldown);
        return tag;
    }

    public void load(CompoundTag tag) {
        element = tag.getDouble("element");
        elementalLevel = tag.getDouble("elementalLevel");
        elementalExp = tag.getDouble("elementalExp");
        cooldown = tag.getDouble("cooldown");
    }

    public void copyFrom(ElementalPlayerData other) {
        element = other.element;
        elementalLevel = other.elementalLevel;
        elementalExp = other.elementalExp;
        cooldown = other.cooldown;
    }
}
