package me.reimnop.d4f;

import me.reimnop.d4f.actions.ModActions;
import me.reimnop.d4f.actions.RunCommandAction;
import me.reimnop.d4f.commands.ModCommands;
import me.reimnop.d4f.events.CustomEventsHandler;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.listeners.MinecraftEventListeners;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class Discord4Fabric implements ModInitializer {
    public static final String MODID = "d4f";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Config CONFIG = new Config();
    public static Discord DISCORD;
    public static AccountLinking ACCOUNT_LINKING;
    public static CustomEvents CUSTOM_EVENTS;

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void kickForUnlinkedAccount(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        ACCOUNT_LINKING.tryQueueForLinking(uuid);
        String code = ACCOUNT_LINKING.getCode(uuid);

        MutableText reason = Text.empty()
                        .append(Text.literal("This server requires a linked Discord account!\n"))
                        .append(Text.literal("Your linking code is "))
                        .append(Text.literal(code)
                                .formatted(Formatting.BLUE, Formatting.UNDERLINE))
                        .append(Text.literal("\nPlease DM the bot this linking code to link your account"));

        player.networkHandler.disconnect(reason);
    }

    @Override
    public void onInitialize() {
        try {
            if (tryInitConfig()) {
                initDiscord();
                initActions();
                initCustomEvents();
                ModCommands.init();
            }
        } catch (LoginException e) {
            LOGGER.error("Login Failed! Please update your config and restart the server");
        } catch (Exception e) {
            Utils.logException(e);
        }
    }

    private boolean tryInitConfig() throws IOException {
        File file = new File(Utils.getConfigPath());
        if (!file.exists()) {
            CONFIG.writeConfig(file);
            LOGGER.error(
                    String.format(
                            "Config not found! Generated template at %s",
                            file.getAbsolutePath()
                    )
            );
            LOGGER.error("Please update your config and restart the server");
            return false;
        }

        CONFIG.readConfig(file);
        return true;
    }

    private void initDiscord() throws LoginException, GuildException, InterruptedException, IOException {
        ACCOUNT_LINKING = new AccountLinking();

        File file = new File(Utils.getUserdataPath());
        if (file.exists()) {
            ACCOUNT_LINKING.read(file);
        }

        DISCORD = new Discord(CONFIG);
        DISCORD.initCache();

        MinecraftEventListeners.init(DISCORD, ACCOUNT_LINKING, CONFIG);
    }

    private void initActions() {
        ModActions.put("run_command", new RunCommandAction());
    }

    private void initCustomEvents() {
        try {
            File file = new File(Utils.getCustomEventsPath());
            if (file.exists()) {
                CUSTOM_EVENTS = new CustomEvents(file);
                CustomEventsHandler.init(CUSTOM_EVENTS);
            } else {
                // Generate empty events file for the user
                if (file.createNewFile()) {
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write("{}");
                    }
                }
                LOGGER.warn("No events file! Generated template");

                CUSTOM_EVENTS = new CustomEvents();
            }
        } catch (IOException e) {
            Utils.logException(e);
        }
    }
}
