package me.reimnop.d4f.listeners;

import eu.pb4.placeholders.api.PlaceholderContext;
import me.reimnop.d4f.customevents.CustomEvents;
import me.reimnop.d4f.customevents.constraints.Constraint;
import me.reimnop.d4f.customevents.constraints.Constraints;
import me.reimnop.d4f.customevents.constraints.LinkedAccountConstraint;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.Map;

public final class CustomEventsHandler {
    private CustomEventsHandler() {}

    public static void init(CustomEvents customEvents) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlaceholderContext placeholderContext = PlaceholderContext.of(handler.player);
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(handler.player.getUuid())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_JOIN, placeholderContext, supportedConstraints);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlaceholderContext placeholderContext = PlaceholderContext.of(handler.player);
            Map<String, Constraint> supportedConstraints = Map.of(
                    Constraints.LINKED_ACCOUNT, new LinkedAccountConstraint(handler.player.getUuid())
            );
            customEvents.raiseEvent(CustomEvents.PLAYER_LEAVE, placeholderContext, supportedConstraints);
        });
    }
}
