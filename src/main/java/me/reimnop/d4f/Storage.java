package me.reimnop.d4f;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
public class Storage {
    public String[] webhookUrl = new String[2];
    public Long[] guildId  = new Long[2];
    public Long[] channelId = new Long[2];

    public void initLength(int num){
        for(int i = 0; i < num; i++){
            webhookUrl[i] = " ";
            guildId[i] = 0L;
            channelId[i] = 0L;
        }
    }

    public void writeStorage(File file, int num) throws  IOException {
        JsonObject jsonObject = new JsonObject();

        for(int i = 0; i < num; i++){
            jsonObject.addProperty("webhook_url_" + i, webhookUrl[i]);
            jsonObject.addProperty("guild_id_" + i, guildId[i]);
            jsonObject.addProperty("channel_id_" + i, channelId[i]);
        }
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        FileWriter writer = new FileWriter(file);
        gson.toJson(jsonObject, writer);
        writer.close();
    }

    public void readStorage(File file, int num) throws  IOException {
        FileReader reader = new FileReader(file);
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(reader, JsonObject.class);
        for(int i = 0; i < num; i++){
            webhookUrl[i] = obj.get("webhook_url_" + i).getAsString();
            guildId[i] = obj.get("guild_id_" + i).getAsLong();
            channelId[i] = obj.get("channel_id_" + i).getAsLong();
        }
        reader.close();
    }


}
