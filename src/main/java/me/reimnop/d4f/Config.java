package me.reimnop.d4f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    public int numServers = 1;
    public String token = "";
    public Boolean useThread = false;
    public Long consoleGuildId = 0L;
    public Long consoleChannelId = 0L;
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
    public String deathDescription = "Better luck next time!";
    public Boolean announceAdvancement = true;
    public String advancementGoalTitle = "%player:name% has reached the goal [%d4f:title%]";
    public String advancementGoalDescription = "%d4f:description%";
    public String advancementTaskTitle = "%player:name% has made the advancement [%d4f:title%]";
    public String advancementTaskDescription = "%d4f:description%";
    public String advancementChallengeTitle = "%player:name% has completed the challenge [%d4f:title%]";
    public String advancementChallengeDescription = "%d4f:description%";
    public boolean sendMessagesToDiscord = true;
    public Boolean sendBroadcastedMessagesToDiscord = true;
    public boolean sendMessagesToMinecraft = true;
    public String discordToMinecraftMessage = "[%d4f:nickname% on Discord] %d4f:message%";
    public String discordToMinecraftWithReplyMessage = "[%d4f:nickname% on Discord (replying to %d4f:reply_nickname%)] %d4f:message%";
    public String discordName = "%player:name%";
    public String minecraftToDiscordMessage = "%d4f:message%";
    // only used when webhook is unavailable, will attempt to convert webhook messages to plain messages using this config option
    public String webhookToPlainMessage = "`%d4f:name%` %d4f:message%";
    public String discordPingFormat = "<blue>@%d4f:fullname%</blue>";
    public Integer updateInterval = 100; // 5 seconds
    public String status = "Minecraft";
    public Integer topicUpdateInterval = -1; // disabled by default due to huge rate limit
    public String topic = "Total player: %server:online%/%server:max_players% | Server TPS: %server:tps%";
    public Boolean forceOnlineUuid = false;
    public Boolean requiresLinkedAccount = false;
    public String avatarUrl = "https://mc-heads.net/avatar/%s";
    public String avatarUrlTextureHash = "https://mc-heads.net/avatar/%s"; // for fabrictailor compat
    public Boolean allowBotMessages = false;
    public Integer playerListDisplayAmount = 5;

    public void writeConfig(File file) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("number_of_servers", numServers);
        jsonObject.addProperty("token", token);
        //jsonObject.addProperty("webhook_url", webhookUrl[num]);
        jsonObject.addProperty("use_thread", useThread);
        //jsonObject.addProperty("guild_id", guildId[num]);
        //jsonObject.addProperty("channel_id", channelId[num]);
        jsonObject.addProperty("console_guild_id", consoleGuildId);
        jsonObject.addProperty("console_channel_id", consoleChannelId);
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
        jsonObject.addProperty("death_description", deathDescription);
        jsonObject.addProperty("announce_advancement", announceAdvancement);
        jsonObject.addProperty("advancement_goal", advancementGoalTitle);
        jsonObject.addProperty("advancement_goal_desc", advancementGoalDescription);
        jsonObject.addProperty("advancement_task", advancementTaskTitle);
        jsonObject.addProperty("advancement_task_desc", advancementTaskDescription);
        jsonObject.addProperty("advancement_challenge", advancementChallengeTitle);
        jsonObject.addProperty("advancement_challenge_desc", advancementChallengeDescription);
        jsonObject.addProperty("send_messages_to_discord", sendMessagesToDiscord);
        jsonObject.addProperty("send_broadcasted_messages_to_discord", sendBroadcastedMessagesToDiscord);
        jsonObject.addProperty("send_messages_to_minecraft", sendMessagesToMinecraft);
        jsonObject.addProperty("discord_to_mc", discordToMinecraftMessage);
        jsonObject.addProperty("discord_to_mc_reply", discordToMinecraftWithReplyMessage);
        jsonObject.addProperty("discord_name", discordName);
        jsonObject.addProperty("mc_to_discord", minecraftToDiscordMessage);
        jsonObject.addProperty("webhook_to_plain", webhookToPlainMessage);
        jsonObject.addProperty("discord_ping", discordPingFormat);
        jsonObject.addProperty("update_interval", updateInterval);
        jsonObject.addProperty("status", status);
        jsonObject.addProperty("topic_update_interval", topicUpdateInterval);
        jsonObject.addProperty("topic", topic);
        jsonObject.addProperty("force_online_uuid", forceOnlineUuid);
        jsonObject.addProperty("requires_linked_account", requiresLinkedAccount);
        jsonObject.addProperty("avatar_url", avatarUrl);
        jsonObject.addProperty("avatar_url_texture_hash", avatarUrlTextureHash);
        jsonObject.addProperty("allow_bot_messages", allowBotMessages);
        jsonObject.addProperty("playerlist_display_amount", playerListDisplayAmount);

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
        numServers = getIntOrDefault(obj,"number_of_servers", numServers);
        token = getStringOrDefault(obj, "token", token);
        //webhookUrl[num] = getStringOrDefault(obj, "webhook_url", webhookUrl[num]);
        useThread = getBooleanOrDefault(obj, "use_thread", useThread);
        //guildId[num] = getLongOrDefault(obj, "guild_id", guildId[num]);
        //channelId[num] = getLongOrDefault(obj, "channel_id", channelId[num]);
        consoleGuildId = getLongOrDefault(obj, "console_guild_id", consoleGuildId);
        consoleChannelId = getLongOrDefault(obj, "console_channel_id", consoleChannelId);
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
        deathDescription = getStringOrDefault(obj, "death_description", deathDescription);
        announceAdvancement = getBooleanOrDefault(obj, "announce_advancement", announceAdvancement);
        advancementGoalTitle = getStringOrDefault(obj, "advancement_goal", advancementGoalTitle);
        advancementGoalDescription = getStringOrDefault(obj, "advancement_goal_desc", advancementGoalDescription);
        advancementTaskTitle = getStringOrDefault(obj, "advancement_task", advancementTaskTitle);
        advancementTaskDescription = getStringOrDefault(obj, "advancement_task_desc", advancementTaskDescription);
        advancementChallengeTitle = getStringOrDefault(obj, "advancement_challenge", advancementChallengeTitle);
        advancementChallengeDescription = getStringOrDefault(obj, "advancement_challenge_desc", advancementChallengeDescription);
        sendMessagesToDiscord = getBooleanOrDefault(obj, "send_messages_to_discord", sendMessagesToDiscord);
        sendBroadcastedMessagesToDiscord = getBooleanOrDefault(obj, "send_broadcasted_messages_to_discord", sendBroadcastedMessagesToDiscord);
        sendMessagesToMinecraft = getBooleanOrDefault(obj, "send_messages_to_minecraft", sendMessagesToMinecraft);
        discordToMinecraftMessage = getStringOrDefault(obj, "discord_to_mc", discordToMinecraftMessage);
        discordToMinecraftWithReplyMessage = getStringOrDefault(obj, "discord_to_mc_reply", discordToMinecraftWithReplyMessage);
        discordName = getStringOrDefault(obj, "discord_name", discordName);
        minecraftToDiscordMessage = getStringOrDefault(obj, "mc_to_discord", minecraftToDiscordMessage);
        webhookToPlainMessage = getStringOrDefault(obj, "webhook_to_plain", webhookToPlainMessage);
        discordPingFormat = getStringOrDefault(obj, "discord_ping", discordPingFormat);
        updateInterval = getIntOrDefault(obj, "update_interval", updateInterval);
        status = getStringOrDefault(obj, "status", status);
        topicUpdateInterval = getIntOrDefault(obj, "topic_update_interval", topicUpdateInterval);
        topic = getStringOrDefault(obj, "topic", topic);
        forceOnlineUuid = getBooleanOrDefault(obj, "force_online_uuid", forceOnlineUuid);
        requiresLinkedAccount = getBooleanOrDefault(obj, "requires_linked_account", requiresLinkedAccount);
        avatarUrl = getStringOrDefault(obj, "avatar_url", avatarUrl);
        avatarUrlTextureHash = getStringOrDefault(obj, "avatar_url_texture_hash", avatarUrlTextureHash);
        allowBotMessages = getBooleanOrDefault(obj, "allow_bot_messages", allowBotMessages);
        playerListDisplayAmount = getIntOrDefault(obj, "playerlist_display_amount", playerListDisplayAmount);

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
