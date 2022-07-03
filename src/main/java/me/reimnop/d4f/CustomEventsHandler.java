package me.reimnop.d4f;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class CustomEventsHandler {
    private CustomEventsHandler() {}

    public static void init(CustomEvents customEvents) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            customEvents.raiseEvent(CustomEvents.PLAYER_JOIN);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            customEvents.raiseEvent(CustomEvents.PLAYER_LEAVE);
        });
    }
}
