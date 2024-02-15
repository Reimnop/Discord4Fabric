package me.reimnop.d4f.utils;

import eu.vanish.Vanish;
import me.drex.vanish.api.VanishAPI;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Compatibility {
    private Compatibility() {}

    public static final String VANISH_ID = "vanish";
    public static final String DREXHD_VANISH_ID = "melius-vanish";

    public static boolean isPlayerVanished(ServerPlayerEntity player) {
        return (Utils.isModLoaded(VANISH_ID) && Vanish.INSTANCE.vanishedPlayers.isVanished(player)) ||
                (Utils.isModLoaded(DREXHD_VANISH_ID) && VanishAPI.isVanished(player));
    }
    public static boolean isVanishModLoaded() {
        return Utils.isModLoaded(VANISH_ID) || Utils.isModLoaded(DREXHD_VANISH_ID);
    }
}
