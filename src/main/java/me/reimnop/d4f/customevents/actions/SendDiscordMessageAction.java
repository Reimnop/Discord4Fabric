package me.reimnop.d4f.customevents.actions;

import com.google.gson.JsonElement;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.ActionContext;

public class SendDiscordMessageAction implements Action {
    @Override
    public void runAction(JsonElement value, ActionContext context) {
        Discord4Fabric.DISCORD.sendPlainMessage(context.parsePlaceholder(value.getAsString()));
    }
}
