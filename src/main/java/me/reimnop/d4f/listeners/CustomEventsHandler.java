package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import me.reimnop.d4f.customevents.constraints.*;
import me.reimnop.d4f.events.PlayerConnectedCallback;
import me.reimnop.d4f.events.PlayerDisconnectedCallback;
import me.reimnop.d4f.utils.Compatibility;
import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.CustomEvents;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class CustomEventsHandler {
    private CustomEventsHandler() {}

    public static void init(Config config, CustomEvents customEvents) {
        PlayerConnectedCallback.EVENT.register((player, server, fromVanish) -> {
            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player) && !fromVanish) {
                return;
            }

            PlaceholderContext placeholderContext = PlaceholderContext.of(player);
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintProcessors.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_JOIN, placeholderContext, supportedConstraints);
        });

        PlayerDisconnectedCallback.EVENT.register((player, server, fromVanish) -> {
            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player) && !fromVanish) {
                return;
            }

            PlaceholderContext placeholderContext = PlaceholderContext.of(player);
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintProcessors.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_LEAVE, placeholderContext, supportedConstraints);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PlaceholderContext placeholderContext = PlaceholderContext.of(server);
            customEvents.raiseEvent(CustomEvents.SERVER_START, placeholderContext, null);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PlaceholderContext placeholderContext = PlaceholderContext.of(server);
            customEvents.raiseEvent(CustomEvents.SERVER_STOP, placeholderContext, null);
        });

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            if (message.getChannel().getIdLong() != config.channelId) {
                return;
            }

            Member member = Discord4Fabric.DISCORD.getMember(user);
            String username = member == null ? user.getName() : member.getEffectiveName();

            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.DISCORD_ID, () -> new LongEqualsConstraintProcessor(user.getIdLong()),
                    ConstraintProcessors.DISCORD_NAME, () -> new StringEqualsConstraintProcessor(username),
                    ConstraintProcessors.DISCORD_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(username),
                    ConstraintProcessors.DISCORD_MESSAGE, () -> new StringEqualsConstraintProcessor(message.getContentRaw()),
                    ConstraintProcessors.DISCORD_MESSAGE_CONTAINS, () -> new StringContainsConstraintProcessor(message.getContentRaw())
            );

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
            PlaceholderContext placeholderContext = PlaceholderContext.of(server);
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("fullname"), (ctx, arg) -> PlaceholderResult.value(user.getAsTag()),
                    Discord4Fabric.id("nickname"), (ctx, arg) -> PlaceholderResult.value(Utils.getNicknameFromUser(user)),
                    Discord4Fabric.id("discriminator"), (ctx, arg) -> PlaceholderResult.value(user.getDiscriminator()),
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(message.getContentRaw())
            );
            customEvents.raiseEvent(CustomEvents.DISCORD_MESSAGE, placeholderContext, null, placeholders);
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, typeKey) -> {
            PlaceholderContext placeholderContext = PlaceholderContext.of(sender);
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(message.getContent())
            );
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(sender.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(sender.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(sender.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(sender.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(sender),
                    ConstraintProcessors.MC_UUID, () -> new MinecraftUuidConstraintProcessor(sender.getUuid()),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(sender.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(sender.getName().getString()),
                    ConstraintProcessors.MC_MESSAGE, () -> new StringEqualsConstraintProcessor(message.getContent().getString()),
                    ConstraintProcessors.MC_MESSAGE_CONTAINS, () -> new StringContainsConstraintProcessor(message.getContent().getString())
            );
            customEvents.raiseEvent(CustomEvents.MINECRAFT_MESSAGE, placeholderContext, supportedConstraints, placeholders);
        });

        PlayerAdvancementCallback.EVENT.register((player, advancement) -> {
            PlaceholderContext placeholderContext = PlaceholderContext.of(player);
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("title"), (ctx, arg) -> PlaceholderResult.value(advancement.getDisplay().getTitle())
            );
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintProcessors.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.ADVANCEMENT, placeholderContext, supportedConstraints, placeholders);
        });
    }
}
