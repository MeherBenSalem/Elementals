package com.naizo.elementals.core;

import java.util.Locale;

public enum NodeType {
    START(14),
    MINOR(9),
    NOTABLE(14),
    ABILITY(15),
    KEYSTONE(19);

    /** Half size of the node frame in tree units at zoom 1. */
    public final int radius;

    NodeType(int radius) {
        this.radius = radius;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static NodeType byId(String id) {
        if (id != null) {
            for (NodeType type : values()) {
                if (type.id().equalsIgnoreCase(id)) {
                    return type;
                }
            }
        }
        return MINOR;
    }
}
