package me.reimnop.d4f.customevents.actions;

import java.util.HashMap;
import java.util.Map;

public final class ModActions {
    private ModActions() {}

    private static final Map<String, Action> actions = new HashMap<>();

    public static void init() {
        put("run_command", new RunCommandAction());
        put("send_discord_message", new SendDiscordMessageAction());
        put("send_minecraft_message", new SendMinecraftMessageAction());
        put("grant_role", new GrantRoleAction());
        put("revoke_role", new RevokeRoleAction());
    }

    public static Action get(String id) {
        return actions.get(id);
    }

    public static void put(String id, Action action) {
        actions.put(id, action);
    }
}
