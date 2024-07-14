package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import eu.pb4.placeholders.api.PlaceholderResult;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.utils.Utils;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LinkedAccountConstraintProcessor implements ConstraintProcessor {
    public static class DiscordProfile {
        public final Long id;
        public final String fullname;
        public final String nickname;
        public final String discriminator;

        public DiscordProfile(Long id) {
            this.id = id;

            User user = Discord4Fabric.DISCORD.getUser(id);
            if (user != null) {
                fullname = user.getAsTag();
                nickname = Utils.getNicknameFromUser(user);
                discriminator = user.getDiscriminator();
            } else {
                fullname = "null";
                nickname = "null";
                discriminator = "null";
            }
        }
    }

    private final Optional<DiscordProfile> discordProfile;

    public LinkedAccountConstraintProcessor(UUID uuid) {
        Optional<Long> discordId = Discord4Fabric.ACCOUNT_LINKING.getLinkedAccount(uuid);
        discordProfile = discordId.map(DiscordProfile::new);
    }

    public Optional<DiscordProfile> getDiscordProfile() {
        return discordProfile;
    }

    @Override
    public void loadArguments(List<String> arguments) {
    }

    @Override
    public boolean satisfied() {
        return discordProfile.isPresent();
    }

    @Override
    @Nullable
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
        return Identifier.of(ConstraintTypes.LINKED_ACCOUNT, path);
    }
}
