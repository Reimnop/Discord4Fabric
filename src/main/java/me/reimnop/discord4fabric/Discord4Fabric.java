package me.reimnop.discord4fabric;

import me.reimnop.discord4fabric.events.DiscordMessageReceivedCallback;
import me.reimnop.discord4fabric.events.PlayerDeathCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Discord4Fabric implements ModInitializer {
    public static final String MODID = "discord4fabric";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static Discord DISCORD;
    public static Config CONFIG;


    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        try {
            setupConfig();
            init();
        } catch (LoginException e) {
            LOGGER.error("Login Failed! Please update your bot token and restart the server");
        } catch (IOException e) {
            LOGGER.error("Could not load config! Please update your config and restart the server");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupConfig() throws IOException {
        File file = new File(Utils.getConfigPath());
        CONFIG = new Config();

        if (!file.exists()) {
            CONFIG.writeConfig(file);
        } else {
            CONFIG.readConfig(file);
        }
    }

    private void init() throws LoginException, InterruptedException {
        DISCORD = new Discord();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DISCORD.sendPlainMessage(CONFIG.getServerStartMessage());
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            DISCORD.sendPlainMessage(CONFIG.getServerStopMessage());
        });

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            String str = message.getContentRaw();

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
            server.getPlayerManager().broadcast(Text.literal(
                    String.format(
                            CONFIG.getDiscordMessageFormat(),
                            user.getName(),
                            str)
            ), MessageType.TELLRAW_COMMAND);
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            DISCORD.sendPlayerMessage(sender, message.raw().getContent().getString());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DISCORD.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.green, String.format(CONFIG.getPlayerJoinMessage(), handler.player.getDisplayName().getString()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            DISCORD.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.red, String.format(CONFIG.getPlayerLeftMessage(), handler.player.getDisplayName().getString()));
        });

        PlayerDeathCallback.EVENT.register(((playerEntity, source) -> {
            DISCORD.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.black, source.getDeathMessage(playerEntity).getString());
        }));
    }
}
