package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.PlaceholderHandler;
import me.reimnop.d4f.Discord4Fabric;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class StringEqualsConstraintProcessor implements ConstraintProcessor {
    private final String valueA;
    private String valueB = null;

    public StringEqualsConstraintProcessor(String valueA) {
        this.valueA = valueA;
    }

    @Override
    public void loadArguments(List<String> arguments) {
        if (arguments.size() == 0) {
            Discord4Fabric.LOGGER.warn("Too few arguments for string comparison constraint!");
        } else if (arguments.size() > 1) {
            Discord4Fabric.LOGGER.warn("Too many arguments for string comparison constraint!");
        }
        valueB = arguments.get(0);
    }

    @Override
    public boolean satisfied() {
        return valueA.equalsIgnoreCase(valueB);
    }

    @Nullable
    @Override
    public Map<Identifier, PlaceholderHandler> getHandlers() {
        return null;
    }
}
