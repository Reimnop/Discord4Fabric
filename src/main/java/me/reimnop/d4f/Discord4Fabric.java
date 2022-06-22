package me.reimnop.d4f;

import me.reimnop.d4f.commands.ModCommands;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.listeners.MinecraftEventListeners;
import me.reimnop.d4f.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;

public class Discord4Fabric implements ModInitializer {
    public static final String MODID = "d4f";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final Config CONFIG = new Config();
    public static Discord DISCORD;

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        try {
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
                return;
            } else {
                CONFIG.readConfig(file);
            }

            initDiscord();
            ModCommands.init();
        } catch (LoginException e) {
            LOGGER.error("Login Failed! Please update your config and restart the server");
        } catch (Exception e) {
            Utils.logException(e);
        }
    }

    private void initDiscord() throws LoginException, GuildException, InterruptedException {
        DISCORD = new Discord(CONFIG);
        DISCORD.initCache();

        MinecraftEventListeners.init(DISCORD, CONFIG);
    }
}
