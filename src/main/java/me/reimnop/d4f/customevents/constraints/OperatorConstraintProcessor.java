package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class OperatorConstraintProcessor implements ConstraintProcessor {

    private final boolean isOp;

    public OperatorConstraintProcessor(ServerPlayerEntity playerEntity) {
        isOp = playerEntity.hasPermissionLevel(4);
    }

    @Override
    public void loadArguments(List<String> arguments) {
    }

    @Override
    public boolean satisfied() {
        return isOp;
    }

    @Nullable
    @Override
    public Map<Identifier, PlaceholderHandler> getHandlers() {
        return null;
    }
}
