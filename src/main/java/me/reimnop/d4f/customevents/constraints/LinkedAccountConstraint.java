package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import me.reimnop.d4f.Discord4Fabric;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LinkedAccountConstraint implements Constraint {
    private static class DiscordProfile {
        public final Long id;
        public final String fullname;
        public final String nickname;
        public final String discriminator;

        public DiscordProfile(Long id) {
            this.id = id;

            User user = Discord4Fabric.DISCORD.getUser(id);
            if (user != null) {
                fullname = user.getAsTag();
                nickname = user.getName();
                discriminator = user.getDiscriminator();
            } else {
                fullname = "null";
                nickname = "null";
                discriminator = "null";
            }
        }
    }

    private final Optional<DiscordProfile> discordProfile;

    public LinkedAccountConstraint(UUID uuid) {
        Optional<Long> discordId = Discord4Fabric.ACCOUNT_LINKING.getLinkedAccount(uuid);
        discordProfile = discordId.map(DiscordProfile::new);
    }

    @Override
    public boolean satisfied() {
        return discordProfile.isPresent();
    }

    @Override
    public Map<Identifier, PlaceholderHandler> getHandlers() {
        assert discordProfile.isPresent();
        DiscordProfile profile = discordProfile.get();
        return Map.of(
                id("id"), (ctx, arg) -> PlaceholderResult.value(profile.id.toString()),
                id("fullname"), (ctx, arg) -> PlaceholderResult.value(profile.fullname),
                id("nickname"), (ctx, arg) -> PlaceholderResult.value(profile.nickname),
                id("discriminator"), (ctx, arg) -> PlaceholderResult.value(profile.nickname)
        );
    }

    private static Identifier id(String path) {
        return new Identifier(Constraints.LINKED_ACCOUNT, path);
    }
}
