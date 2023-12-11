package me.reimnop.d4f.mixin;

import com.mojang.brigadier.context.CommandContext;
import me.reimnop.d4f.events.OnSayCommandCallback;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.SayCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SayCommand.class)
public class SayCommandMixin {

    @Inject(
            method = "method_43657(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/network/message/SignedMessage;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/network/message/MessageType$Parameters;)V",
                    shift = At.Shift.AFTER))
    private static void onSayCommand(CommandContext commandContext, SignedMessage message, CallbackInfo ci){
        OnSayCommandCallback.EVENT.invoker().onSayCommand(commandContext, message);
    }

}
