package me.reimnop.discord4fabric;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.reimnop.discord4fabric.listeners.DiscordMessageListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;

import javax.security.auth.login.LoginException;
import java.awt.*;

public class Discord {
    private final JDA jda;

    private final WebhookClient webhookClient;

    private final long guildId;
    private final long channelId;
    private final int updateInterval;

    private int tick = 0;

    public Discord() throws LoginException, InterruptedException {
        guildId = Discord4Fabric.CONFIG.getGuildId();
        channelId = Discord4Fabric.CONFIG.getChannelId();
        updateInterval = Discord4Fabric.CONFIG.getUpdateInterval();

        // init jda
        JDABuilder builder = JDABuilder.createDefault(Discord4Fabric.CONFIG.getToken());
        jda = builder.build();
        jda.addEventListener(new DiscordMessageListener());
        jda.awaitReady();

        // init webhook
        webhookClient = WebhookClient.withUrl(Discord4Fabric.CONFIG.getWebhookUrl());

        // init server events
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (tick < updateInterval) {
                tick++;
                return;
            }
            tick = 0;

            jda
                    .getGuildById(guildId)
                    .getChannelById(TextChannel.class, channelId)
                    .getManager()
                    .setTopic(
                            String.format(
                                    Discord4Fabric.CONFIG.getChannelDescOnline(),
                                    server.getCurrentPlayerCount(),
                                    server.getMaxPlayerCount()
                            )
                    )
                    .queue();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            jda
                    .getGuildById(guildId)
                    .getChannelById(TextChannel.class, channelId)
                    .getManager()
                    .setTopic(Discord4Fabric.CONFIG.getChannelDescOffline())
                    .queue();
        });
    }

    public void sendPlayerMessage(PlayerEntity sender, String message) {
        WebhookMessageBuilder wmb = new WebhookMessageBuilder()
                .setAvatarUrl(Utils.getAvatarUrl(sender.getUuid()))
                .setUsername(sender.getDisplayName().getString())
                .setContent(message);

        webhookClient.send(wmb.build());
    }

    public void sendEmbedMessageUsingPlayerAvatar(PlayerEntity sender, Color color, String message) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(message, null, Utils.getAvatarUrl(sender.getUuid()))
                .setColor(color);

        sendEmbedMessage(embedBuilder);
    }

    public void sendEmbedMessage(EmbedBuilder embedBuilder) {
        jda
                .getGuildById(guildId)
                .getChannelById(TextChannel.class, channelId)
                .sendMessage(new MessageBuilder()
                        .setEmbeds(embedBuilder.build())
                        .build())
                .queue();
    }

    public void sendPlainMessage(String message) {
        jda
                .getGuildById(guildId)
                .getChannelById(TextChannel.class, channelId)
                .sendMessage(message)
                .queue();
    }
}
