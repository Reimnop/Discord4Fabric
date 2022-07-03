package me.reimnop.d4f;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.reimnop.d4f.actions.ActionList;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomEvents {
    public static final String PLAYER_JOIN = "player_join";
    public static final String PLAYER_LEAVE = "player_leave";

    private final Map<String, ActionList> actionLists = new HashMap<>();

    public CustomEvents() {
    }

    public CustomEvents(File file) throws IOException {
        FileReader reader = new FileReader(file);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        for (String key : jsonObject.keySet()) {
            actionLists.put(key, new ActionList(jsonObject.get(key).getAsJsonArray()));
        }
    }

    public void raiseEvent(String id) {
        if (actionLists.containsKey(id)) {
            actionLists.get(id).runActions();
        }
    }
}
