package me.reimnop.d4f.customevents.actions;

import com.google.gson.JsonElement;
import me.reimnop.d4f.customevents.ActionContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

public class RunCommandAction implements Action {
    @Override
    public void runAction(JsonElement value, ActionContext context) {
        MinecraftDedicatedServer server = (MinecraftDedicatedServer) FabricLoader.getInstance().getGameInstance();
        String cmd = context.parsePlaceholder(value.getAsString()).getString();
        server.enqueueCommand(cmd, server.getCommandSource());
    }
}
