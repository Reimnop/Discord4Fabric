package me.reimnop.d4f.listeners;

import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;


import java.util.Map;

public final class DiscordCommandProcessor {
    private DiscordCommandProcessor() {}

    static int playerListDisplayAmount;
    private interface DiscordCommandHandler {
        void handle(MinecraftServer server);
    }

    private static final Map<String, DiscordCommandHandler> commandHandlers = Map.of(
            "ping", server -> Discord4Fabric.DISCORD.sendPlainMessage("Pong! :ping_pong:"),
            "tps", server -> Discord4Fabric.DISCORD.sendPlainMessage("Server TPS: " + Utils.getTpsAsString()),
            "playerlist", server -> {
                int maxPlayers = server.getMaxPlayerCount();
                String[] playerNames = server.getPlayerNames();

                StringBuilder stringBuilder = new StringBuilder();
                if(playerListDisplayAmount == 0){
                    stringBuilder
                            .append(playerNames.length)
                            .append("/")
                            .append(maxPlayers)
                            .append(" players currently online");
                } else {
                    stringBuilder
                            .append(playerNames.length)
                            .append("/")
                            .append(maxPlayers)
                            .append(" players currently online:\n");

                    if (playerListDisplayAmount < 0 || playerListDisplayAmount > server.getMaxPlayerCount()) playerListDisplayAmount = server.getMaxPlayerCount();
                    int playersToShow = Math.min(playerNames.length, playerListDisplayAmount);
                    for (int i = 0; i < playersToShow; i++) {
                        stringBuilder
                                .append(playerNames[i])
                                .append(playersToShow - i == 1 ? "" : ", ");
                    }
                    if (playerNames.length > playersToShow) {
                        stringBuilder
                                .append(" *and ")
                                .append(playerNames.length - playersToShow)
                                .append(" more*");
                    }

                }

                Discord4Fabric.DISCORD.sendPlainMessage(stringBuilder.toString());
            }
    );

    private static final String prefix = "!";

    public static void init(Config config) {
        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            if (message.getChannel().getIdLong() != config.channelId) {
                return;
            }

            String content = message.getContentRaw();
            if (!content.startsWith(prefix)) {
                return;
            }

            String cmd = content.substring(prefix.length());
            if (commandHandlers.containsKey(cmd)) {
                commandHandlers.get(cmd).handle((MinecraftServer) FabricLoader.getInstance().getGameInstance());
            }
        });

        playerListDisplayAmount = config.playerListDisplayAmount;

    }
}
