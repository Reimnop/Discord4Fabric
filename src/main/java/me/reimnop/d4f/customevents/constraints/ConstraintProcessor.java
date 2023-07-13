package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface ConstraintProcessor {
    void loadArguments(List<String> arguments);
    boolean satisfied();

    @Nullable
    Map<Identifier, PlaceholderHandler> getHandlers();
}
