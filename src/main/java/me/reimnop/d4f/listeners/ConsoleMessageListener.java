package me.reimnop.d4f.listeners;

import me.reimnop.d4f.events.OnConsoleMessageReceivedCallback;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

public class ConsoleMessageListener extends AbstractAppender {
    public ConsoleMessageListener() {
        super("D4fConsoleMessageListener", null, null, false, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
        OnConsoleMessageReceivedCallback.EVENT.invoker().onMessage(event);
    }
}
