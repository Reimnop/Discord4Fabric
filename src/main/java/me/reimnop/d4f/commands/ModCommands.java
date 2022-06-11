package me.reimnop.d4f.commands;

import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import java.io.File;

public final class ModCommands {
    private ModCommands() {}

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("discord4fabric")
                            .then(CommandManager.literal("reload")
                                    .requires(source -> source.hasPermissionLevel(4))
                                    .executes(context -> {
                                        try {
                                            File configFile = new File(Utils.getConfigPath());
                                            if (configFile.exists()) {
                                                context.getSource().sendFeedback(
                                                        Text.literal("Reloading config!"),
                                                        false
                                                );
                                                Discord4Fabric.CONFIG.readConfig(configFile);
                                            } else {
                                                context.getSource().sendFeedback(
                                                        Text.literal("Config file not found! Writing from memory"),
                                                        false
                                                );
                                                Discord4Fabric.CONFIG.writeConfig(configFile);
                                            }
                                            return 1;
                                        } catch (Exception e) {
                                            context.getSource().sendError(Text.literal("An unexpected error occurred! Check logs for more details"));
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
                                                    Text.literal("Updating config!"),
                                                    false
                                            );
                                            Discord4Fabric.CONFIG.writeConfig(configFile);
                                            return 1;
                                        } catch (Exception e) {
                                            context.getSource().sendError(Text.literal("An unexpected error occurred! Check logs for more details"));
                                            Utils.logException(e);
                                            return 0;
                                        }
                                    }))
            );
        });
    }
}
