package me.reimnop.d4f.listeners;

import me.reimnop.d4f.events.DiscordMessageReceivedCallback;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        DiscordMessageReceivedCallback.EVENT.invoker().onMessageReceived(event.getAuthor(), event.getMessage());
    }
}
