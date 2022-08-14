package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.PlaceholderHandler;
import eu.pb4.placeholders.PlaceholderResult;
import me.reimnop.d4f.customevents.constraints.*;
import me.reimnop.d4f.events.PlayerConnectedCallback;
import me.reimnop.d4f.events.PlayerDisconnectedCallback;
import me.reimnop.d4f.utils.Compatibility;
import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.CustomEvents;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.events.PlayerChatReceivedCallback;
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
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_JOIN, player, supportedConstraints);
        });

        PlayerDisconnectedCallback.EVENT.register((player, server, fromVanish) -> {
            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player) && !fromVanish) {
                return;
            }
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
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

            MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("fullname"), ctx -> PlaceholderResult.value(user.getAsTag()),
                    Discord4Fabric.id("nickname"), ctx -> PlaceholderResult.value(user.getName()),
                    Discord4Fabric.id("discriminator"), ctx -> PlaceholderResult.value(user.getDiscriminator()),
                    Discord4Fabric.id("message"), ctx -> PlaceholderResult.value(message.getContentRaw())
            );
            customEvents.raiseEvent(CustomEvents.DISCORD_MESSAGE, server, null, placeholders);
        });

        PlayerChatReceivedCallback.EVENT.register((player, message) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), ctx -> PlaceholderResult.value(message)
            );
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString()),
                    ConstraintProcessors.MC_MESSAGE, () -> new StringEqualsConstraintProcessor(message),
                    ConstraintProcessors.MC_MESSAGE_CONTAINS, () -> new StringContainsConstraintProcessor(message)
            );
            customEvents.raiseEvent(CustomEvents.MINECRAFT_MESSAGE, player, supportedConstraints, placeholders);
        });

        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("title"), ctx -> PlaceholderResult.value(advancement.getDisplay().getTitle())
            );
            Map<String, ConstraintProcessorFactory> supportedConstraints = Map.of(
                    ConstraintProcessors.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(playerEntity.getUuid()),
                    ConstraintProcessors.OPERATOR, () -> new OperatorConstraintProcessor(playerEntity),
                    ConstraintProcessors.MC_NAME, () -> new StringEqualsConstraintProcessor(playerEntity.getName().getString()),
                    ConstraintProcessors.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(playerEntity.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.ADVANCEMENT, playerEntity, supportedConstraints, placeholders);
        });
    }
}
