package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import me.reimnop.d4f.Discord4Fabric;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LongEqualsConstraintProcessor implements ConstraintProcessor {
    private final Long valueA;
    private Long valueB = null;

    public LongEqualsConstraintProcessor(Long valueA) {
        this.valueA = valueA;
    }

    @Override
    public void loadArguments(List<String> arguments) {
        if (arguments.size() == 0) {
            Discord4Fabric.LOGGER.warn("Too few arguments for string comparison constraint!");
            return;
        }
        if (arguments.size() > 1) {
            Discord4Fabric.LOGGER.warn("Too many arguments for string comparison constraint!");
            return;
        }
        if (!NumberUtils.isParsable(arguments.get(0))) {
            Discord4Fabric.LOGGER.warn("Argument is not a number!");
            return;
        }
        valueB = Long.parseLong(arguments.get(0));
    }

    @Override
    public boolean satisfied() {
        return Objects.equals(valueA, valueB);
    }

    @Nullable
    @Override
    public Map<Identifier, PlaceholderHandler> getHandlers() {
        return null;
    }
}
