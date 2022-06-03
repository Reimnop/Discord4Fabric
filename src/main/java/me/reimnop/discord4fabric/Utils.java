package me.reimnop.discord4fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.util.UUID;

public final class Utils {
    private Utils() {}

    public static String getAvatarUrl(UUID uuid) {
        return "https://crafatar.com/avatars/" + uuid.toString() + "?overlay";
    }

    public static String getConfigPath() {
        return FabricLoader.getInstance().getConfigDir() + "/discord4fabric.json";
    }
}
