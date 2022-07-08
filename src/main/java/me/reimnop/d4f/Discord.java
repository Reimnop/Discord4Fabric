package me.reimnop.d4f;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.reimnop.d4f.exceptions.ChannelException;
import me.reimnop.d4f.exceptions.GuildException;
import me.reimnop.d4f.listeners.DiscordMessageListener;
import me.reimnop.d4f.utils.Utils;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Discord {
    private final JDA jda;
    private final WebhookClient webhookClient;
    private final Config config;
    private final Map<String, Emote> emotes = new HashMap<>();

    public Discord(Config config) throws LoginException, InterruptedException {
        this.config = config;

        // init jda
        JDABuilder builder = JDABuilder
                .createDefault(config.token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL);
        jda = builder.build();
        jda.addEventListener(new DiscordMessageListener());
        jda.awaitReady();

        // init webhook
        webhookClient = WebhookClient.withUrl(config.webhookUrl);
    }

    public void initCache() throws GuildException {
        getGuild().loadMembers();

        emotes.clear();
        for (Emote emote : getGuild().getEmotes()) {
            emotes.put(emote.getName(), emote);
        }
    }

    public void close() {
        jda.shutdown();
        webhookClient.close();
    }

    public Guild getGuild() throws GuildException {
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

    @Nullable
    public User getUser(Long id) {
        return jda.retrieveUserById(id).complete();
    }

    @Nullable
    public Member getMember(User user) {
        try {
            return getGuild().getMember(user);
        } catch (GuildException e) {
            Utils.logException(e);
        }
        return null;
    }

    @Nullable
    public User findUser(String tag) throws GuildException {
        Member member = getGuild().getMemberByTag(tag);
        return member != null ? member.getUser() : null;
    }

    @Nullable
    public Emote findEmote(String name) {
        return emotes.getOrDefault(name, null);
    }

    public void sendPlayerMessage(PlayerEntity sender, Text name, Text message) {
        WebhookMessageBuilder wmb = new WebhookMessageBuilder()
                .setAvatarUrl(Utils.getAvatarUrl(sender.getUuid()))
                .setUsername(name.getString())
                .setContent(message.getString())
                .setAllowedMentions(new AllowedMentions()
                        .withParseEveryone(false)
                        .withParseRoles(false)
                        .withParseUsers(true));

        webhookClient.send(wmb.build());
    }

    public void sendEmbedMessageUsingPlayerAvatar(PlayerEntity sender, Color color, String message, String description) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(message, null, Utils.getAvatarUrl(sender.getUuid()))
                .setDescription(description)
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

    public void sendPlainMessage(String message) {
        try {
            getTextChannel()
                    .sendMessage(message)
                    .queue();
        } catch (Exception e) {
            Utils.logException(e);
        }
    }

    public void sendPlainMessage(Text message) {
        sendPlainMessage(message.getString());
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
