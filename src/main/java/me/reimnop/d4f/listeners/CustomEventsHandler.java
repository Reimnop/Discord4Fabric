package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.vanish.Vanish;
import me.reimnop.d4f.events.PlayerConnectedCallback;
import me.reimnop.d4f.events.PlayerDisconnectedCallback;
import me.reimnop.d4f.utils.Compatibility;
import me.reimnop.d4f.Config;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.CustomEvents;
import me.reimnop.d4f.customevents.constraints.Constraint;
import me.reimnop.d4f.customevents.constraints.Constraints;
import me.reimnop.d4f.customevents.constraints.LinkedAccountConstraint;
import me.reimnop.d4f.customevents.constraints.OperatorConstraint;
import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import me.reimnop.d4f.events.PlayerAdvancementCallback;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(player.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(player)
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_JOIN, placeholderContext, supportedConstraints);
        });

        PlayerDisconnectedCallback.EVENT.register((player, server, fromVanish) -> {
            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player) && !fromVanish) {
                return;
            }

            PlaceholderContext placeholderContext = PlaceholderContext.of(player);
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(player.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(player)
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
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(sender.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(sender)
            );
            customEvents.raiseEvent(CustomEvents.MINECRAFT_MESSAGE, placeholderContext, supportedConstraints, placeholders);
        });

        PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
            PlaceholderContext placeholderContext = PlaceholderContext.of(playerEntity);
            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("title"), (ctx, arg) -> PlaceholderResult.value(advancement.getDisplay().getTitle())
            );
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(playerEntity.getUuid()),
                    Constraints.OPERATOR, new OperatorConstraint(playerEntity)
            );
            customEvents.raiseEvent(CustomEvents.ADVANCEMENT, placeholderContext, supportedConstraints, placeholders);
        });
    }
}
