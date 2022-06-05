package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class, (listeners) -> (player, source, deathMessage) -> {
        for (PlayerDeathCallback listener : listeners) {
            listener.onPlayerDeath(player, source, deathMessage);
        }
    });

    void onPlayerDeath(ServerPlayerEntity playerEntity, DamageSource source, Text deathMessage);
}
