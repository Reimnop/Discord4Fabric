package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.PlaceholderHandler;
import eu.pb4.placeholders.PlaceholderResult;
import me.reimnop.d4f.customevents.constraints.*;
import me.reimnop.d4f.events.*;
import me.reimnop.d4f.utils.Compatibility;
import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.CustomEvents;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintTypes.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintTypes.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintTypes.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintTypes.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_JOIN, player, supportedConstraints);
        });

        PlayerDisconnectedCallback.EVENT.register((player, server, fromVanish) -> {
            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player) && !fromVanish) {
                return;
            }
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintTypes.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintTypes.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintTypes.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintTypes.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_LEAVE, player, supportedConstraints);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            customEvents.raiseEvent(CustomEvents.SERVER_START, server, null);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            customEvents.raiseEvent(CustomEvents.SERVER_STOP, server, null);
        });

        DiscordMessageReceivedCallback.EVENT.register((user, message) -> {
            if (message.getChannel().getIdLong() != config.channelId) {
                return;
            }

            Member member = Discord4Fabric.DISCORD.getMember(user);
            String username = member == null ? user.getName() : member.getEffectiveName();

            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintTypes.DISCORD_ID, () -> new LongEqualsConstraintProcessor(user.getIdLong()),
                    ConstraintTypes.DISCORD_NAME, () -> new StringEqualsConstraintProcessor(username),
                    ConstraintTypes.DISCORD_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(username),
                    ConstraintTypes.DISCORD_MESSAGE, () -> new StringEqualsConstraintProcessor(message.getContentRaw()),
                    ConstraintTypes.DISCORD_MESSAGE_CONTAINS, () -> new StringContainsConstraintProcessor(message.getContentRaw())
            );

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("fullname"), ctx -> PlaceholderResult.value(user.getAsTag()),
                    Discord4Fabric.id("nickname"), ctx -> PlaceholderResult.value(user.getName()),
                    Discord4Fabric.id("discriminator"), ctx -> PlaceholderResult.value(user.getDiscriminator()),
                    Discord4Fabric.id("message"), ctx -> PlaceholderResult.value(message.getContentRaw())
            );
            customEvents.raiseEvent(CustomEvents.DISCORD_MESSAGE, server, supportedConstraints, placeholders);
        });

        PlayerChatReceivedCallback.EVENT.register((player, message) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), ctx -> PlaceholderResult.value(message)
            );
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintTypes.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintTypes.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintTypes.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintTypes.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.MC_MESSAGE, () -> new StringEqualsConstraintProcessor(message),
                    ConstraintTypes.MC_MESSAGE_CONTAINS, () -> new StringContainsConstraintProcessor(message)
            );
            customEvents.raiseEvent(CustomEvents.MINECRAFT_MESSAGE, player, supportedConstraints, placeholders);
        });

        PlayerAdvancementCallback.EVENT.register((player, advancement) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("title"), ctx -> PlaceholderResult.value(advancement.getDisplay().getTitle())
            );
            String advancementTitle = advancement.getDisplay().getTitle().getString();

            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintTypes.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintTypes.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintTypes.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintTypes.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.ADVANCEMENT_NAME, () -> new StringEqualsConstraintProcessor(advancementTitle),
                    ConstraintTypes.ADVANCEMENT_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(advancementTitle)
            );
            customEvents.raiseEvent(CustomEvents.ADVANCEMENT, player, supportedConstraints, placeholders);
        });
    }
}
