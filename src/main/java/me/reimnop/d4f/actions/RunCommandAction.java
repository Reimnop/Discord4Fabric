package me.reimnop.d4f.actions;

import com.google.gson.JsonElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

public class RunCommandAction implements Action {
    @Override
    public void runAction(JsonElement value) {
        MinecraftDedicatedServer server = (MinecraftDedicatedServer) FabricLoader.getInstance().getGameInstance();
        server.enqueueCommand(value.getAsString(), server.getCommandSource());
    }
}
