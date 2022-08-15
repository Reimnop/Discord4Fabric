package me.reimnop.d4f.customevents.constraints;

import eu.pb4.placeholders.api.PlaceholderHandler;
import me.reimnop.d4f.Discord4Fabric;
import net.dv8tion.jda.api.entities.Member;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LinkedAccountNickConstraintProcessor implements ConstraintProcessor {
    // Only here to provide account
    private final LinkedAccountConstraintProcessor linkedAccountConstraintProcessor;

    private String value;

    public LinkedAccountNickConstraintProcessor(UUID uuid) {
        linkedAccountConstraintProcessor = new LinkedAccountConstraintProcessor(uuid);
    }

    @Override
    public void loadArguments(List<String> arguments) {
        if (arguments.size() == 0) {
            Discord4Fabric.LOGGER.warn("Too few arguments for linked account nick constraint!");
            return;
        }
        if (arguments.size() > 1) {
            Discord4Fabric.LOGGER.warn("Too many arguments for linked account nick constraint!");
            return;
        }
        value = arguments.get(0);
    }

    @Override
    public boolean satisfied() {
        Optional<LinkedAccountConstraintProcessor.DiscordProfile> discordProfile = linkedAccountConstraintProcessor.getDiscordProfile();
        if (discordProfile.isEmpty()) {
            return false;
        }

        Member member = Discord4Fabric.DISCORD.getMember(discordProfile.get().id);
        if (member == null) {
            return false;
        }

        return member.getEffectiveName().equalsIgnoreCase(value);
    }

    @Nullable
    @Override
    public Map<Identifier, PlaceholderHandler> getHandlers() {
        return null;
    }
}
