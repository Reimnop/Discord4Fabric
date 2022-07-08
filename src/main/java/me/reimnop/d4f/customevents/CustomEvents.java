package me.reimnop.d4f.customevents;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.placeholders.PlaceholderContext;
import eu.pb4.placeholders.PlaceholderHandler;
import eu.pb4.placeholders.PlaceholderResult;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.constraints.Constraint;
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
        private static class ConstraintId {
            public final String id;
            public final boolean negated;

            public ConstraintId(String str) {
                if (str.startsWith("!")) {
                    id = str.substring(1);
                    negated = true;
                } else {
                    id = str;
                    negated = false;
                }
            }
        }

        public final Set<ConstraintId> contraintIds;
        public final ActionList actionList;

        public ConstraintActionListPair(JsonObject jsonObject) {
            contraintIds = new HashSet<>();
            for (JsonElement jsonElement : jsonObject.get("requires").getAsJsonArray()) {
                contraintIds.add(new ConstraintId(jsonElement.getAsString()));
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

    public void read(File file) throws IOException {
        FileReader reader = new FileReader(file);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        constraintActionListPairs.clear();
        for (String key : jsonObject.keySet()) {
            constraintActionListPairs.put(key, new ConstraintActionListPair(jsonObject.get(key).getAsJsonObject()));
        }
    }

    public void raiseEvent(
            String id,
            Object object,
            @Nullable Map<String, Constraint> supportedConstraints,
            @Nullable Map<Identifier, PlaceholderHandler> externalHandlers) {
        if (!constraintActionListPairs.containsKey(id)) {
            return;
        }

        Map<Identifier, PlaceholderHandler> placeholderHandlers = new HashMap<>();

        // Technoblade easter egg
        placeholderHandlers.put(
                Discord4Fabric.id("pig"), ctx -> PlaceholderResult.value(TechnobladeQuoteFactory.getRandomQuote())
        );

        if (externalHandlers != null) {
            placeholderHandlers.putAll(externalHandlers);
        }

        ConstraintActionListPair constraintActionListPair = constraintActionListPairs.get(id);
        if (supportedConstraints != null) {
            for (ConstraintActionListPair.ConstraintId constraintId : constraintActionListPair.contraintIds) {
                if (!supportedConstraints.containsKey(constraintId.id)) {
                    continue;
                }

                Constraint constraint = supportedConstraints.get(constraintId.id);
                if (constraintId.negated == constraint.satisfied()) {
                    return;
                }

                if (!constraintId.negated) {
                    Map<Identifier, PlaceholderHandler> constraintProvidedHandlers = constraint.getHandlers();
                    if (constraintProvidedHandlers != null) {
                        placeholderHandlers.putAll(constraintProvidedHandlers);
                    }
                }
            }
        }

        ActionContext context = new ActionContext(object, placeholderHandlers);
        constraintActionListPair.actionList.runActions(context);
    }

    public void raiseEvent(String id, Object object, @Nullable Map<String, Constraint> supportedConstraints) {
        raiseEvent(id, object, supportedConstraints, null);
    }
}
