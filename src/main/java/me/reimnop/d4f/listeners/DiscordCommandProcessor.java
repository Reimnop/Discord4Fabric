package me.reimnop.d4f.listeners;

import me.drex.vanish.api.VanishAPI;
import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.utils.Compatibility;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;


import java.util.Arrays;
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
                playerNames = filterDrexHDVanishedPlayers(server, playerNames);


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

    /**
     * Filters out players who have vanished using <a href="https://github.com/DrexHD/Vanish">DrexHD's Vanish mod</a>
     * from the player list command.
     *
     * @param server The Minecraft server the players are on
     * @param unfilteredNames An unfiltered array of player names {@linkplain PlayerManager#getPlayerNames() obtained
     * from the player manager}.
     * @return Returns a list of player names on the server, without the names of vanished players. If DrexHD's Vanish
     * mod is not loaded, then returns the original unfiltered names.
     */
    private static String[] filterDrexHDVanishedPlayers(MinecraftServer server, String[] unfilteredNames) {

        if (!Compatibility.isVanishModLoaded()) {
            return unfilteredNames;
        }

        return Arrays.stream(unfilteredNames)
                .filter(
                        name -> {
                            PlayerManager manager = server.getPlayerManager();
                            ServerPlayerEntity player = manager.getPlayer(name);
                            // player should never be null as the names are assumed to all be valid
                            return player != null && Compatibility.isPlayerVanished(player);
                        }
                )
                .toArray(String[]::new);
    }
}
