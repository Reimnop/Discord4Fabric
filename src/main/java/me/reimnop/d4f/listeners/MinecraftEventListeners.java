package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.api.*;
import me.reimnop.d4f.*;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.events.PlayerDeathCallback;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.utils.Utils;
import me.reimnop.d4f.utils.VariableTimer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
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
import java.util.regex.Pattern;

public final class MinecraftEventListeners {
    private MinecraftEventListeners() {}

    private static final Pattern DISCORD_PING_PATTERN = Pattern.compile("<@(?<id>\\d+)>");
    private static final Pattern MINECRAFT_PING_PATTERN = Pattern.compile("@(?<tag>.+?#\\d{4})");

    public static void init(Discord discord, Config config) {
        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            if (!config.announceAdvancement) {
                return;
            }

            assert advancement.getDisplay() != null;

            String titleStr = config.advancementTaskTitle;
            String descStr = config.advancementTaskDescription;
            switch (advancement.getDisplay().getFrame()) {
                case GOAL -> {
                    titleStr = config.advancementGoalTitle;
                    descStr = config.advancementGoalDescription;
                }
                case TASK -> {
                    titleStr = config.advancementTaskTitle;
                    descStr = config.advancementTaskDescription;
                }
                case CHALLENGE -> {
                    titleStr = config.advancementChallengeTitle;
                    descStr = config.advancementChallengeDescription;
                }
            }

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("title"), (ctx, arg) -> PlaceholderResult.value(advancement.getDisplay().getTitle()),
                    Discord4Fabric.id("description"), (ctx, arg) -> PlaceholderResult.value(advancement.getDisplay().getDescription())
            );

            Text title = Placeholders.parseText(
                    TextParserUtils.formatText(titleStr),
                    PlaceholderContext.of(playerEntity),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            Text desc = Placeholders.parseText(
                    TextParserUtils.formatText(descStr),
                    PlaceholderContext.of(playerEntity),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.yellow, title.getString(), desc.getString());
        });

        VariableTimer<MinecraftServer> statusTimer = new VariableTimer<>(
                () -> config.updateInterval,
                server -> {
                    Text status = Placeholders.parseText(
                            TextParserUtils.formatText(config.status),
                            PlaceholderContext.of(server)
                    );
                    discord.setStatus(status);
                });

        ServerTickEvents.END_SERVER_TICK.register(statusTimer::tick);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!config.announceServerStartStop) {
                return;
            }

            Text message = Placeholders.parseText(
                    TextParserUtils.formatText(config.serverStartMessage),
                    PlaceholderContext.of(server)
            );
            discord.sendPlainMessage(message);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (!config.announceServerStartStop) {
                // The bot should close discord regardless if it should announce start stop or not
                // See: https://github.com/Reimnop/Discord4Fabric/issues/6
                discord.close();
                return;
            }

            Text message = Placeholders.parseText(
                    TextParserUtils.formatText(config.serverStopMessage),
                    PlaceholderContext.of(server)
            );
            discord.sendPlainMessage(message);

            discord.close();
        });

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            // Oversight.
            // See: https://github.com/Reimnop/Discord4Fabric/issues/8
            if (message.getChannel().getIdLong() != config.channelId) {
                return;
            }

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();

            // Parse Discord pings
            Text discordPingFormat = TextParserUtils.formatText(config.discordPingFormat);

            Text parsedMsg = Utils.regexDynamicReplaceText(
                    message.getContentRaw(),
                    DISCORD_PING_PATTERN,
                    match -> {
                        String idStr = match.group("id");
                        Long id = Long.parseLong(idStr);

                        User pingedUser = discord.getUser(id);

                        if (pingedUser != null) {
                            Map<Identifier, PlaceholderHandler> pingPlaceholders = Map.of(
                                    Discord4Fabric.id("fullname"), (ctx, arg) -> PlaceholderResult.value(pingedUser.getAsTag()),
                                    Discord4Fabric.id("nickname"), (ctx, arg) -> PlaceholderResult.value(pingedUser.getName()),
                                    Discord4Fabric.id("discriminator"), (ctx, arg) -> PlaceholderResult.value(pingedUser.getDiscriminator())
                            );

                            return Placeholders.parseText(
                                    discordPingFormat,
                                    PlaceholderContext.of(server),
                                    Placeholders.PLACEHOLDER_PATTERN,
                                    placeholder -> Utils.getPlaceholderHandler(placeholder, pingPlaceholders)
                            );
                        }
                        return Text.literal(match.group());
                    }
            );

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("fullname"), (ctx, arg) -> PlaceholderResult.value(user.getAsTag()),
                    Discord4Fabric.id("nickname"), (ctx, arg) -> PlaceholderResult.value(user.getName()),
                    Discord4Fabric.id("discriminator"), (ctx, arg) -> PlaceholderResult.value(user.getDiscriminator()),
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(parsedMsg)
            );

            server.getPlayerManager().broadcast(
                    Placeholders.parseText(
                            TextParserUtils.formatText(config.discordToMinecraftMessage),
                            PlaceholderContext.of(server),
                            Placeholders.PLACEHOLDER_PATTERN,
                            placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
                    ),
                    MessageType.TELLRAW_COMMAND);
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            String content = Utils.regexDynamicReplaceString(
                    message.filtered().getContent().getString(),
                    MINECRAFT_PING_PATTERN,
                    match -> {
                        String tag = match.group("tag");

                        try {
                            User user = discord.findUser(tag);
                            if (user != null) {
                                return user.getAsMention();
                            }
                        } catch (GuildException e) {
                            Utils.logException(e);
                        }
                        return match.group();
                    }
            );

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(content)
            );

            Text msg = Placeholders.parseText(
                    TextParserUtils.formatText(config.minecraftToDiscordMessage),
                    PlaceholderContext.of(sender),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            Text name = Placeholders.parseText(
                    TextParserUtils.formatText(config.discordName),
                    PlaceholderContext.of(sender),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendPlayerMessage(sender, name, msg);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!config.announcePlayerJoinLeave) {
                return;
            }

            // Requested by https://github.com/Reimnop/Discord4Fabric/issues/15
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("post_online"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(server.getCurrentPlayerCount() + 1))
            );

            Text msg = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerJoinMessage),
                    PlaceholderContext.of(handler.player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            Text desc = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerJoinDescription),
                    PlaceholderContext.of(handler.player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.green, msg.getString(), desc.getString());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!config.announcePlayerJoinLeave) {
                return;
            }

            // Requested by https://github.com/Reimnop/Discord4Fabric/issues/15
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("post_online"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(server.getCurrentPlayerCount() - 1))
            );

            Text msg = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerLeftMessage),
                    PlaceholderContext.of(handler.player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            Text desc = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerLeftDescription),
                    PlaceholderContext.of(handler.player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.red, msg.getString(), desc.getString());
        });

        PlayerDeathCallback.EVENT.register(((playerEntity, source, deathMessage) -> {
            if (!config.announcePlayerDeath) {
                return;
            }

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("reason"), (ctx, arg) -> PlaceholderResult.value(deathMessage)
            );

            Text msg = Placeholders.parseText(
                    TextParserUtils.formatText(config.deathMessage),
                    PlaceholderContext.of(playerEntity),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.black, msg.getString(), null);
        }));
    }
}
