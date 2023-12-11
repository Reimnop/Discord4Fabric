package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface OnMessageBroadcastCallback {

    Event<OnMessageBroadcastCallback> EVENT = EventFactory.createArrayBacked(OnMessageBroadcastCallback.class, (listeners) -> ((message, sender) -> {
        for(OnMessageBroadcastCallback listener : listeners){
            listener.onMessageBroadcast(message, sender);
        }
    }));

    void onMessageBroadcast(SignedMessage message, @Nullable ServerPlayerEntity sender);

}
