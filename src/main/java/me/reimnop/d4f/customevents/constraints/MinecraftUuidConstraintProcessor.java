package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import me.reimnop.d4f.Discord4Fabric;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MinecraftUuidConstraintProcessor implements  ConstraintProcessor {
    private final UUID valueA;
    private UUID valueB = null;

    public MinecraftUuidConstraintProcessor(UUID uuid) {
        valueA = uuid;
    }

    @Override
    public void loadArguments(List<String> arguments) {
        if (arguments.size() == 0) {
            Discord4Fabric.LOGGER.warn("Too few arguments for minecraft uuid constraint!");
            return;
        }
        if (arguments.size() > 1) {
            Discord4Fabric.LOGGER.warn("Too many arguments for minecraft uuid constraint!");
            return;
        }
        try {
            valueB = UUID.fromString(arguments.get(0));
        } catch (IllegalArgumentException e) {
            Discord4Fabric.LOGGER.warn("Not a valid UUID!");
        }
    }

    @Override
    public boolean satisfied() {
        return valueA.equals(valueB);
    }

    @Nullable
    @Override
    public Map<Identifier, PlaceholderHandler> getHandlers() {
        return null;
    }
}
