package me.reimnop.d4f.utils;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.Placeholders;
import me.reimnop.d4f.Discord4Fabric;
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

    public interface TextRegexReplacer {
        Text replace(Matcher match);
    }

    public interface StringRegexReplacer {
        String replace(Matcher match);
    }

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

    public static Text regexDynamicReplaceText(String value, Pattern pattern, TextRegexReplacer replacer) {
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(value);
        MutableText text = Text.empty();
        while (matcher.find()) {
            text
                    .append(value.substring(lastIndex, matcher.start()))
                    .append(replacer.replace(matcher));
            lastIndex = matcher.end();
        }
        if (lastIndex < value.length()) {
            text.append(value.substring(lastIndex));
        }
        return text;
    }

    public static String regexDynamicReplaceString(String value, Pattern pattern, StringRegexReplacer replacer) {
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(value);
        StringBuilder output = new StringBuilder();
        while (matcher.find()) {
            output
                    .append(value, lastIndex, matcher.start())
                    .append(replacer.replace(matcher));
            lastIndex = matcher.end();
        }
        if (lastIndex < value.length()) {
            output.append(value.substring(lastIndex));
        }
        return output.toString();
    }
}
