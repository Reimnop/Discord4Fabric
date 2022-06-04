package me.reimnop.d4f;

import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.PlaceholderHandler;
import eu.pb4.placeholders.PlaceholderResult;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerDeathCallback;
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
import java.util.Map;

public class Discord4Fabric implements ModInitializer {
    public static final String MODID = "d4f";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Config CONFIG = new Config();
    public static Discord DISCORD;

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        try {
            File file = new File(Utils.getConfigPath());
            if (!file.exists()) {
                CONFIG.writeConfig(file);
            } else {
                CONFIG.readConfig(file);
            }

            initDiscord();
        } catch (LoginException e) {
            LOGGER.error("Login Failed! Please update your bot token and restart the server");
        } catch (IOException e) {
            LOGGER.error("Could not load config! Please update your config and restart the server");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void initDiscord() throws LoginException, InterruptedException {
        DISCORD = new Discord(CONFIG);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Text message = PlaceholderAPI.parseText(Text.literal(CONFIG.getServerStartMessage()), server);
            DISCORD.sendPlainMessage(message);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Text message = PlaceholderAPI.parseText(Text.literal(CONFIG.getServerStopMessage()), server);
            DISCORD.sendPlainMessage(message);
        });

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    id("fullname"), ctx -> PlaceholderResult.value(Text.literal(user.getAsTag())),
                    id("nickname"), ctx -> PlaceholderResult.value(Text.literal(user.getName())),
                    id("discriminator"), ctx -> PlaceholderResult.value(Text.literal(user.getDiscriminator())),
                    id("message"), ctx -> PlaceholderResult.value(Text.literal(message.getContentRaw())));

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
            server.getPlayerManager().broadcast(
                    PlaceholderAPI.parseTextCustom(
                            Text.literal(CONFIG.getDiscordToMinecraftMessage()),
                            server,
                            placeholders,
                            PlaceholderAPI.PLACEHOLDER_PATTERN
                    ),
                    MessageType.TELLRAW_COMMAND);
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            Text content = message.filtered().getContent();
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    id("message"), ctx -> PlaceholderResult.value(content)
            );
            Text msg = PlaceholderAPI.parseTextCustom(
                    Text.literal(CONFIG.getMinecraftToDiscordMessage()),
                    sender,
                    placeholders,
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            DISCORD.sendPlayerMessage(sender, msg);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DISCORD.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.green,
                    PlaceholderAPI.parseText(
                            Text.literal(CONFIG.getPlayerJoinMessage()),
                            handler.player
                    ));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            DISCORD.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.red,
                    PlaceholderAPI.parseText(
                            Text.literal(CONFIG.getPlayerLeftMessage()),
                            handler.player
                    ));
        });

        PlayerDeathCallback.EVENT.register(((playerEntity, source) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    id("message"), ctx -> PlaceholderResult.value(source.getDeathMessage(playerEntity))
            );
            Text msg = PlaceholderAPI.parseTextCustom(
                    Text.literal(CONFIG.getDeathMessage()),
                    playerEntity,
                    placeholders,
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            DISCORD.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.black, msg);
        }));
    }
}
