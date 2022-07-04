package me.reimnop.d4f.customevents.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.ActionContext;
import me.reimnop.d4f.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class GrantRoleAction implements Action {
    @Override
    public void runAction(JsonElement value, ActionContext context) {
        if (!(value instanceof JsonObject jsonObject)) {
            Discord4Fabric.LOGGER.error("Invalid grant_role value format (should be object)");
            return;
        }

        // How to cause performance problems
        String userIdStr = context.parsePlaceholder(jsonObject.get("user").getAsString()).getString();
        String roleIdStr = context.parsePlaceholder(jsonObject.get("role").getAsString()).getString();

        User user = Discord4Fabric.DISCORD.getUser(Long.parseLong(userIdStr));
        if (user == null) {
            Discord4Fabric.LOGGER.error("The grant_role action could not find the user specified");
            return;
        }

        try {
            Guild guild = Discord4Fabric.DISCORD.getGuild();
            Role role = guild.getRoleById(Long.parseLong(roleIdStr));
            if (role == null) {
                Discord4Fabric.LOGGER.error("The grant_role action could not find the role specified");
                return;
            }
            guild.addRoleToMember(user, role).queue();
        } catch (Exception e) {
            Utils.logException(e);
        }
    }
}
