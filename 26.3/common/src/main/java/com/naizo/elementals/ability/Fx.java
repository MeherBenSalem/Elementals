package com.naizo.elementals.ability;

import com.naizo.elementals.compat.Compat;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Particle shapes built from vanilla particles, so every client sees them without a resource pack. */
public final class Fx {
    private Fx() {
    }

    public static void at(ServerLevel level, ParticleOptions particle, Vec3 pos, int count, double spread, double speed) {
        level.sendParticles(particle, pos.x, pos.y, pos.z, count, spread, spread, spread, speed);
    }

    public static void at(ServerLevel level, ParticleOptions particle, Vec3 pos, int count, double dx, double dy, double dz, double speed) {
        level.sendParticles(particle, pos.x, pos.y, pos.z, count, dx, dy, dz, speed);
    }

    /** Flat ring on the XZ plane. */
    public static void ring(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int points, double rise) {
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2 * i / points;
            level.sendParticles(particle, center.x + Math.cos(a) * radius, center.y, center.z + Math.sin(a) * radius, 1, 0, rise, 0, rise > 0 ? 0.02 : 0);
        }
    }

    /** Ring whose particles fly outward. */
    public static void burstRing(ServerLevel level, ParticleOptions particle, Vec3 center, int points, double speed) {
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2 * i / points;
            level.sendParticles(particle, center.x, center.y, center.z, 0, Math.cos(a), 0.05, Math.sin(a), speed);
        }
    }

    /** Hollow sphere of particles. */
    public static void sphere(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int points) {
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < points; i++) {
            double y = 1 - (i / (double) (points - 1)) * 2;
            double r = Math.sqrt(1 - y * y);
            double theta = golden * i;
            level.sendParticles(particle, center.x + Math.cos(theta) * r * radius, center.y + y * radius, center.z + Math.sin(theta) * r * radius, 1, 0, 0, 0, 0);
        }
    }

    /** Straight line with one particle every step blocks. */
    public static void line(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to, double step) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1e-3) {
            return;
        }
        Vec3 dir = delta.scale(1 / length);
        for (double d = 0; d <= length; d += step) {
            Vec3 p = from.add(dir.scale(d));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    /** Jagged lightning-style line between two points. */
    public static void arc(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 to, double jitter) {
        Vec3 previous = from;
        int segments = Math.max(3, (int) (from.distanceTo(to) / 1.2));
        for (int i = 1; i <= segments; i++) {
            Vec3 point = from.lerp(to, i / (double) segments);
            if (i < segments) {
                point = point.add((level.getRandom().nextDouble() - 0.5) * jitter, (level.getRandom().nextDouble() - 0.5) * jitter, (level.getRandom().nextDouble() - 0.5) * jitter);
            }
            line(level, particle, previous, point, 0.25);
            previous = point;
        }
    }

    /** Rising spiral around a vertical axis. */
    public static void spiral(ServerLevel level, ParticleOptions particle, Vec3 base, double radius, double height, int points, double turns) {
        for (int i = 0; i < points; i++) {
            double t = i / (double) points;
            double a = t * Math.PI * 2 * turns;
            level.sendParticles(particle, base.x + Math.cos(a) * radius, base.y + t * height, base.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
    }

    /** Filled disk of particles. */
    public static void disk(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double r = radius * Math.sqrt(level.getRandom().nextDouble());
            double a = level.getRandom().nextDouble() * Math.PI * 2;
            level.sendParticles(particle, center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r, 1, 0, 0.05, 0, 0.01);
        }
    }

    public static void sound(ServerLevel level, Vec3 pos, String sound, float volume, float pitch) {
        Compat.playSound(level, pos.x, pos.y, pos.z, sound, volume, pitch);
    }

    public static ParticleOptions dust(int rgb, float size) {
        return Compat.dust(rgb, size);
    }
}
