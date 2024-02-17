package me.reimnop.d4f.mixin;

import me.reimnop.d4f.duck.IStyleAccess;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Style.class)
public class StyleMixin implements IStyleAccess {

    @Mutable @Shadow @Final @Nullable ClickEvent clickEvent;

    @Override
    public void Discord4Fabric$setClickEvent(@Nullable ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }
}
