package me.reimnop.d4f.utils;

import eu.vanish.Vanish;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Compatibility {
    private Compatibility() {}

    public static final String VANISH_ID = "vanish";

    public static boolean isPlayerVanished(ServerPlayerEntity player) {
        return Utils.isModLoaded(VANISH_ID) && Vanish.INSTANCE.vanishedPlayers.isVanished(player);
    }
}
