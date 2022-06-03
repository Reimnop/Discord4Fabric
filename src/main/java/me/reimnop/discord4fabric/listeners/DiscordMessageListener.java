package me.reimnop.discord4fabric.listeners;

import me.reimnop.discord4fabric.events.DiscordMessageReceivedCallback;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class DiscordMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        DiscordMessageReceivedCallback.EVENT.invoker().onMessageReceived(event.getAuthor(), event.getMessage());
    }
}
