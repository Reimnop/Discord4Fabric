package me.reimnop.d4f.listeners;

import com.vdurmont.emoji.EmojiParser;
import eu.pb4.placeholders.api.*;
import me.reimnop.d4f.AccountLinking;
import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.console.ConsoleChannelHandler;
import me.reimnop.d4f.duck.IStyleAccess;
import me.reimnop.d4f.events.*;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.utils.Compatibility;
import me.reimnop.d4f.utils.Utils;
import me.reimnop.d4f.utils.VariableTimer;
import me.reimnop.d4f.utils.text.TextUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class MinecraftEventListeners {
    private MinecraftEventListeners() {}

    private static final Pattern DISCORD_PING_PATTERN = Pattern.compile("<@(?<id>\\d+)>");
    private static final Pattern MINECRAFT_PING_PATTERN = Pattern.compile("@(?<name>\\w+)");
    private static final Pattern EMOTE_PATTERN = Pattern.compile(":(?<name>[^\\n ]+?):");
    private static final Pattern RAW_EMOTE_PATTERN = Pattern.compile("<a?:(?<name>.+?):\\d+>");

    public static void init(Discord discord, AccountLinking accountLinking, Config config) {
        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            if (!config.announceAdvancement) {
                return;
            }
            // Vanish compatibility
            if(Compatibility.isPlayerVanished(playerEntity)) {
                return;
            }

            Optional<AdvancementDisplay> advancementDisplay = advancement.display();
            if (advancementDisplay.isEmpty()) {
                return;
            }
            AdvancementDisplay display = advancementDisplay.get();

            String titleStr = config.advancementTaskTitle;
            String descStr = config.advancementTaskDescription;
            switch (display.getFrame()) {
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
                    Discord4Fabric.id("title"), (ctx, arg) -> PlaceholderResult.value(display.getTitle()),
                    Discord4Fabric.id("description"), (ctx, arg) -> PlaceholderResult.value(display.getDescription())
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

        VariableTimer<MinecraftServer> topicTimer = new VariableTimer<>(
                () -> config.topicUpdateInterval,
                server -> {
                    Text status = Placeholders.parseText(
                            TextParserUtils.formatText(config.topic),
                            PlaceholderContext.of(server)
                    );
                    discord.setChannelTopic(status);
                });
        ServerTickEvents.END_SERVER_TICK.register(topicTimer::tick);

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

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (!config.announceServerStartStop) {
                return;
            }

            Text message = Placeholders.parseText(
                    TextParserUtils.formatText(config.serverStopMessage),
                    PlaceholderContext.of(server)
            );
            discord.sendPlainMessage(message);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ConsoleChannelHandler.stop();
            discord.close();
        });

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            if (user.isBot() && (!config.allowBotMessages || user.getIdLong() == discord.selfUser.getIdLong())) {
                return;
            }

            if (message.getChannel() instanceof PrivateChannel channel) {
                String code = message.getContentRaw();
                AccountLinking.LinkingResult result = accountLinking.tryLinkAccount(code, user.getIdLong());
                switch (result) {
                    case INVALID_CODE -> channel
                            .sendMessage(MessageCreateData.fromContent("Invalid linking code!"))
                            .queue();
                    case ACCOUNT_LINKED -> channel
                            .sendMessage(MessageCreateData.fromContent("Your account was already linked!"))
                            .queue();
                    case SUCCESS -> channel
                            .sendMessage(MessageCreateData.fromContent("Your account was successfully linked!"))
                            .queue();
                }
                return;
            }

            // Oversight.
            // See: https://github.com/Reimnop/Discord4Fabric/issues/8
            if (message.getChannel().getIdLong() != config.channelId) {
                return;
            }

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();

            // Parse Discord pings
            String parsedString = TextUtils.regexDynamicReplaceString(
                    message.getContentRaw(),
                    DISCORD_PING_PATTERN,
                    match -> {
                        String idStr = match.group("id");
                        Long id = Long.parseLong(idStr);

                        User pingedUser = discord.getUser(id);

                        if (pingedUser != null) {
                            // Play ping sound to pinged user if they have an account linked
                            Optional<UUID> pingedPlayerUuid = accountLinking.getLinkedAccount(pingedUser.getIdLong());
                            if (pingedPlayerUuid.isPresent()) {
                                ServerPlayerEntity player = server.getPlayerManager().getPlayer(pingedPlayerUuid.get());
                                if (player != null) {
                                    player.playSound(SoundEvents.BLOCK_BELL_USE);
                                }
                            }

                            Map<Identifier, PlaceholderHandler> pingPlaceholders = Map.of(
                                    Discord4Fabric.id("fullname"), (ctx, arg) -> PlaceholderResult.value(pingedUser.getAsTag()),
                                    Discord4Fabric.id("nickname"), (ctx, arg) -> PlaceholderResult.value(Utils.getNicknameFromUser(pingedUser)),
                                    Discord4Fabric.id("colored_nickname"), (ctx, arg) -> PlaceholderResult.value(Utils.getColoredNicknameFromUser(user)),
                                    Discord4Fabric.id("discriminator"), (ctx, arg) -> PlaceholderResult.value(pingedUser.getDiscriminator())
                            );

                            return Placeholders
                                    .parseText(
                                        Text.literal(config.discordPingFormat), // We'll format this later
                                        PlaceholderContext.of(server),
                                        Placeholders.PLACEHOLDER_PATTERN,
                                        placeholder -> Utils.getPlaceholderHandler(placeholder, pingPlaceholders)
                                    )
                                    .getString();
                        }
                        return match.group();
                    }
            );

            // Parse raw emotes
            parsedString = TextUtils.regexDynamicReplaceString(
                    parsedString,
                    RAW_EMOTE_PATTERN,
                    match -> match.group("name")
            );

            parsedString = TextUtils.parseMarkdownToPAPI(parsedString);
            parsedString = EmojiParser.parseToAliases(parsedString);

            Text parsedMsg = TextParserUtils.formatText(parsedString);

            // Remove all click events from all child (major security risk)
            parsedMsg.visit((style, pos) -> {
                // TODO: Dirty mixin hack to remove click event
                // But I was too lazy to do it properly
                ((IStyleAccess) style).Discord4Fabric$setClickEvent(null);
                return Optional.of(Style.EMPTY);
            }, Style.EMPTY);


            Map<Identifier, PlaceholderHandler> placeholders = new HashMap<>(Map.of(
                    Discord4Fabric.id("fullname"), (ctx, arg) -> PlaceholderResult.value(user.getAsTag()),
                    Discord4Fabric.id("nickname"), (ctx, arg) -> PlaceholderResult.value(Utils.getNicknameFromUser(user)),
                    Discord4Fabric.id("colored_nickname"), (ctx, arg) -> PlaceholderResult.value(Utils.getColoredNicknameFromUser(user)),
                    Discord4Fabric.id("discriminator"), (ctx, arg) -> PlaceholderResult.value(user.getDiscriminator()),
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(parsedMsg)
            ));

            Message repliedMessage = message.getReferencedMessage();
            if (repliedMessage != null) {
                User repliedUser = repliedMessage.getAuthor();
                placeholders.putAll(Map.of(
                        Discord4Fabric.id("reply_fullname"), (ctx, arg) -> PlaceholderResult.value(repliedUser.getAsTag()),
                        Discord4Fabric.id("reply_nickname"), (ctx, arg) -> PlaceholderResult.value(Utils.getNicknameFromUser(repliedUser)),
                        Discord4Fabric.id("reply_colored_nickname"), (ctx, arg) -> PlaceholderResult.value(Utils.getColoredNicknameFromUser(user)),
                        Discord4Fabric.id("reply_discriminator"), (ctx, arg) -> PlaceholderResult.value(repliedUser.getDiscriminator())
                ));
            }

            MutableText msg = (MutableText) Placeholders.parseText(
                    TextParserUtils.formatText(repliedMessage == null ? config.discordToMinecraftMessage : config.discordToMinecraftWithReplyMessage),
                    PlaceholderContext.of(server),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            for (Message.Attachment attachment : message.getAttachments()) {
                msg.append(Text.literal(" "));
                msg.append(Text.literal("[att]")
                        .setStyle(Style.EMPTY
                                .withFormatting(Formatting.BLUE, Formatting.UNDERLINE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachment.getUrl()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open URL"))))
                );
            }

            server.getPlayerManager().broadcast(msg, false);
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            if (!config.sendMessagesToDiscord) {
                return;
            }

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();

            String content = TextUtils.regexDynamicReplaceString(
                    message.getContent().getString(),
                    MINECRAFT_PING_PATTERN,
                    match -> {
                        String name = match.group("name");

                        try {
                            User user = discord.findUserByName(name);

                            if (user == null) {
                                ServerPlayerEntity pingedPlayer = server.getPlayerManager().getPlayer(name);
                                if (pingedPlayer != null) {
                                    Optional<Long> linkedId = accountLinking.getLinkedAccount(pingedPlayer.getUuid());
                                    user = linkedId.map(discord::getUser).orElse(null);
                                }
                            }

                            if (user != null) {
                                // Play ping sound to pinged user if they have an account linked
                                Optional<UUID> pingedPlayerUuid = accountLinking.getLinkedAccount(user.getIdLong());
                                if (pingedPlayerUuid.isPresent()) {
                                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(pingedPlayerUuid.get());
                                    if (player != null) {
                                        player.playSound(SoundEvents.BLOCK_BELL_USE);
                                    }
                                }

                                return user.getAsMention();
                            }
                        } catch (GuildException e) {
                            Utils.logException(e);
                        }
                        return match.group();
                    }
            );

            // Parse emojis
            content = TextUtils.regexDynamicReplaceString(
                    content,
                    EMOTE_PATTERN,
                    match -> {
                        String emoteName = match.group("name");
                        Emoji emoji = discord.findEmojis(emoteName);
                        if (emoji != null) {
                            return emoji.getFormatted();
                        }
                        return match.group();
                    }
            );

            String finalContent = content;
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(finalContent)
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

        ServerMessageEvents.COMMAND_MESSAGE.register(((message, source, typeKey) -> {
            if (!config.sendBroadcastedMessagesToDiscord) {
                return;
            }

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();

            String content = TextUtils.regexDynamicReplaceString(
                    message.getContent().getString(),
                    MINECRAFT_PING_PATTERN,
                    match -> {
                        String name = match.group("name");

                        try {
                            User user = discord.findUserByName(name);

                            if (user == null) {
                                ServerPlayerEntity pingedPlayer = server.getPlayerManager().getPlayer(name);
                                if (pingedPlayer != null) {
                                    Optional<Long> linkedId = accountLinking.getLinkedAccount(pingedPlayer.getUuid());
                                    user = linkedId.map(discord::getUser).orElse(null);
                                }
                            }

                            if (user != null) {
                                // Play ping sound to pinged user if they have an account linked
                                Optional<UUID> pingedPlayerUuid = accountLinking.getLinkedAccount(user.getIdLong());
                                if (pingedPlayerUuid.isPresent()) {
                                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(pingedPlayerUuid.get());
                                    if (player != null) {
                                        player.playSound(SoundEvents.BLOCK_BELL_USE);
                                    }
                                }

                                return user.getAsMention();
                            }
                        } catch (GuildException e) {
                            Utils.logException(e);
                        }
                        return match.group();
                    }
            );

            // Parse emojis
            content = TextUtils.regexDynamicReplaceString(
                    content,
                    EMOTE_PATTERN,
                    match -> {
                        String emoteName = match.group("name");
                        Emoji emoji = discord.findEmojis(emoteName);
                        if (emoji != null) {
                            return emoji.getFormatted();
                        }
                        return match.group();
                    }
            );

            String finalContent = content;
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(finalContent)
            );

            ServerPlayerEntity sender = source.getPlayer();

            Text msg = sender == null ? message.getContent() : Placeholders.parseText(
                    TextParserUtils.formatText(config.minecraftToDiscordMessage),
                    PlaceholderContext.of(sender),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            Text name = sender == null ? Text.literal("Server") : Placeholders.parseText(
                    TextParserUtils.formatText(config.discordName),
                    PlaceholderContext.of(sender),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendPlayerMessage(sender, name, msg);

        }));

        PlayerConnectedCallback.EVENT.register((player, server, fromVanish) -> {
            if (config.requiresLinkedAccount && accountLinking.getLinkedAccount(player.getUuid()).isEmpty()) {
                Discord4Fabric.kickForUnlinkedAccount(player);
                return;
            }

            if (!config.announcePlayerJoinLeave) {
                return;
            }

            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player) && !fromVanish) {
                return;
            }

            // Requested by https://github.com/Reimnop/Discord4Fabric/issues/15
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("post_online"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(server.getCurrentPlayerCount() + 1))
            );

            Text msg = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerJoinMessage),
                    PlaceholderContext.of(player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            Text desc = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerJoinDescription),
                    PlaceholderContext.of(player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(player, Color.green, msg.getString(), desc.getString());
        });

        PlayerDisconnectedCallback.EVENT.register((player, server, fromVanish) -> {
            if (!config.announcePlayerJoinLeave) {
                return;
            }

            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player) && !fromVanish) {
                return;
            }

            // Requested by https://github.com/Reimnop/Discord4Fabric/issues/15
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("post_online"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(server.getCurrentPlayerCount() - 1))
            );

            Text msg = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerLeftMessage),
                    PlaceholderContext.of(player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            Text desc = Placeholders.parseText(
                    TextParserUtils.formatText(config.playerLeftDescription),
                    PlaceholderContext.of(player),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(player, Color.red, msg.getString(), desc.getString());
        });

        PlayerDeathCallback.EVENT.register(((playerEntity, source, deathMessage) -> {
            if (!config.announcePlayerDeath) {
                return;
            }

            // Vanish compatibility
            if (Compatibility.isPlayerVanished(playerEntity)) {
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

            Text desc = Placeholders.parseText(
                    TextParserUtils.formatText(config.deathDescription),
                    PlaceholderContext.of(playerEntity),
                    Placeholders.PLACEHOLDER_PATTERN,
                    placeholder -> Utils.getPlaceholderHandler(placeholder, placeholders)
            );

            discord.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.black, msg.getString(), desc.getString());
        }));
    }
}
