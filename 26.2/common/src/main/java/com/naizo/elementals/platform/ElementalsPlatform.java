package com.naizo.elementals.platform;

import com.naizo.elementals.data.ElementalPlayerData;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

/** What the shared code needs from each mod loader. */
public interface ElementalsPlatform {
    Path configDirectory();

    boolean isModLoaded(String modId);

    /** Sends a JSON message to one player. */
    void sendToPlayer(ServerPlayer player, String message);

    /** Sends a JSON message from the client to the server. Client side only. */
    void sendToServer(String message);

    /** Data saved by Elementals 1.x, or null when there is none. */
    default ElementalPlayerData legacyData(ServerPlayer player) {
        return null;
    }
}
