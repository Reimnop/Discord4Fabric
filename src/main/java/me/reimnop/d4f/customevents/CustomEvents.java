package me.reimnop.d4f.customevents;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.constraints.ConstraintProcessorFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CustomEvents {
    private static class ExecutableEvent {
        private final ConstraintList constraints;
        private final ActionList actions;

        public ExecutableEvent(Map<String, ConstraintProcessorFactory> supportedConstraints, JsonObject jsonObject) {
            constraints = new ConstraintList(supportedConstraints, jsonObject.get("requires").getAsJsonArray());
            actions = new ActionList(jsonObject.get("actions").getAsJsonArray());
        }

        public void execute(PlaceholderContext placeholderContext, Map<Identifier, PlaceholderHandler> placeholderHandlers) {
            // check constraints
            for (Constraint constraint : constraints.constraints) {
                if (!constraint.satisfied()) {
                    return;
                }

                Map<Identifier, PlaceholderHandler> constraintPlaceholders = constraint.getHandlers();

                if (constraintPlaceholders != null && !constraint.negated) {
                    for (Identifier identifier : constraintPlaceholders.keySet()) {
                        if (placeholderHandlers.containsKey(identifier)) {
                            continue;
                        }
                        placeholderHandlers.put(identifier, constraintPlaceholders.get(identifier));
                    }
                }
            }

            ActionContext context = new ActionContext(placeholderContext, placeholderHandlers);
            actions.runActions(context);
        }
    }

    public static final String PLAYER_JOIN = "player_join";
    public static final String PLAYER_LEAVE = "player_leave";
    public static final String PLAYER_DEATH = "player_death";
    public static final String SERVER_START = "server_start";
    public static final String SERVER_STOP = "server_stop";
    public static final String DISCORD_MESSAGE = "discord_message";
    public static final String MINECRAFT_MESSAGE = "minecraft_message";
    public static final String ADVANCEMENT = "advancement";

    private final Map<String, List<JsonObject>> eventNameToEventJsons = new HashMap<>();

    public CustomEvents() {
    }

    public void read(File file) throws IOException {
        FileReader reader = new FileReader(file);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        eventNameToEventJsons.clear();

        for (String key : jsonObject.keySet()) {
            JsonElement element = jsonObject.get(key);

            if (element.isJsonObject()) {
                eventNameToEventJsons.put(key, List.of(element.getAsJsonObject()));
            } else if (element.isJsonArray()) {
                List<JsonObject> jsonObjects = new ArrayList<>();
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    if (!jsonElement.isJsonObject()) {
                        Discord4Fabric.LOGGER.warn("Invalid custom event!");
                        return;
                    }
                    jsonObjects.add(jsonElement.getAsJsonObject());
                }
                eventNameToEventJsons.put(key, jsonObjects);
            } else {
                Discord4Fabric.LOGGER.warn("Invalid custom event!");
            }
        }
    }

    public void raiseEvent(
            String id,
            PlaceholderContext placeholderContext,
            @Nullable Map<String, ConstraintProcessorFactory> supportedConstraints,
            @Nullable Map<Identifier, PlaceholderHandler> externalHandlers) {

        if (!eventNameToEventJsons.containsKey(id)) {
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

        for (JsonObject eventJson : eventNameToEventJsons.get(id)) {
            ExecutableEvent executableEvent = new ExecutableEvent(supportedConstraints, eventJson);
            executableEvent.execute(placeholderContext, placeholderHandlers);
        }
    }

    public void raiseEvent(String id, PlaceholderContext placeholderContext, @Nullable Map<String, ConstraintProcessorFactory> supportedConstraints) {
        raiseEvent(id, placeholderContext, supportedConstraints, null);
    }
}
