package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.PlaceholderHandler;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Map;

public interface Constraint {
    boolean satisfied();

    @Nullable
    Map<Identifier, PlaceholderHandler> getHandlers();
}
