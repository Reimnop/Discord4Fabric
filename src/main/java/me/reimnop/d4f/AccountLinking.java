package me.reimnop.d4f;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.reimnop.d4f.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

public class AccountLinking {
    public enum LinkingResult {
        INVALID_CODE,
        ACCOUNT_LINKED,
        SUCCESS
    }

    public enum UnlinkingResult {
        ACCOUNT_UNLINKED,
        SUCCESS
    }

    public enum QueuingResult {
        ACCOUNT_QUEUED,
        ACCOUNT_LINKED,
        SUCCESS
    }

    private final BiMap<UUID, Long> uuidDiscordIdBiMap = HashBiMap.create();
    private final BiMap<String, UUID> codeUuidBiMap = HashBiMap.create();

    private final SecureRandom random = new SecureRandom();

    public void write(File file) throws IOException {
        JsonObject jsonObject = new JsonObject();
        for (UUID uuid : uuidDiscordIdBiMap.keySet()) {
            jsonObject.addProperty(uuid.toString(), uuidDiscordIdBiMap.get(uuid));
        }

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        FileWriter writer = new FileWriter(file);
        gson.toJson(jsonObject, writer);
        writer.close();
    }

    public void read(File file) throws IOException {
        FileReader reader = new FileReader(file);

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        uuidDiscordIdBiMap.clear();
        for (String uuidString : jsonObject.keySet()) {
            UUID uuid = UUID.fromString(uuidString);
            uuidDiscordIdBiMap.put(uuid, jsonObject.get(uuidString).getAsLong());
        }
    }

    public Optional<Long> getLinkedAccount(UUID uuid) {
        return Optional.ofNullable(uuidDiscordIdBiMap.getOrDefault(uuid, null));
    }

    public Optional<UUID> getLinkedAccount(Long discordId) {
        return Optional.ofNullable(uuidDiscordIdBiMap.inverse().getOrDefault(discordId, null));
    }

    public QueuingResult tryQueueForLinking(UUID uuid) {
        if (uuidDiscordIdBiMap.containsKey(uuid)) {
            return QueuingResult.ACCOUNT_LINKED;
        }

        if (codeUuidBiMap.inverse().containsKey(uuid)) {
            return QueuingResult.ACCOUNT_QUEUED;
        }

        codeUuidBiMap.put(randomId(), uuid);
        return QueuingResult.SUCCESS;
    }

    @Nullable
    public String getCode(UUID uuid) {
        return codeUuidBiMap.inverse().getOrDefault(uuid, null);
    }

    public LinkingResult tryLinkAccount(String code, Long discordId) {
        if (uuidDiscordIdBiMap.inverse().containsKey(discordId)) {
            return LinkingResult.ACCOUNT_LINKED;
        }

        if (!codeUuidBiMap.containsKey(code)) {
            return LinkingResult.INVALID_CODE;
        }

        UUID uuid = codeUuidBiMap.get(code);
        codeUuidBiMap.remove(code);

        if (uuidDiscordIdBiMap.containsKey(uuid)) {
            return LinkingResult.ACCOUNT_LINKED;
        }

        uuidDiscordIdBiMap.put(uuid, discordId);

        try {
            File file = new File(Utils.getUserdataPath());
            write(file);
        } catch (IOException e) {
            Utils.logException(e);
        }

        return LinkingResult.SUCCESS;
    }

    public UnlinkingResult tryUnlinkAccount(UUID uuid) {
        if (!uuidDiscordIdBiMap.containsKey(uuid)) {
            return UnlinkingResult.ACCOUNT_UNLINKED;
        }

        uuidDiscordIdBiMap.remove(uuid);

        try {
            File file = new File(Utils.getUserdataPath());
            write(file);
        } catch (IOException e) {
            Utils.logException(e);
        }

        return UnlinkingResult.SUCCESS;
    }

    private String randomId() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int num = random.nextInt(36);
            char c = (char) (num < 10 ? '0' + num : 'a' + num - 10);
            builder.append(c);
        }

        return builder.toString();
    }
}
