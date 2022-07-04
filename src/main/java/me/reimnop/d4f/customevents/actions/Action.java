package me.reimnop.d4f.customevents.actions;

import com.google.gson.JsonElement;
import me.reimnop.d4f.customevents.ActionContext;

public interface Action {
    void runAction(JsonElement value, ActionContext context);
}
