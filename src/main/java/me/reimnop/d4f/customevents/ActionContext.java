package me.reimnop.d4f.customevents;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import me.reimnop.d4f.utils.Utils;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ActionContext {
    private final PlaceholderContext placeholderContext;
    private final Map<Identifier, PlaceholderHandler> placeholderHandlers;

    public ActionContext(PlaceholderContext placeholderContext) {
        this.placeholderContext = placeholderContext;
        placeholderHandlers = Map.of();
    }

    public ActionContext(PlaceholderContext placeholderContext, Map<Identifier, PlaceholderHandler> placeholderHandlers) {
        this.placeholderContext = placeholderContext;
        this.placeholderHandlers = placeholderHandlers;
    }

    public Text parsePlaceholder(Text text) {
        return Placeholders.parseText(
                text,
                placeholderContext,
                Placeholders.PLACEHOLDER_PATTERN,
                placeholder -> Utils.getPlaceholderHandler(placeholder, placeholderHandlers)
        );
    }

    public Text parsePlaceholder(String value) {
        return parsePlaceholder(TextParserUtils.formatText(value));
    }
}
