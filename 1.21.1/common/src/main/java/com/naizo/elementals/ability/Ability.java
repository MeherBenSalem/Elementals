package com.naizo.elementals.ability;

/** Server behavior of one ability. Returns false when there was nothing to cast at, so nothing is spent. */
@FunctionalInterface
public interface Ability {
    boolean cast(AbilityContext ctx);
}
