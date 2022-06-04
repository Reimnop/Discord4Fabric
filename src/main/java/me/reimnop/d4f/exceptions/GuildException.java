package me.reimnop.d4f.exceptions;

public class GuildException extends Exception {
    private final String message;

    public GuildException(Long id) {
        message = "Couldn't find guild with id '" + id + "'!";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
