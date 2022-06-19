package me.reimnop.d4f.utils;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.Placeholders;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.utils.text.TextRegexReplacer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    private Utils() {}

    public static void logException(Exception e) {
        Discord4Fabric.LOGGER.error(e.getMessage());
    }

    public static String getAvatarUrl(UUID uuid) {
        return "https://crafatar.com/avatars/" + uuid.toString() + "?overlay";
    }

    public static String getConfigPath() {
        return FabricLoader.getInstance().getConfigDir() + "/discord4fabric.json";
    }

    public static PlaceholderHandler getPlaceholderHandler(String placeholder, Map<Identifier, PlaceholderHandler> handlers) {
        Identifier id = Identifier.tryParse(placeholder);
        if (handlers.containsKey(id)) {
            return handlers.get(id);
        }
        return (ctx, arg) -> Placeholders.parsePlaceholder(id, arg, ctx);
    }


}
