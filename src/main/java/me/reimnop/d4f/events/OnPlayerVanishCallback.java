package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface OnPlayerVanishCallback {
    Event<OnPlayerVanishCallback> EVENT = EventFactory.createArrayBacked(OnPlayerVanishCallback.class, (listeners) -> (player) -> {
        for (OnPlayerVanishCallback listener : listeners) {
            listener.onVanish(player);
        }
    });

    void onVanish(ServerPlayerEntity player);
}
