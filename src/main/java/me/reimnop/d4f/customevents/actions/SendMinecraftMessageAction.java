package me.reimnop.d4f.customevents.actions;

import com.google.gson.JsonElement;
import me.reimnop.d4f.customevents.ActionContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;

public class SendMinecraftMessageAction implements Action {
    @Override
    public void runAction(JsonElement value, ActionContext context) {
        MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
        server.getPlayerManager().broadcast(context.parsePlaceholder(value.getAsString()), false);
    }
}
