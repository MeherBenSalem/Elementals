package com.naizo.elementals.power;

import com.naizo.elementals.Elementals;
import com.naizo.elementals.compat.Compat;
import com.naizo.elementals.core.PowerProfile;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** One JSON file per player in the world folder under remnants_powers/, the same on every loader. */
public final class PowerStorage {
    private PowerStorage() {
    }

    private static Path file(MinecraftServer server, UUID id) {
        return Compat.worldDirectory(server).resolve("remnants_powers").resolve(id + ".json");
    }

    public static PowerProfile load(MinecraftServer server, UUID id) {
        Path file = file(server, id);
        if (Files.exists(file)) {
            try {
                return PowerProfile.fromJson(Files.readString(file, StandardCharsets.UTF_8));
            } catch (IOException | RuntimeException ex) {
                Elementals.LOGGER.error("Could not read skill data {}; the player starts fresh and the old file is kept as .broken", file, ex);
                try {
                    Files.move(file, file.resolveSibling(id + ".json.broken"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                    // keep going with a fresh profile
                }
            }
        }
        return null;
    }

    public static void save(MinecraftServer server, UUID id, PowerProfile profile) {
        Path file = file(server, id);
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(id + ".json.tmp");
            Files.writeString(temp, profile.toJsonString(), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            try {
                Files.writeString(file, profile.toJsonString(), StandardCharsets.UTF_8);
            } catch (IOException second) {
                Elementals.LOGGER.error("Could not save skill data {}", file, second);
            }
        }
    }
}
