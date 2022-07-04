package me.reimnop.d4f.customevents;

import me.reimnop.d4f.customevents.actions.Action;

import java.util.HashMap;
import java.util.Map;

public final class ModActions {
    private ModActions() {}

    private static final Map<String, Action> actions = new HashMap<>();

    public static Action get(String id) {
        return actions.get(id);
    }

    public static void put(String id, Action action) {
        actions.put(id, action);
    }
}
