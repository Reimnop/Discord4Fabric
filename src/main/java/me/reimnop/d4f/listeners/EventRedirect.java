package me.reimnop.d4f.listeners;

import me.reimnop.d4f.events.OnPlayerUnvanishCallback;
import me.reimnop.d4f.events.OnPlayerVanishCallback;
import me.reimnop.d4f.events.PlayerConnectedCallback;
import me.reimnop.d4f.events.PlayerDisconnectedCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import static me.drex.vanish.api.VanishAPI.isVanished;

public final class EventRedirect {
    private EventRedirect() {}

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!(FabricLoader.getInstance().isModLoaded("melius-vanish") && isVanished(handler.player))) {
                PlayerConnectedCallback.EVENT.invoker().onConnected(handler.player, server, false);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!(FabricLoader.getInstance().isModLoaded("melius-vanish") && isVanished(handler.player))) {
                PlayerDisconnectedCallback.EVENT.invoker().onDisconnected(handler.player, server, false);
            }
        });

        OnPlayerVanishCallback.EVENT.register(player -> {
            PlayerDisconnectedCallback.EVENT.invoker().onDisconnected(player, player.server, true);
        });

        OnPlayerUnvanishCallback.EVENT.register(player -> {
            PlayerConnectedCallback.EVENT.invoker().onConnected(player, player.server, true);
        });
    }
}
