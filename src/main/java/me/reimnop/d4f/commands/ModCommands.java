package me.reimnop.d4f.commands;

import me.reimnop.d4f.AccountLinking;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.File;

public final class ModCommands {
    private ModCommands() {}

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                    CommandManager.literal("discord4fabric")
                            .then(CommandManager.literal("link")
                                    .executes(context -> {
                                        ServerPlayerEntity serverPlayer = context.getSource().getPlayer();
                                        AccountLinking.QueuingResult result = Discord4Fabric.ACCOUNT_LINKING.tryQueueForLinking(serverPlayer.getUuid());
                                        switch (result) {
                                            case ACCOUNT_LINKED -> context.getSource().sendError(new LiteralText("Your account was already linked!"));
                                            case ACCOUNT_QUEUED -> context.getSource().sendError(new LiteralText("Your account is already being linked! Your linking code is " + Discord4Fabric.ACCOUNT_LINKING.getCode(serverPlayer.getUuid())));
                                            case SUCCESS -> {
                                                String code = Discord4Fabric.ACCOUNT_LINKING.getCode(serverPlayer.getUuid());

                                                MutableText text = new LiteralText("");
                                                text.append(new LiteralText("Your linking code is ").formatted(Formatting.GRAY));
                                                text.append(new LiteralText(code)
                                                                .setStyle(Style.EMPTY
                                                                        .withFormatting(Formatting.BLUE, Formatting.UNDERLINE)
                                                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code))
                                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Copy to clipboard")))));
                                                text.append(new LiteralText(" (click to copy)\nPlease DM the bot this linking code to finish the linking process")
                                                        .formatted(Formatting.GRAY));

                                                context.getSource().sendFeedback(text, false);
                                            }
                                        }
                                        return 1;
                                    }))
                            .then(CommandManager.literal("unlink")
                                    .executes(context -> {
                                        ServerPlayerEntity serverPlayer = context.getSource().getPlayer();
                                        AccountLinking.UnlinkingResult result = Discord4Fabric.ACCOUNT_LINKING.tryUnlinkAccount(serverPlayer.getUuid());
                                        switch (result) {
                                            case ACCOUNT_UNLINKED -> context.getSource().sendError(new LiteralText("Your account was not linked!"));
                                            case SUCCESS -> {
                                                if (Discord4Fabric.CONFIG.requiresLinkedAccount) {
                                                    Discord4Fabric.kickForUnlinkedAccount(serverPlayer);
                                                    return 1;
                                                }

                                                context.getSource().sendFeedback(new LiteralText("Your account was successfully unlinked!"), false);
                                            }
                                        }
                                        return 1;
                                    }))
                            .then(CommandManager.literal("refresh_cache")
                                    .requires(source -> source.hasPermissionLevel(4))
                                    .executes(context -> {
                                        try {
                                            context.getSource().sendFeedback(
                                                    new LiteralText("Refreshing cache!"),
                                                    false
                                            );
                                            Discord4Fabric.DISCORD.initCache();
                                            return 1;
                                        } catch (GuildException e) {
                                            context.getSource().sendError(new LiteralText("An unexpected error occurred! Check logs for more details"));
                                            Utils.logException(e);
                                            return 0;
                                        }
                                    }))
                            .then(CommandManager.literal("reload")
                                    .requires(source -> source.hasPermissionLevel(4))
                                    .executes(context -> {
                                        try {
                                            File configFile = new File(Utils.getConfigPath());
                                            if (configFile.exists()) {
                                                context.getSource().sendFeedback(
                                                        new LiteralText("Reloading config!"),
                                                        false
                                                );
                                                Discord4Fabric.CONFIG.readConfig(configFile);
                                            } else {
                                                context.getSource().sendFeedback(
                                                        new LiteralText("Config file not found! Writing from memory"),
                                                        false
                                                );
                                                Discord4Fabric.CONFIG.writeConfig(configFile);
                                            }
                                            return 1;
                                        } catch (Exception e) {
                                            context.getSource().sendError(new LiteralText("An unexpected error occurred! Check logs for more details"));
                                            Utils.logException(e);
                                            return 0;
                                        }
                                    }))
                            .then(CommandManager.literal("reload_custom_events")
                                    .requires(source -> source.hasPermissionLevel(4))
                                    .executes(context -> {
                                        try {
                                            File file = new File(Utils.getCustomEventsPath());
                                            if (file.exists()) {
                                                context.getSource().sendFeedback(
                                                        new LiteralText("Reloading custom events!"),
                                                        false
                                                );
                                                Discord4Fabric.CUSTOM_EVENTS.read(file);
                                            } else {
                                                context.getSource().sendFeedback(
                                                        new LiteralText("Custom events file not found!"),
                                                        false
                                                );
                                            }
                                            return 1;
                                        } catch (Exception e) {
                                            context.getSource().sendError(new LiteralText("An unexpected error occurred! Check logs for more details"));
                                            Utils.logException(e);
                                            return 0;
                                        }
                                    }))
                            .then(CommandManager.literal("update")
                                    .requires(source -> source.hasPermissionLevel(4))
                                    .executes(context -> {
                                        try {
                                            File configFile = new File(Utils.getConfigPath());
                                            context.getSource().sendFeedback(
                                                    new LiteralText("Updating config!"),
                                                    false
                                            );
                                            Discord4Fabric.CONFIG.writeConfig(configFile);
                                            return 1;
                                        } catch (Exception e) {
                                            context.getSource().sendError(new LiteralText("An unexpected error occurred! Check logs for more details"));
                                            Utils.logException(e);
                                            return 0;
                                        }
                                    }))
            );
        });
    }
}
