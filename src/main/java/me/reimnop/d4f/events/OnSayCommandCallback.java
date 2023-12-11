package me.reimnop.d4f.events;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.message.SignedMessage;

public interface OnSayCommandCallback {
    Event<OnSayCommandCallback> EVENT = EventFactory.createArrayBacked(OnSayCommandCallback.class, (listeners) -> (((commandContext, message) -> {
        for(OnSayCommandCallback listener : listeners){
            listener.onSayCommand(commandContext, message);
        }
    })));

    void onSayCommand(CommandContext commandContext, SignedMessage message);

}
