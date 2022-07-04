package me.reimnop.d4f.customevents;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.actions.Action;
import me.reimnop.d4f.customevents.actions.ModActions;

import java.util.ArrayList;
import java.util.List;

public class ActionList {
    private static class ActionValuePair {
        public Action action;
        public JsonElement value;

        public ActionValuePair(Action action, JsonElement value) {
            this.action = action;
            this.value = value;
        }
    }

    private final List<ActionValuePair> actions = new ArrayList<>();

    public ActionList(JsonArray jsonArray) {
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String id = jsonObject.get("id").getAsString();
            Action action = ModActions.get(id);
            if (action == null) {
                Discord4Fabric.LOGGER.warn(String.format("Unknown action id '%s', skipping", id));
                continue;
            }
            actions.add(new ActionValuePair(action, jsonObject.get("value")));
        }
    }

    public void runActions(ActionContext context) {
        for (ActionValuePair pair : actions) {
            pair.action.runAction(pair.value, context);
        }
    }
}
