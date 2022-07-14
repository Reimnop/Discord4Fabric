package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerConnectedCallback {
    Event<PlayerConnectedCallback> EVENT = EventFactory.createArrayBacked(PlayerConnectedCallback.class, (listeners) -> (player, server, fromVanish) -> {
        for (PlayerConnectedCallback listener : listeners) {
            listener.onConnected(player, server, fromVanish);
        }
    });

    void onConnected(ServerPlayerEntity player, MinecraftServer server, boolean fromVanish);
}
