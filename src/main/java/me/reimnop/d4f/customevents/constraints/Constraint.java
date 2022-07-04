package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface Constraint {
    boolean satisfied();
    Map<Identifier, PlaceholderHandler> getHandlers();
}
