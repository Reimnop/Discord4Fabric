package me.reimnop.d4f.mixin;

import me.reimnop.d4f.events.PlayerAdvancementCallback;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    private void grantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (advancement.getDisplay() == null) {
            return;
        }

        if (!advancement.getDisplay().shouldAnnounceToChat()) {
            return;
        }

        if (!owner.getAdvancementTracker().getProgress(advancement).isDone()) {
            return;
        }

        PlayerAdvancementCallback.EVENT.invoker().onAdvancementGranted(owner, advancement);
    }
}
