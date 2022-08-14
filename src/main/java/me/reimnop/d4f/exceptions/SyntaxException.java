package me.reimnop.d4f.exceptions;

public class SyntaxException extends Exception {
    private final String message;

    public SyntaxException(int index) {
        message = "Invalid constraint syntax at " + index + "!";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
