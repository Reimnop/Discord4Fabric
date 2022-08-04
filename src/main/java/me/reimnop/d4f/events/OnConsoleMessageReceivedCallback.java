package me.reimnop.d4f.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.LogEvent;

public interface OnConsoleMessageReceivedCallback {
    Event<OnConsoleMessageReceivedCallback> EVENT = EventFactory.createArrayBacked(OnConsoleMessageReceivedCallback.class, (listeners) -> (event) -> {
        for (OnConsoleMessageReceivedCallback listener : listeners) {
            listener.onMessage(event);
        }
    });

    void onMessage(LogEvent event);
}
