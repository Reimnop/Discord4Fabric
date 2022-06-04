package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.Advancement;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerAdvancementCallback {
    Event<PlayerAdvancementCallback> EVENT = EventFactory.createArrayBacked(PlayerAdvancementCallback.class, (listeners) -> (player, advancement) -> {
        for (PlayerAdvancementCallback listener : listeners) {
            listener.onAdvancementGranted(player, advancement);
        }
    });

    void onAdvancementGranted(ServerPlayerEntity playerEntity, Advancement advancement);
}
