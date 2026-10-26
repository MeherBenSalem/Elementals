package com.naizo.elementals.power;

import com.naizo.elementals.core.Keystone;
import com.naizo.elementals.core.Stat;
import com.naizo.elementals.core.StatSheet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Turns tree stats into vanilla attribute modifiers. Minecraft 1.20.1 edition (UUID-keyed modifiers). */
public final class StatApplier {
    private record Managed(Attribute attribute, UUID id) {
    }

    private static final List<Managed> MANAGED = new ArrayList<>();

    private StatApplier() {
    }

    private static UUID id(String path) {
        return UUID.nameUUIDFromBytes(("elementals:" + path).getBytes(StandardCharsets.UTF_8));
    }

    public static void apply(LivingEntity entity, StatSheet sheet) {
        clear(entity);
        float healthBefore = entity.getHealth();
        add(entity, Attributes.MAX_HEALTH, "tree/max_health", sheet.get(Stat.MAX_HEALTH), AttributeModifier.Operation.ADDITION);
        add(entity, Attributes.ARMOR, "tree/armor", sheet.get(Stat.ARMOR), AttributeModifier.Operation.ADDITION);
        add(entity, Attributes.ARMOR_TOUGHNESS, "tree/armor_toughness", sheet.get(Stat.ARMOR_TOUGHNESS), AttributeModifier.Operation.ADDITION);
        add(entity, Attributes.ATTACK_DAMAGE, "tree/attack_damage", sheet.get(Stat.ATTACK_DAMAGE) / 100.0, AttributeModifier.Operation.MULTIPLY_BASE);
        add(entity, Attributes.ATTACK_SPEED, "tree/attack_speed", sheet.get(Stat.ATTACK_SPEED) / 100.0, AttributeModifier.Operation.MULTIPLY_BASE);
        add(entity, Attributes.MOVEMENT_SPEED, "tree/movement_speed", sheet.get(Stat.MOVEMENT_SPEED) / 100.0, AttributeModifier.Operation.MULTIPLY_BASE);
        add(entity, Attributes.KNOCKBACK_RESISTANCE, "tree/knockback_resistance", sheet.get(Stat.KNOCKBACK_RESISTANCE) / 100.0, AttributeModifier.Operation.ADDITION);
        add(entity, Attributes.LUCK, "tree/luck", sheet.get(Stat.LUCK), AttributeModifier.Operation.ADDITION);
        if (sheet.has(Keystone.GLASS_CANNON)) {
            add(entity, Attributes.MAX_HEALTH, "keystone/glass_cannon", -0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (sheet.has(Keystone.WINDBORNE)) {
            add(entity, Attributes.MOVEMENT_SPEED, "keystone/windborne_speed", 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(entity, Attributes.ARMOR, "keystone/windborne_armor", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (sheet.has(Keystone.UNYIELDING)) {
            add(entity, Attributes.KNOCKBACK_RESISTANCE, "keystone/unyielding_knockback", 1.0, AttributeModifier.Operation.ADDITION);
            add(entity, Attributes.ARMOR, "keystone/unyielding_armor", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
            add(entity, Attributes.MOVEMENT_SPEED, "keystone/unyielding_speed", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        } else if (healthBefore > entity.getHealth()) {
            entity.setHealth(Math.min(healthBefore, entity.getMaxHealth()));
        }
    }

    /** Temporary modifier used by abilities, for example Stone Skin's knockback immunity. */
    public static void tempModifier(LivingEntity entity, String name, String attribute, double amount, boolean multiply) {
        Attribute target = switch (attribute) {
            case "knockback_resistance" -> Attributes.KNOCKBACK_RESISTANCE;
            case "movement_speed" -> Attributes.MOVEMENT_SPEED;
            case "armor" -> Attributes.ARMOR;
            default -> null;
        };
        if (target == null) {
            return;
        }
        AttributeInstance instance = entity.getAttribute(target);
        if (instance == null) {
            return;
        }
        UUID id = id("buff/" + name);
        instance.removeModifier(id);
        if (amount != 0) {
            instance.addTransientModifier(new AttributeModifier(id, "elementals:buff/" + name, amount,
                    multiply ? AttributeModifier.Operation.MULTIPLY_TOTAL : AttributeModifier.Operation.ADDITION));
        }
    }

    private static void add(LivingEntity entity, Attribute attribute, String path, double amount, AttributeModifier.Operation operation) {
        UUID id = id(path);
        Managed managed = new Managed(attribute, id);
        if (!MANAGED.contains(managed)) {
            MANAGED.add(managed);
        }
        if (amount == 0) {
            return;
        }
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, "elementals:" + path, amount, operation));
        }
    }

    public static void clear(LivingEntity entity) {
        for (Managed managed : MANAGED) {
            AttributeInstance instance = entity.getAttribute(managed.attribute());
            if (instance != null) {
                instance.removeModifier(managed.id());
            }
        }
    }
}
