package me.reimnop.d4f.events;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface DiscordMessageReceivedCallback {
    Event<DiscordMessageReceivedCallback> EVENT = EventFactory.createArrayBacked(DiscordMessageReceivedCallback.class, (listeners) -> (user, message) -> {
        for (DiscordMessageReceivedCallback listener : listeners) {
            listener.onMessageReceived(user, message);
        }
    });

    void onMessageReceived(User user, Message message);
}
