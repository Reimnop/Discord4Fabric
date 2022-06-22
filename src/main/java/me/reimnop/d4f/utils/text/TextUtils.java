package me.reimnop.d4f.utils.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {
    // TODO: Implement spoiler
    // private static final Pattern SPOILER_PATTERN = Pattern.compile("\\|\\|(?<text>.+?)\\|\\|");
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(?<text>.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(?<text>.+?)\\*");
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("~~(?<text>.+?)~~");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("__(?<text>.+?)__");

    private TextUtils() {}

    public static Text regexDynamicReplaceText(String value, Pattern pattern, TextRegexReplacer replacer) {
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(value);
        MutableText text = Text.empty();
        while (matcher.find()) {
            text
                    .append(value.substring(lastIndex, matcher.start()))
                    .append(replacer.replace(matcher));
            lastIndex = matcher.end();
        }
        if (lastIndex < value.length()) {
            text.append(value.substring(lastIndex));
        }
        return text;
    }

    public static String regexDynamicReplaceString(String value, Pattern pattern, StringRegexReplacer replacer) {
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(value);
        StringBuilder output = new StringBuilder();
        while (matcher.find()) {
            output
                    .append(value, lastIndex, matcher.start())
                    .append(replacer.replace(matcher));
            lastIndex = matcher.end();
        }
        if (lastIndex < value.length()) {
            output.append(value.substring(lastIndex));
        }
        return output.toString();
    }

    public static String parseMarkdownToPAPI(String value) {
        // This is regex hell
        // There is no way this isn't going to cause performance issues later on
        // Too bad!
        String replaced =
                regexDynamicReplaceString(
                        regexDynamicReplaceString(
                                regexDynamicReplaceString(
                                        regexDynamicReplaceString(
                                                // regexDynamicReplaceString(
                                                //         value,
                                                //         SPOILER_PATTERN,
                                                //         match -> "<spoiler>" + match.group("text") + "</spoiler>"
                                                // ),
                                                value,
                                                BOLD_PATTERN,
                                                match -> "<bold>" + match.group("text") + "</bold>"
                                        ),
                                        ITALIC_PATTERN,
                                        match -> "<italic>" + match.group("text") + "</italic>"
                                ),
                                STRIKETHROUGH_PATTERN,
                                match -> "<strikethrough>" + match.group("text") + "</strikethrough>"
                        ),
                        UNDERLINE_PATTERN,
                        match -> "<underline>" + match.group("text") + "</underline>"
                );
        return replaced;
    }
}
