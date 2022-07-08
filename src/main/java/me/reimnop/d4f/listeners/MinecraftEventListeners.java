package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.*;
import me.reimnop.d4f.*;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.events.PlayerChatReceivedCallback;
import me.reimnop.d4f.events.PlayerDeathCallback;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.utils.Utils;
import me.reimnop.d4f.utils.VariableTimer;
import me.reimnop.d4f.utils.text.TextUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

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
                    Discord4Fabric.id("title"), ctx -> PlaceholderResult.value(advancement.getDisplay().getTitle()),
                    Discord4Fabric.id("description"), ctx -> PlaceholderResult.value(advancement.getDisplay().getDescription())
            );

            Text title = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(titleStr),
                    playerEntity,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            Text desc = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(descStr),
                    playerEntity,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            discord.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.yellow, title.getString(), desc.getString());
        });

        VariableTimer<MinecraftServer> statusTimer = new VariableTimer<>(
                () -> config.updateInterval,
                server -> {
                    Text status = PlaceholderAPI.parseText(
                            TextParser.parse(config.status),
                            server
                    );
                    discord.setStatus(status);
                });

        ServerTickEvents.END_SERVER_TICK.register(statusTimer::tick);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!config.announceServerStartStop) {
                return;
            }

            Text message = PlaceholderAPI.parseText(
                    TextParser.parse(config.serverStartMessage),
                    server
            );
            discord.sendPlainMessage(message);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (!config.announceServerStartStop) {
                // The bot should close discord regardless if it should announce start stop or not
                // See: https://github.com/Reimnop/Discord4Fabric/issues/6
                discord.close();
                return;
            }

            Text message = PlaceholderAPI.parseText(
                    TextParser.parse(config.serverStopMessage),
                    server
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
                                    Discord4Fabric.id("fullname"), ctx -> PlaceholderResult.value(pingedUser.getAsTag()),
                                    Discord4Fabric.id("nickname"), ctx -> PlaceholderResult.value(pingedUser.getName()),
                                    Discord4Fabric.id("discriminator"), ctx -> PlaceholderResult.value(pingedUser.getDiscriminator())
                            );

                            return PlaceholderAPI
                                    .parseTextCustom(
                                            new LiteralText(config.discordPingFormat), // We'll format this later
                                            server,
                                            Utils.getPlaceholderHandlerMap(pingPlaceholders),
                                            PlaceholderAPI.PLACEHOLDER_PATTERN
                                    )
                                    .getString();
                        }
                        return match.group();
                    }
            );

            parsedString = TextUtils.parseMarkdownToPAPI(parsedString);
            Text parsedMsg = TextParser.parse(parsedString);

            Member member = discord.getMember(user);
            assert member != null;

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("fullname"), ctx -> PlaceholderResult.value(user.getAsTag()),
                    Discord4Fabric.id("nickname"), ctx -> PlaceholderResult.value(member.getEffectiveName()),
                    Discord4Fabric.id("discriminator"), ctx -> PlaceholderResult.value(user.getDiscriminator()),
                    Discord4Fabric.id("message"), ctx -> PlaceholderResult.value(parsedMsg)
            );

            server.getPlayerManager().broadcast(
                    PlaceholderAPI.parseTextCustom(
                            TextParser.parse(config.discordToMinecraftMessage),
                            server,
                            Utils.getPlaceholderHandlerMap(placeholders),
                            PlaceholderAPI.PLACEHOLDER_PATTERN
                    ),
                    MessageType.SYSTEM,
                    Util.NIL_UUID);
        });

        PlayerChatReceivedCallback.EVENT.register((sender, message) -> {
            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();

            String content = TextUtils.regexDynamicReplaceString(
                    message,
                    MINECRAFT_PING_PATTERN,
                    match -> {
                        String tag = match.group("tag");

                        try {
                            User user = discord.findUser(tag);
                            if (user != null) {
                                // Play ping sound to pinged user if they have an account linked
                                Optional<UUID> pingedPlayerUuid = accountLinking.getLinkedAccount(user.getIdLong());
                                if (pingedPlayerUuid.isPresent()) {
                                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(pingedPlayerUuid.get());
                                    if (player != null) {
                                        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.NEUTRAL, 1.0f, 1.0f);
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
                        Emote emote = discord.findEmote(emoteName);
                        if (emote != null) {
                            return emote.getAsMention();
                        }
                        return match.group();
                    }
            );

            String finalContent = content;
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), ctx -> PlaceholderResult.value(finalContent)
            );

            Text msg = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.minecraftToDiscordMessage),
                    sender,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            Text name = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.discordName),
                    sender,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
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
                    Discord4Fabric.id("post_online"), ctx -> PlaceholderResult.value(String.valueOf(server.getCurrentPlayerCount() + 1))
            );

            Text msg = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.playerJoinMessage),
                    handler.player,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            Text desc = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.playerJoinDescription),
                    handler.player,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            discord.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.green, msg.getString(), desc.getString());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!config.announcePlayerJoinLeave) {
                return;
            }

            // Requested by https://github.com/Reimnop/Discord4Fabric/issues/15
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("post_online"), ctx -> PlaceholderResult.value(String.valueOf(server.getCurrentPlayerCount() - 1))
            );

            Text msg = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.playerLeftMessage),
                    handler.player,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            Text desc = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.playerLeftDescription),
                    handler.player,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            discord.sendEmbedMessageUsingPlayerAvatar(handler.player, Color.red, msg.getString(), desc.getString());
        });

        PlayerDeathCallback.EVENT.register(((playerEntity, source, deathMessage) -> {
            if (!config.announcePlayerDeath) {
                return;
            }

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("reason"), ctx -> PlaceholderResult.value(deathMessage)
            );

            Text msg = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.deathMessage),
                    playerEntity,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            Text desc = PlaceholderAPI.parseTextCustom(
                    TextParser.parse(config.deathDescription),
                    playerEntity,
                    Utils.getPlaceholderHandlerMap(placeholders),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );

            discord.sendEmbedMessageUsingPlayerAvatar(playerEntity, Color.black, msg.getString(), desc.getString());
        }));
    }
}
