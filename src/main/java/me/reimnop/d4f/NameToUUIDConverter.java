package me.reimnop.d4f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.reimnop.d4f.utils.Utils;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class NameToUUIDConverter {
    private final HashMap<String, UUID> cache = new HashMap<>();

    private static UUID toUuid(String string) {

        if (string == null || string.length() != 32) {
            throw new IllegalArgumentException("invalid input string!");
        }

        char[] input = string.toCharArray();
        char[] output = new char[36];

        System.arraycopy(input, 0, output, 0, 8);
        System.arraycopy(input, 8, output, 9, 4);
        System.arraycopy(input, 12, output, 14, 4);
        System.arraycopy(input, 16, output, 19, 4);
        System.arraycopy(input, 20, output, 24, 12);

        output[8] = '-';
        output[13] = '-';
        output[18] = '-';
        output[23] = '-';

        return UUID.fromString(String.valueOf(output));
    }

    public UUID getUuid(ServerPlayerEntity player) {
        String name = player.getName().getString();

        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 204) {
                addToCache(name, player.getUuid());
                return player.getUuid();
            }

            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(isr, JsonObject.class);

            String uuidStr = jsonObject.get("id").getAsString();
            UUID uuid = toUuid(uuidStr);

            Discord4Fabric.LOGGER.info(uuid.toString());

            addToCache(name, uuid);

            return uuid;
        } catch (IOException e) {
            Utils.logException(e);
            return player.getUuid();
        }
    }

    private void addToCache(String name, UUID uuid) {
        cache.put(name, uuid);

        File file = new File(Utils.getNameCachePath());

        try {
            writeCache(file);
        } catch (IOException e) {
            Utils.logException(e);
        }
    }

    public void loadCache(File file) throws IOException {
        FileReader reader = new FileReader(file);

        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(reader, JsonObject.class);

        cache.clear();
        for (String name : obj.keySet()) {
            UUID uuid = UUID.fromString(obj.get(name).getAsString());
            cache.put(name, uuid);
        }

        reader.close();
    }

    public void writeCache(File file) throws IOException {
        JsonObject jsonObject = new JsonObject();

        for (String name : cache.keySet()) {
            jsonObject.addProperty(name, cache.get(name).toString());
        }

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        FileWriter writer = new FileWriter(file);
        gson.toJson(jsonObject, writer);
        writer.close();
    }
}
