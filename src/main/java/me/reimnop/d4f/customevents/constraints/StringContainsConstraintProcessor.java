package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import me.reimnop.d4f.Discord4Fabric;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class StringContainsConstraintProcessor implements ConstraintProcessor {
    private final String valueA;
    private String valueB = null;

    public StringContainsConstraintProcessor(String valueA) {
        this.valueA = valueA;
    }

    @Override
    public void loadArguments(List<String> arguments) {
        if (arguments.size() == 0) {
            Discord4Fabric.LOGGER.warn("Too few arguments for string contains constraint!");
            return;
        }
        if (arguments.size() > 1) {
            Discord4Fabric.LOGGER.warn("Too many arguments for string contains constraint!");
            return;
        }
        valueB = arguments.get(0);
    }

    @Override
    public boolean satisfied() {
        return StringUtils.containsIgnoreCase(valueA, valueB);
    }

    @Nullable
    @Override
    public Map<Identifier, PlaceholderHandler> getHandlers() {
        return null;
    }
}
