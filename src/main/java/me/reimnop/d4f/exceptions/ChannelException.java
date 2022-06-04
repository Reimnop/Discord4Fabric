package me.reimnop.d4f.exceptions;

public class ChannelException extends Exception {
    private final String message;

    public ChannelException(Long id) {
        message = "Couldn't find channel with id '" + id + "'!";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
