package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface PlayerChatReceivedCallback {
    Event<PlayerChatReceivedCallback> EVENT = EventFactory.createArrayBacked(PlayerChatReceivedCallback.class, (listeners) -> (player, message) -> {
        for (PlayerChatReceivedCallback listener : listeners) {
            listener.onMessageReceived(player, message);
        }
    });

    void onMessageReceived(ServerPlayerEntity playerEntity, String message);
}
