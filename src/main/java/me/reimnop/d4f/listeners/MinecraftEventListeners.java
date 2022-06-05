package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import me.reimnop.d4f.*;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.events.PlayerDeathCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Map;

public final class MinecraftEventListeners {
    private MinecraftEventListeners() {}

    public static void init(Discord discord, Config config) {
        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            assert advancement.getDisplay() != null;

            String message = config.advancementTaskMessage;
            switch (advancement.getDisplay().getFrame()) {
                case GOAL -> message = config.advancementGoalMessage;
                case TASK -> message = config.advancementTaskMessage;
                case CHALLENGE -> message = config.advancementChallengeMessage;
            }

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(advancement.getDisplay().getTitle())
            );

            Text msg = Placeholders.parseText(
                    Text.literal(message),
                    PlaceholderContext.of(playerEntity),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.yellow, msg);
        });

        Timer<MinecraftServer> statusTimer = new Timer<>(config.updateInterval, server -> {
            Text status = Placeholders.parseText(
                    Text.literal(config.status),
                    PlaceholderContext.of(server)
            );
            discord.setStatus(status);
        });

        ServerTickEvents.END_SERVER_TICK.register(statusTimer::tick);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Text message = Placeholders.parseText(
                    Text.literal(config.serverStartMessage),
                    PlaceholderContext.of(server)
            );
            discord.sendPlainMessage(message);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Text message = Placeholders.parseText(
                    Text.literal(config.serverStopMessage),
                    PlaceholderContext.of(server)
            );
            discord.sendPlainMessage(message);

            discord.close();
        });

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("fullname"), (ctx, arg) -> PlaceholderResult.value(Text.literal(user.getAsTag())),
                    Discord4Fabric.id("nickname"), (ctx, arg) -> PlaceholderResult.value(Text.literal(user.getName())),
                    Discord4Fabric.id("discriminator"), (ctx, arg) -> PlaceholderResult.value(Text.literal(user.getDiscriminator())),
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(Text.literal(message.getContentRaw()))
            );

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
            server.getPlayerManager().broadcast(
                    Placeholders.parseText(
                            Text.literal(config.discordToMinecraftMessage),
                            PlaceholderContext.of(server),
                            Placeholders.PLACEHOLDER_PATTERN,
                            placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
                    ),
                    MessageType.TELLRAW_COMMAND);
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            Text content = message.filtered().getContent();
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(content)
            );

            Text msg = Placeholders.parseText(
                    TextNode.convert(Text.literal(config.minecraftToDiscordMessage)),
                    PlaceholderContext.of(sender),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendPlayerMessage(sender, msg);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            discord.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.green,
                    Placeholders.parseText(
                            Text.literal(config.playerJoinMessage),
                            PlaceholderContext.of(handler.player)
                    ));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            discord.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.red,
                    Placeholders.parseText(
                            Text.literal(config.playerLeftMessage),
                            PlaceholderContext.of(handler.player)
                    ));
        });

        PlayerDeathCallback.EVENT.register(((playerEntity, source) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(source.getDeathMessage(playerEntity))
            );

            Text msg = Placeholders.parseText(
                    Text.literal(config.deathMessage),
                    PlaceholderContext.of(playerEntity),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.black, msg);
        }));
    }
}
