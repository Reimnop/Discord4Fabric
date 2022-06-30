package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.api.*;
import me.reimnop.d4f.*;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.events.PlayerDeathCallback;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.utils.Utils;
import me.reimnop.d4f.utils.VariableTimer;
import me.reimnop.d4f.utils.text.TextUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.swing.text.html.Option;
import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class MinecraftEventListeners {
    private MinecraftEventListeners() {}

    private static final Pattern DISCORD_PING_PATTERN = Pattern.compile("<@(?<id>\\d+)>");
    private static final Pattern MINECRAFT_PING_PATTERN = Pattern.compile("@(?<tag>.+?#\\d{4})");
    private static final Pattern EMOTE_PATTERN = Pattern.compile(":(?<name>[^\\n ]+?):");

    public static void init(Discord discord, AccountLinking accountLinking, Config config) {
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
            if (message.getChannel() instanceof PrivateChannel channel) {
                String code = message.getContentRaw();
                AccountLinking.LinkingResult result = accountLinking.tryLinkAccount(code, user.getIdLong());
                switch (result) {
                    case INVALID_CODE -> channel.sendMessage(new MessageBuilder().append("Invalid linking code!").build()).queue();
                    case ACCOUNT_LINKED -> channel.sendMessage(new MessageBuilder().append("Your account was already linked!").build()).queue();
                    case SUCCESS -> channel.sendMessage(new MessageBuilder().append("Your account was successfully linked!").build()).queue();
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
                                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
                                }
                            }

                            Map<Identifier, PlaceholderHandler> pingPlaceholders = Map.of(
                                    Discord4Fabric.id("fullname"), (ctx, arg) -> PlaceholderResult.value(pingedUser.getAsTag()),
                                    Discord4Fabric.id("nickname"), (ctx, arg) -> PlaceholderResult.value(pingedUser.getName()),
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

            parsedString = TextUtils.parseMarkdownToPAPI(parsedString);
            Text parsedMsg = TextParserUtils.formatText(parsedString);

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
            String content = TextUtils.regexDynamicReplaceString(
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

            // Parse emojis
            content = TextUtils.regexDynamicReplaceString(
                    content,
                    EMOTE_PATTERN,
                    match -> {
                        String emoteName = match.group("name");
                        Emote emote = discord.findEmote(emoteName);
                        if (emote != null) {
                            return emote.getAsMention();
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

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (config.requiresLinkedAccount && accountLinking.getLinkedAccount(handler.player.getUuid()).isEmpty()) {
                Discord4Fabric.kickForUnlinkedAccount(handler.player);
                return;
            }

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
