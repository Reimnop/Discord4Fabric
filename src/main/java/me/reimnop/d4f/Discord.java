package me.reimnop.d4f;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.reimnop.d4f.exceptions.ChannelException;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.listeners.DiscordMessageListener;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;

import javax.security.auth.login.LoginException;
import java.awt.*;

public class Discord {
    private final JDA jda;
    private final WebhookClient webhookClient;
    private final Config config;

    public Discord(Config config) throws LoginException {
        this.config = config;

        // init jda
        JDABuilder builder = JDABuilder.createDefault(config.token);
        jda = builder.build();
        jda.addEventListener(new DiscordMessageListener());

        // init webhook
        webhookClient = WebhookClient.withUrl(config.webhookUrl);
    }

    public void close() {
        jda.shutdown();
        webhookClient.close();
    }

    private Guild getGuild() throws GuildException {
        Guild guild = jda.getGuildById(config.guildId);
        if (guild == null) {
            throw new GuildException(config.guildId);
        }
        return guild;
    }

    private TextChannel getTextChannel() throws GuildException, ChannelException {
        TextChannel channel = getGuild().getChannelById(TextChannel.class, config.channelId);
        if (channel == null) {
            throw new ChannelException(config.channelId);
        }
        return channel;
    }

    public void sendPlayerMessage(PlayerEntity sender, Text name, Text message) {
        WebhookMessageBuilder wmb = new WebhookMessageBuilder()
                .setAvatarUrl(Utils.getAvatarUrl(sender.getUuid()))
                .setUsername(name.getString())
                .setContent(message.getString());

        webhookClient.send(wmb.build());
    }

    public void sendEmbedMessageUsingPlayerAvatar(PlayerEntity sender, Color color, Text message) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(message.getString(), null, Utils.getAvatarUrl(sender.getUuid()))
                .setColor(color);

        sendEmbedMessage(embedBuilder);
    }

    public void sendEmbedMessage(EmbedBuilder embedBuilder) {
        try {
            getTextChannel()
                    .sendMessage(new MessageBuilder()
                            .setEmbeds(embedBuilder.build())
                            .build())
                    .queue();
        } catch (Exception e) {
            Utils.logException(e);
        }
    }

    public void sendPlainMessage(Text message) {
        try {
            getTextChannel()
                    .sendMessage(message.getString())
                    .queue();
        } catch (Exception e) {
            Utils.logException(e);
        }
    }

    public void setChannelTopic(Text topic) {
        try {
            getTextChannel()
                    .getManager()
                    .setTopic(topic.getString())
                    .queue();
        } catch (Exception e) {
            Utils.logException(e);
        }
    }

    public void setStatus(Text status) {
        jda.getPresence().setActivity(
                Activity.playing(status.getString())
        );
    }
}
