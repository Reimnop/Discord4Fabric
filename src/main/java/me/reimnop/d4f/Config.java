package me.reimnop.d4f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    public String token = "";
    public String webhookUrl = "";
    public Long guildId = 0L;
    public Long channelId = 0L;
    public String serverStartMessage = "✅ **Server started!**";
    public String serverStopMessage = "❎ **Server stopped!**";
    public String playerJoinMessage = "%player:name% joined the game";
    public String playerLeftMessage = "%player:name% left the game";
    public String advancementGoalMessage = "%player:name% has reached the goal [%d4f:message%]";
    public String advancementTaskMessage = "%player:name% has made the advancement [%d4f:message%]";
    public String advancementChallengeMessage = "%player:name% has completed the challenge [%d4f:message%]";
    public String discordToMinecraftMessage = "[%d4f:nickname% on Discord] %d4f:message%";
    public String minecraftToDiscordMessage = "%d4f:message%";
    public String deathMessage = "%d4f:message%";
    public Integer updateInterval = 40;
    public String status = "Total player: %server:online%/%server:max_players% | Server TPS: %server:tps%";

    public void writeConfig(File file) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("webhook_url", webhookUrl);
        jsonObject.addProperty("guild_id", guildId);
        jsonObject.addProperty("channel_id", channelId);
        jsonObject.addProperty("server_start", serverStartMessage);
        jsonObject.addProperty("server_stop", serverStopMessage);
        jsonObject.addProperty("player_join", playerJoinMessage);
        jsonObject.addProperty("player_left", playerLeftMessage);
        jsonObject.addProperty("advancement_goal", advancementGoalMessage);
        jsonObject.addProperty("advancement_task", advancementTaskMessage);
        jsonObject.addProperty("advancement_challenge", advancementChallengeMessage);
        jsonObject.addProperty("discord_to_mc", discordToMinecraftMessage);
        jsonObject.addProperty("mc_to_discord", minecraftToDiscordMessage);
        jsonObject.addProperty("death", deathMessage);
        jsonObject.addProperty("update_interval", updateInterval);
        jsonObject.addProperty("status", status);

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        FileWriter writer = new FileWriter(file);
        gson.toJson(jsonObject, writer);
        writer.close();
    }

    public void readConfig(File file) throws IOException {
        FileReader reader = new FileReader(file);

        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(reader, JsonObject.class);

        token = obj.get("token").getAsString();
        webhookUrl = obj.get("webhook_url").getAsString();
        guildId = obj.get("guild_id").getAsLong();
        channelId = obj.get("channel_id").getAsLong();
        serverStartMessage = obj.get("server_start").getAsString();
        serverStopMessage = obj.get("server_stop").getAsString();
        playerJoinMessage = obj.get("player_join").getAsString();
        playerLeftMessage = obj.get("player_left").getAsString();
        advancementGoalMessage = obj.get("advancement_goal").getAsString();
        advancementTaskMessage = obj.get("advancement_task").getAsString();
        advancementChallengeMessage = obj.get("advancement_challenge").getAsString();
        discordToMinecraftMessage = obj.get("discord_to_mc").getAsString();
        minecraftToDiscordMessage = obj.get("mc_to_discord").getAsString();
        deathMessage = obj.get("death").getAsString();
        updateInterval = obj.get("update_interval").getAsInt();
        status = obj.get("status").getAsString();

        reader.close();
    }
}
