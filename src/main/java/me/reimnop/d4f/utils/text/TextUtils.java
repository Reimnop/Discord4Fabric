package me.reimnop.d4f.utils.text;

import me.reimnop.d4f.utils.Utils;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {
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
}
