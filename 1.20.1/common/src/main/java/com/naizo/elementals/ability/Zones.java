package com.naizo.elementals.ability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Effects that play out over several ticks: rifts, storms, waves, delayed impacts. */
public final class Zones {
    @FunctionalInterface
    public interface Zone {
        /** Called every server tick with the number of ticks since the zone started. Return false to end it. */
        boolean tick(int age);
    }

    private static final class Running {
        final Zone zone;
        int age;

        Running(Zone zone) {
            this.zone = zone;
        }
    }

    private static final List<Running> ACTIVE = new ArrayList<>();
    private static final List<Running> PENDING = new ArrayList<>();

    private Zones() {
    }

    public static void add(Zone zone) {
        PENDING.add(new Running(zone));
    }

    public static void tick() {
        ACTIVE.addAll(PENDING);
        PENDING.clear();
        Iterator<Running> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Running running = iterator.next();
            boolean keep;
            try {
                keep = running.zone.tick(running.age++);
            } catch (RuntimeException ex) {
                com.naizo.elementals.Elementals.LOGGER.warn("An ability effect failed and was stopped", ex);
                keep = false;
            }
            if (!keep) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        ACTIVE.clear();
        PENDING.clear();
    }
}
