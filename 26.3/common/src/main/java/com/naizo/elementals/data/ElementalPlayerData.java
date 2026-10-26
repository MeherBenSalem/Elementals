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
        element = tag.getDouble("element").orElse(0.0);
        elementalLevel = tag.getDouble("elementalLevel").orElse(0.0);
        elementalExp = tag.getDouble("elementalExp").orElse(0.0);
        cooldown = tag.getDouble("cooldown").orElse(0.0);
    }

    public void copyFrom(ElementalPlayerData other) {
        element = other.element;
        elementalLevel = other.elementalLevel;
        elementalExp = other.elementalExp;
        cooldown = other.cooldown;
    }
}
