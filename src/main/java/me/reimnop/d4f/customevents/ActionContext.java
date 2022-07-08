package me.reimnop.d4f.customevents;

import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.PlaceholderHandler;
import eu.pb4.placeholders.TextParser;
import me.reimnop.d4f.utils.Utils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ActionContext {
    private final Object object;
    private final Map<Identifier, PlaceholderHandler> placeholderHandlers;

    public ActionContext(Object object) {
        this.object = object;
        placeholderHandlers = Map.of();
    }

    public ActionContext(Object object, Map<Identifier, PlaceholderHandler> placeholderHandlers) {
        this.object = object;
        this.placeholderHandlers = placeholderHandlers;
    }

    public Text parsePlaceholder(Text text) {
        if (object instanceof ServerPlayerEntity player) {
            return PlaceholderAPI.parseTextCustom(
                    text,
                    player,
                    Utils.getPlaceholderHandlerMap(placeholderHandlers),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );
        } else if (object instanceof MinecraftServer server) {
            return PlaceholderAPI.parseTextCustom(
                    text,
                    server,
                    Utils.getPlaceholderHandlerMap(placeholderHandlers),
                    PlaceholderAPI.PLACEHOLDER_PATTERN
            );
        }
        return null;
    }

    public Text parsePlaceholder(String value) {
        return parsePlaceholder(TextParser.parse(value));
    }
}
