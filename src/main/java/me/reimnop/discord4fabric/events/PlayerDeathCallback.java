package me.reimnop.discord4fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class, (listeners) -> (player, source) -> {
        for (PlayerDeathCallback listener : listeners) {
            listener.onPlayerDeath(player, source);
        }
    });

    void onPlayerDeath(ServerPlayerEntity playerEntity, DamageSource source);
}
