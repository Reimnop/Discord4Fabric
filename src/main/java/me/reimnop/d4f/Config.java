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
    public String serverStartMessage = ":white_check_mark: **Server started!**";
    public String serverStopMessage = ":negative_squared_cross_mark: **Server stopped!**";
    public Boolean announcePlayerJoinLeave = true;
    public String playerJoinMessage = "%player:name% joined the game";
    public String playerJoinDescription = "Welcome to the server :wave:";
    public String playerLeftMessage = "%player:name% left the game";
    public String playerLeftDescription = "See you again :wave:";
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
    public String discordPingFormat = "<blue>@%d4f:fullname%</blue>";
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
        jsonObject.addProperty("player_join_description", playerJoinDescription);
        jsonObject.addProperty("player_left", playerLeftMessage);
        jsonObject.addProperty("player_left_description", playerLeftDescription);
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
        jsonObject.addProperty("discord_ping", discordPingFormat);
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

        token = getStringOrDefault(obj, "token", token);
        webhookUrl = getStringOrDefault(obj, "webhook_url", webhookUrl);
        guildId = getLongOrDefault(obj, "guild_id", guildId);
        channelId = getLongOrDefault(obj, "channel_id", channelId);
        announceServerStartStop = getBooleanOrDefault(obj, "announce_server_start_stop", announceServerStartStop);
        serverStartMessage = getStringOrDefault(obj, "server_start", serverStartMessage);
        serverStopMessage = getStringOrDefault(obj, "server_stop", serverStopMessage);
        announcePlayerJoinLeave = getBooleanOrDefault(obj, "announce_player_join_leave", announcePlayerJoinLeave);
        playerJoinMessage = getStringOrDefault(obj, "player_join", playerJoinMessage);
        playerJoinDescription = getStringOrDefault(obj, "player_join_description", playerJoinDescription);
        playerLeftMessage = getStringOrDefault(obj, "player_left", playerLeftMessage);
        playerLeftDescription = getStringOrDefault(obj, "player_left_description", playerLeftDescription);
        announcePlayerDeath = getBooleanOrDefault(obj, "announce_player_death", announcePlayerDeath);
        deathMessage = getStringOrDefault(obj, "death", deathMessage);
        announceAdvancement = getBooleanOrDefault(obj, "announce_advancement", announceAdvancement);
        advancementGoalTitle = getStringOrDefault(obj, "advancement_goal", advancementGoalTitle);
        advancementGoalDescription = getStringOrDefault(obj, "advancement_goal_desc", advancementGoalDescription);
        advancementTaskTitle = getStringOrDefault(obj, "advancement_task", advancementTaskTitle);
        advancementTaskDescription = getStringOrDefault(obj, "advancement_task_desc", advancementTaskDescription);
        advancementChallengeTitle = getStringOrDefault(obj, "advancement_challenge", advancementChallengeTitle);
        advancementChallengeDescription = getStringOrDefault(obj, "advancement_challenge_desc", advancementChallengeDescription);
        discordToMinecraftMessage = getStringOrDefault(obj, "discord_to_mc", discordToMinecraftMessage);
        discordName = getStringOrDefault(obj, "discord_name", discordName);
        minecraftToDiscordMessage = getStringOrDefault(obj, "mc_to_discord", minecraftToDiscordMessage);
        discordPingFormat = getStringOrDefault(obj, "discord_ping", discordPingFormat);
        updateInterval = getIntOrDefault(obj, "update_interval", updateInterval);
        status = getStringOrDefault(obj, "status", status);

        reader.close();
    }

    private Boolean getBooleanOrDefault(JsonObject obj, String name, Boolean def) {
        return obj.has(name) ? obj.get(name).getAsBoolean() : def;
    }

    private String getStringOrDefault(JsonObject obj, String name, String def) {
        return obj.has(name) ? obj.get(name).getAsString() : def;
    }

    private Integer getIntOrDefault(JsonObject obj, String name, Integer def) {
        return obj.has(name) ? obj.get(name).getAsInt() : def;
    }

    private Long getLongOrDefault(JsonObject obj, String name, Long def) {
        return obj.has(name) ? obj.get(name).getAsLong() : def;
    }
}
