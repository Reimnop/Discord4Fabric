package me.reimnop.d4f.listeners;

import me.reimnop.d4f.events.OnPlayerUnvanishCallback;
import me.reimnop.d4f.events.OnPlayerVanishCallback;
import me.reimnop.d4f.events.PlayerConnectedCallback;
import me.reimnop.d4f.events.PlayerDisconnectedCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class EventRedirect {
    private EventRedirect() {}

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerConnectedCallback.EVENT.invoker().onConnected(handler.player, server, false);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlayerDisconnectedCallback.EVENT.invoker().onDisconnected(handler.player, server, false);
        });

        OnPlayerVanishCallback.EVENT.register(player -> {
            PlayerDisconnectedCallback.EVENT.invoker().onDisconnected(player, player.server, true);
        });

        OnPlayerUnvanishCallback.EVENT.register(player -> {
            PlayerConnectedCallback.EVENT.invoker().onConnected(player, player.server, true);
        });
    }
}
