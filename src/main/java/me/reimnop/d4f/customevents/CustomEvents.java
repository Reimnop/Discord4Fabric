package me.reimnop.d4f.customevents;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.ActionContext;
import me.reimnop.d4f.customevents.ActionList;
import me.reimnop.d4f.customevents.constraints.Constraint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomEvents {
    private static class ConstraintActionListPair {
        public final Set<String> contraintIds;
        public final ActionList actionList;

        public ConstraintActionListPair(JsonObject jsonObject) {
            contraintIds = new HashSet<>();
            for (JsonElement jsonElement : jsonObject.get("requires").getAsJsonArray()) {
                contraintIds.add(jsonElement.getAsString());
            }
            actionList = new ActionList(jsonObject.get("actions").getAsJsonArray());
        }
    }

    public static final String PLAYER_JOIN = "player_join";
    public static final String PLAYER_LEAVE = "player_leave";
    public static final String SERVER_START = "server_start";
    public static final String SERVER_STOP = "server_stop";
    public static final String DISCORD_MESSAGE = "discord_message";
    public static final String MINECRAFT_MESSAGE = "minecraft_message";
    public static final String ADVANCEMENT = "advancement";

    private final Map<String, ConstraintActionListPair> constraintActionListPairs = new HashMap<>();

    public CustomEvents() {
    }

    public CustomEvents(File file) throws IOException {
        FileReader reader = new FileReader(file);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        for (String key : jsonObject.keySet()) {
            constraintActionListPairs.put(key, new ConstraintActionListPair(jsonObject.get(key).getAsJsonObject()));
        }
    }

    public void raiseEvent(
            String id,
            PlaceholderContext placeholderContext,
            @Nullable Map<String, Constraint> supportedConstraints,
            @Nullable Map<Identifier, PlaceholderHandler> externalHandlers) {
        if (!constraintActionListPairs.containsKey(id)) {
            return;
        }

        Map<Identifier, PlaceholderHandler> placeholderHandlers = new HashMap<>();

        // Technoblade easter egg
        placeholderHandlers.put(
                Discord4Fabric.id("pig"), (ctx, arg) -> PlaceholderResult.value(TechnobladeQuoteFactory.getRandomQuote())
        );

        if (externalHandlers != null) {
            placeholderHandlers.putAll(externalHandlers);
        }

        ConstraintActionListPair constraintActionListPair = constraintActionListPairs.get(id);
        if (supportedConstraints != null) {
            for (String constraintId : constraintActionListPair.contraintIds) {
                if (!supportedConstraints.containsKey(constraintId)) {
                    continue;
                }

                Constraint constraint = supportedConstraints.get(constraintId);
                if (!constraint.satisfied()) {
                    return;
                }

                Map<Identifier, PlaceholderHandler> constraintProvidedHandlers = constraint.getHandlers();
                placeholderHandlers.putAll(constraintProvidedHandlers);
            }
        }

        ActionContext context = new ActionContext(placeholderContext, placeholderHandlers);
        constraintActionListPair.actionList.runActions(context);
    }

    public void raiseEvent(String id, PlaceholderContext placeholderContext, @Nullable Map<String, Constraint> supportedConstraints) {
        raiseEvent(id, placeholderContext, supportedConstraints, null);
    }
}
