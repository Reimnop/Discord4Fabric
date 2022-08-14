package me.reimnop.d4f.customevents;

import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.exceptions.SyntaxException;

import java.util.ArrayList;
import java.util.List;

public class ConstraintParser {
    private static class Scanner {
        public static class Token {
            public final TokenType type;
            public final String value;

            public Token(TokenType type, String value) {
                this.type = type;
                this.value = value;
            }
        }

        public enum TokenType {
            EXCLAMATION_MARK,
            OPEN_PAREN,
            CLOSE_PAREN,
            COMMA,
            NAME,
            VALUE
        }

        public Token currentToken = null;

        private final String value;
        private int index = 0;

        public Scanner(String value) {
            this.value = value;
        }

        public boolean next() {
            if (index < value.length()) {
                // skip whitespace
                while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
                    index++;
                }

                char currentChar = value.charAt(index);
                if (currentChar == '!') {
                    currentToken = new Token(TokenType.EXCLAMATION_MARK, "!");
                    index++;
                    return true;
                }

                if (currentChar == '(') {
                    currentToken = new Token(TokenType.OPEN_PAREN, "(");
                    index++;
                    return true;
                }

                if (currentChar == ')') {
                    currentToken = new Token(TokenType.CLOSE_PAREN, ")");
                    index++;
                    return true;
                }

                if (currentChar == ',') {
                    currentToken = new Token(TokenType.COMMA, ",");
                    index++;
                    return true;
                }

                if (currentChar == '\'') {
                    currentToken = getValue();
                    return true;
                }

                currentToken = getName();
                return true;
            }

            return false;
        }

        private Token getName() {
            StringBuilder stringBuilder = new StringBuilder();
            while (index < value.length() && (Character.isLetterOrDigit(value.charAt(index)) || value.charAt(index) == '_' || value.charAt(index) == '-')) {
                stringBuilder.append(value.charAt(index));
                index++;
            }
            return new Token(TokenType.NAME, stringBuilder.toString());
        }

        private Token getValue() {
            StringBuilder stringBuilder = new StringBuilder();

            if (value.charAt(index) != '\'') {
                return null; // This should never happen
            }
            index++;

            char lastChar = 0;
            while (!isValueEnding(value.charAt(index), lastChar)) {
                lastChar = value.charAt(index);
                stringBuilder.append(value.charAt(index));
                index++;
            }
            index++;

            return new Token(TokenType.VALUE, stringBuilder.toString());
        }

        private boolean isValueEnding(char currentChar, char lastChar) {
            if (lastChar == '\\') {
                return false;
            }

            return currentChar == '\'';
        }

        public void consume(TokenType tokenType) throws SyntaxException {
            // Discord4Fabric.LOGGER.info("Expected " + tokenType + ", got " + currentToken.type + ", value = " + currentToken.value);

            if (currentToken.type != tokenType) {
                throw new SyntaxException(index);
            }

            next();
        }
    }

    public static class ParseResult {
        public final boolean negated;
        public final String name;
        public final List<String> arguments;

        public ParseResult(boolean negated, String name, List<String> arguments) {
            this.negated = negated;
            this.name = name;
            this.arguments = arguments;
        }
    }

    private final Scanner scanner;

    private boolean negated = false;
    private String name = "";
    private List<String> arguments = null;

    public ConstraintParser(String value) {
        scanner = new Scanner(value);
    }

    public ParseResult parse() throws SyntaxException {
        scanner.next(); // prime it

        if (scanner.currentToken.type == Scanner.TokenType.EXCLAMATION_MARK) {
            negated = true;
            scanner.consume(Scanner.TokenType.EXCLAMATION_MARK);
        }
        parseConstraint();

        return new ParseResult(negated, name, arguments);
    }

    private void parseConstraint() throws SyntaxException {
        Scanner.Token nameToken = scanner.currentToken;
        scanner.consume(Scanner.TokenType.NAME);

        name = nameToken.value;

        if (scanner.currentToken.type == Scanner.TokenType.OPEN_PAREN) {
            scanner.consume(Scanner.TokenType.OPEN_PAREN);

            arguments = new ArrayList<>();

            Scanner.Token firstValueToken = scanner.currentToken;
            scanner.consume(Scanner.TokenType.VALUE);

            arguments.add(firstValueToken.value);

            while (scanner.currentToken.type == Scanner.TokenType.COMMA) {
                scanner.consume(Scanner.TokenType.COMMA);

                Scanner.Token valueToken = scanner.currentToken;
                scanner.consume(Scanner.TokenType.VALUE);

                arguments.add(valueToken.value);
            }

            scanner.consume(Scanner.TokenType.CLOSE_PAREN);
        }
    }
}
