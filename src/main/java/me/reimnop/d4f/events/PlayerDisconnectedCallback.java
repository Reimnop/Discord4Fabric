package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDisconnectedCallback {
    Event<PlayerDisconnectedCallback> EVENT = EventFactory.createArrayBacked(PlayerDisconnectedCallback.class, (listeners) -> (player, server, fromVanish) -> {
        for (PlayerDisconnectedCallback listener : listeners) {
            listener.onDisconnected(player, server, fromVanish);
        }
    });

    void onDisconnected(ServerPlayerEntity player, MinecraftServer server, boolean fromVanish);
}
