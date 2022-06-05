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
    public Boolean announceServerStartStop = true;
    public String serverStartMessage = "✅ **Server started!**";
    public String serverStopMessage = "❎ **Server stopped!**";
    public Boolean announcePlayerJoinLeave = true;
    public String playerJoinMessage = "%player:name% joined the game";
    public String playerLeftMessage = "%player:name% left the game";
    public Boolean announcePlayerDeath = true;
    public String deathMessage = "%d4f:reason%";
    public Boolean announceAdvancement = true;
    public String advancementGoalTitle = "%player:name% has reached the goal [%d4f:title%]";
    public String advancementGoalDescription = "%d4f:description%";
    public String advancementTaskTitle = "%player:name% has made the advancement [%d4f:title%]";
    public String advancementTaskDescription = "%d4f:description%";
    public String advancementChallengeTitle = "%player:name% has completed the challenge [%d4f:title%]";
    public String advancementChallengeDescription = "%d4f:description%";
    public String discordToMinecraftMessage = "[%d4f:nickname% on Discord] %d4f:message%";
    public String discordName = "%player:name%";
    public String minecraftToDiscordMessage = "%d4f:message%";
    public Integer updateInterval = 100;
    public String status = "Total player: %server:online%/%server:max_players% | Server TPS: %server:tps%";

    public void writeConfig(File file) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("webhook_url", webhookUrl);
        jsonObject.addProperty("guild_id", guildId);
        jsonObject.addProperty("channel_id", channelId);
        jsonObject.addProperty("announce_server_start_stop", announceServerStartStop);
        jsonObject.addProperty("server_start", serverStartMessage);
        jsonObject.addProperty("server_stop", serverStopMessage);
        jsonObject.addProperty("announce_player_join_leave", announcePlayerJoinLeave);
        jsonObject.addProperty("player_join", playerJoinMessage);
        jsonObject.addProperty("player_left", playerLeftMessage);
        jsonObject.addProperty("announce_player_death", announcePlayerDeath);
        jsonObject.addProperty("death", deathMessage);
        jsonObject.addProperty("announce_advancement", announceAdvancement);
        jsonObject.addProperty("advancement_goal", advancementGoalTitle);
        jsonObject.addProperty("advancement_goal_desc", advancementGoalDescription);
        jsonObject.addProperty("advancement_task", advancementTaskTitle);
        jsonObject.addProperty("advancement_task_desc", advancementTaskDescription);
        jsonObject.addProperty("advancement_challenge", advancementChallengeTitle);
        jsonObject.addProperty("advancement_challenge_desc", advancementChallengeDescription);
        jsonObject.addProperty("discord_to_mc", discordToMinecraftMessage);
        jsonObject.addProperty("discord_name", discordName);
        jsonObject.addProperty("mc_to_discord", minecraftToDiscordMessage);
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
        announceServerStartStop = obj.get("announce_server_start_stop").getAsBoolean();
        serverStartMessage = obj.get("server_start").getAsString();
        serverStopMessage = obj.get("server_stop").getAsString();
        announcePlayerJoinLeave = obj.get("announce_player_join_leave").getAsBoolean();
        playerJoinMessage = obj.get("player_join").getAsString();
        playerLeftMessage = obj.get("player_left").getAsString();
        announcePlayerDeath = obj.get("announce_player_death").getAsBoolean();
        deathMessage = obj.get("death").getAsString();
        announceAdvancement = obj.get("announce_advancement").getAsBoolean();
        advancementGoalTitle = obj.get("advancement_goal").getAsString();
        advancementGoalDescription = obj.get("advancement_goal_desc").getAsString();
        advancementTaskTitle = obj.get("advancement_task").getAsString();
        advancementTaskDescription = obj.get("advancement_task_desc").getAsString();
        advancementChallengeTitle = obj.get("advancement_challenge").getAsString();
        advancementChallengeDescription = obj.get("advancement_challenge_desc").getAsString();
        discordToMinecraftMessage = obj.get("discord_to_mc").getAsString();
        discordName = obj.get("discord_name").getAsString();
        minecraftToDiscordMessage = obj.get("mc_to_discord").getAsString();
        updateInterval = obj.get("update_interval").getAsInt();
        status = obj.get("status").getAsString();

        reader.close();
    }
}
