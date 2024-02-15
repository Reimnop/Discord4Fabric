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
import me.reimnop.d4f.events.PlayerDeathCallback;
import me.reimnop.d4f.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

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
                    ConstraintTypes.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintTypes.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintTypes.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintTypes.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
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
                    ConstraintTypes.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(player.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(player.getUuid()),
                    ConstraintTypes.OPERATOR, () -> new OperatorConstraintProcessor(player),
                    ConstraintTypes.MC_UUID, () -> new MinecraftUuidConstraintProcessor(player.getUuid()),
                    ConstraintTypes.MC_NAME, () -> new StringEqualsConstraintProcessor(player.getName().getString()),
                    ConstraintTypes.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(player.getName().getString())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_LEAVE, placeholderContext, supportedConstraints);
        });

        PlayerDeathCallback.EVENT.register((player, source, deathMessage) -> {
            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player)) {
                return;
            }
            PlaceholderContext placeholderContext = PlaceholderContext.of(player);

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("message"), (ctx, arg) -> PlaceholderResult.value(deathMessage)
            );
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
            customEvents.raiseEvent(CustomEvents.PLAYER_DEATH, placeholderContext, supportedConstraints, placeholders);
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
                    ConstraintTypes.DISCORD_ID, () -> new LongEqualsConstraintProcessor(user.getIdLong()),
                    ConstraintTypes.DISCORD_NAME, () -> new StringEqualsConstraintProcessor(username),
                    ConstraintTypes.DISCORD_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(username),
                    ConstraintTypes.DISCORD_MESSAGE, () -> new StringEqualsConstraintProcessor(message.getContentRaw()),
                    ConstraintTypes.DISCORD_MESSAGE_CONTAINS, () -> new StringContainsConstraintProcessor(message.getContentRaw())
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
                    ConstraintTypes.LINKED_ACCOUNT, () -> new LinkedAccountConstraintProcessor(sender.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK, () -> new LinkedAccountNickConstraintProcessor(sender.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_NICK_CONTAINS, () -> new LinkedAccountNickContainsConstraintProcessor(sender.getUuid()),
                    ConstraintTypes.LINKED_ACCOUNT_HAS_ROLE, () -> new LinkedAccountHasRoleConstraintProcessor(sender.getUuid()),
                    ConstraintTypes.OPERATOR, () -> new OperatorConstraintProcessor(sender),
                    ConstraintTypes.MC_UUID, () -> new MinecraftUuidConstraintProcessor(sender.getUuid()),
                    ConstraintTypes.MC_NAME, () -> new StringEqualsConstraintProcessor(sender.getName().getString()),
                    ConstraintTypes.MC_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(sender.getName().getString()),
                    ConstraintTypes.MC_MESSAGE, () -> new StringEqualsConstraintProcessor(message.getContent().getString()),
                    ConstraintTypes.MC_MESSAGE_CONTAINS, () -> new StringContainsConstraintProcessor(message.getContent().getString())
            );
            customEvents.raiseEvent(CustomEvents.MINECRAFT_MESSAGE, placeholderContext, supportedConstraints, placeholders);
        });

        PlayerAdvancementCallback.EVENT.register((player, advancement) -> {
            // Vanish compatibility
            if (Compatibility.isPlayerVanished(player)) {
                return;
            }
            PlaceholderContext placeholderContext = PlaceholderContext.of(player);
            Optional<AdvancementDisplay> advancementDisplay = advancement.display();
            String advancementTitle;
            if (advancementDisplay.isPresent()) {
                advancementTitle = advancementDisplay.get().getTitle().getString();
            } else {
                advancementTitle = "Unknown advancement";
            }

            Map<Identifier, PlaceholderHandler> placeholders = Map.of(
                    Discord4Fabric.id("title"), (ctx, arg) -> PlaceholderResult.value(advancementTitle)
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
                    ConstraintTypes.ADVANCEMENT_NAME, () -> new StringEqualsConstraintProcessor(advancementTitle),
                    ConstraintTypes.ADVANCEMENT_NAME_CONTAINS, () -> new StringContainsConstraintProcessor(advancementTitle)
            );
            customEvents.raiseEvent(CustomEvents.ADVANCEMENT, placeholderContext, supportedConstraints, placeholders);
        });
    }
}
