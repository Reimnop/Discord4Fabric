package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface OnPlayerUnvanishCallback {
    Event<OnPlayerUnvanishCallback> EVENT = EventFactory.createArrayBacked(OnPlayerUnvanishCallback.class, (listeners) -> (player) -> {
        for (OnPlayerUnvanishCallback listener : listeners) {
            listener.onUnvanish(player);
        }
    });

    void onUnvanish(ServerPlayerEntity player);
}
