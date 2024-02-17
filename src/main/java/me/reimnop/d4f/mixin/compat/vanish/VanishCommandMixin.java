package me.reimnop.d4f.mixin.compat.vanish;

import eu.vanish.commands.VanishCommand;
import eu.vanish.data.Settings;
import me.reimnop.d4f.events.OnPlayerUnvanishCallback;
import me.reimnop.d4f.events.OnPlayerVanishCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VanishCommand.class, remap = false)
public class VanishCommandMixin {

    @Shadow @Final private static Settings settings;

    @Inject(
            method = "vanish",
            remap = true,
            at = @At(
                    target = "Leu/vanish/commands/VanishCommand;logVanish(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    value = "INVOKE"
            )
    )
    private static void Discord4Fabric$vanish(ServerPlayerEntity vanishingPlayer, CallbackInfoReturnable<Integer> ci) {
        if (settings.showFakeLeaveMessage()) {
            OnPlayerVanishCallback.EVENT.invoker().onVanish(vanishingPlayer);
        }
    }

    @Inject(
            method = "unvanish",
            remap = true,
            at = @At(
                    target = "Leu/vanish/commands/VanishCommand;logUnvanish(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    value = "INVOKE"
            )
    )
    private static void Discord4Fabric$unvanish(ServerPlayerEntity unvanishingPlayer, CallbackInfoReturnable<Integer> ci) {
        if (settings.showFakeJoinMessage()) {
            OnPlayerUnvanishCallback.EVENT.invoker().onUnvanish(unvanishingPlayer);
        }
    }
}
