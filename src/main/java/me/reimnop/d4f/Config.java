package me.reimnop.d4f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private String token = "";
    private String webhookUrl = "";
    private Long guildId = 0L;
    private Long channelId = 0L;
    private String serverStartMessage = "✅ **Server started!**";
    private String serverStopMessage = "❎ **Server stopped!**";
    private String playerJoinMessage = "%player:name% joined the game";
    private String playerLeftMessage = "%player:name% left the game";
    private String discordToMinecraftMessage = "[%d4f:nickname% on Discord] %d4f:message%";
    private String minecraftToDiscordMessage = "%d4f:message%";
    private String deathMessage = "%d4f:message%";
    private Integer updateInterval = 40;
    private String channelDescOnline = "Total player: %server:online%/%server:max_players% | Server TPS: %server:tps%";
    private String channelDescOffline = "Server offline";

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
        jsonObject.addProperty("discord_to_mc", discordToMinecraftMessage);
        jsonObject.addProperty("mc_to_discord", minecraftToDiscordMessage);
        jsonObject.addProperty("death", deathMessage);
        jsonObject.addProperty("update_interval", updateInterval);
        jsonObject.addProperty("channel_desc_online", channelDescOnline);
        jsonObject.addProperty("channel_desc_offline", channelDescOffline);

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
        discordToMinecraftMessage = obj.get("discord_to_mc").getAsString();
        minecraftToDiscordMessage = obj.get("mc_to_discord").getAsString();
        deathMessage = obj.get("death").getAsString();
        updateInterval = obj.get("update_interval").getAsInt();
        channelDescOnline = obj.get("channel_desc_online").getAsString();
        channelDescOffline = obj.get("channel_desc_offline").getAsString();

        reader.close();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getServerStartMessage() {
        return serverStartMessage;
    }

    public void setServerStartMessage(String serverStartMessage) {
        this.serverStartMessage = serverStartMessage;
    }

    public String getServerStopMessage() {
        return serverStopMessage;
    }

    public void setServerStopMessage(String serverStopMessage) {
        this.serverStopMessage = serverStopMessage;
    }

    public String getPlayerJoinMessage() {
        return playerJoinMessage;
    }

    public void setPlayerJoinMessage(String playerJoinMessage) {
        this.playerJoinMessage = playerJoinMessage;
    }

    public String getPlayerLeftMessage() {
        return playerLeftMessage;
    }

    public void setPlayerLeftMessage(String playerLeftMessage) {
        this.playerLeftMessage = playerLeftMessage;
    }

    public String getDiscordToMinecraftMessage() {
        return discordToMinecraftMessage;
    }

    public void setDiscordToMinecraftMessage(String discordToMinecraftMessage) {
        this.discordToMinecraftMessage = discordToMinecraftMessage;
    }

    public String getMinecraftToDiscordMessage() {
        return minecraftToDiscordMessage;
    }

    public void setMinecraftToDiscordMessage(String minecraftToDiscordMessage) {
        this.minecraftToDiscordMessage = minecraftToDiscordMessage;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public String getChannelDescOnline() {
        return channelDescOnline;
    }

    public void setChannelDescOnline(String channelDescOnline) {
        this.channelDescOnline = channelDescOnline;
    }

    public String getChannelDescOffline() {
        return channelDescOffline;
    }

    public void setChannelDescOffline(String channelDescOffline) {
        this.channelDescOffline = channelDescOffline;
    }

    public Integer getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(Integer updateInterval) {
        this.updateInterval = updateInterval;
    }
}
