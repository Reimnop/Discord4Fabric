package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.PlaceholderHandler;
import eu.pb4.placeholders.PlaceholderResult;
import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.CustomEvents;
import me.reimnop.d4f.customevents.constraints.Constraint;
import me.reimnop.d4f.customevents.constraints.Constraints;
import me.reimnop.d4f.customevents.constraints.LinkedAccountConstraint;
import me.reimnop.d4f.customevents.constraints.OperatorConstraint;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.events.PlayerChatReceivedCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class CustomEventsHandler {
    private CustomEventsHandler() {}

    public static void init(Config config, CustomEvents customEvents) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(handler.player.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(handler.player)
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_JOIN, handler.player, supportedConstraints);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(handler.player.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(handler.player)
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_LEAVE, handler.player, supportedConstraints);
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
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(player.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(player)
            );
            customEvents.raiseEvent(CustomEvents.MINECRAFT_MESSAGE, player, supportedConstraints, placeholders);
        });

        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("title"), ctx -> PlaceholderResult.value(advancement.getDisplay().getTitle())
            );
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(playerEntity.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(playerEntity)
            );
            customEvents.raiseEvent(CustomEvents.ADVANCEMENT, playerEntity, supportedConstraints, placeholders);
        });
    }
}
