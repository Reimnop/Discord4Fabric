package me.reimnop.d4f.utils;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.Placeholders;
import me.reimnop.d4f.Discord4Fabric;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public final class Utils {
    private Utils() {}

    public static void logException(Exception e) {
        e.printStackTrace();
    }

    public static String getAvatarUrl(UUID uuid) {
        return "https://crafatar.com/avatars/" + uuid.toString() + "?overlay";
    }

    public static String getConfigPath() {
        return FabricLoader.getInstance().getConfigDir() + "/discord4fabric.json";
    }

    public static String getUserdataPath() {
        return FabricLoader.getInstance().getConfigDir() + "/d4f_userdata.json";
    }

    public static String getCustomEventsPath() {
        return FabricLoader.getInstance().getConfigDir() + "/d4f_custom_events.json";
    }

    public static PlaceholderHandler getPlaceholderHandler(String placeholder, Map<Identifier, PlaceholderHandler> handlers) {
        Identifier id = Identifier.tryParse(placeholder);
        if (handlers.containsKey(id)) {
            return handlers.get(id);
        }
        return (ctx, arg) -> Placeholders.parsePlaceholder(id, arg, ctx);
    }

    public static String getNameCachePath() {
        return FabricLoader.getInstance().getConfigDir() + "/d4f_name_cache.json";
    }

    public static String getNicknameFromUser(User user) {
        Member member = Discord4Fabric.DISCORD.getMember(user);
        if (member == null) {
            return user.getName();
        }
        return member.getEffectiveName();
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }
}
