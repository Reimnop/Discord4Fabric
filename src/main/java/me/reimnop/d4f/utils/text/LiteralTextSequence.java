package me.reimnop.d4f.utils.text;

import me.reimnop.d4f.utils.Utils;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiteralTextSequence {
    private final List<LiteralText> literalTextList;

    public LiteralTextSequence(Text text) {
        this.literalTextList = new ArrayList<>();
        recursivelyAppendList(literalTextList, text);
    }

    public Text regex(Pattern pattern, TextRegexReplacer replacer) {
        MutableText text = Text.empty();
        for (LiteralText literalText : literalTextList) {
            text.append(regexDynamicReplaceText(literalText, pattern, replacer));
        }
        return text;
    }

    private Text regexDynamicReplaceText(LiteralText literalText, Pattern pattern, TextRegexReplacer replacer) {
        int lastIndex = 0;
        Matcher matcher = pattern.matcher(literalText.text);
        MutableText text = Text
                .empty()
                .setStyle(literalText.style);

        while (matcher.find()) {
            text
                    .append(literalText.text.substring(lastIndex, matcher.start()))
                    .append(replacer.replace(matcher));
            lastIndex = matcher.end();
        }
        if (lastIndex < literalText.text.length()) {
            text.append(literalText.text.substring(lastIndex));
        }
        return text;
    }

    private void recursivelyAppendList(List<LiteralText> list, Text text) {
        String string;
        if (text.getContent() instanceof PlainTextContent content) {
            string = content.string();
        } else if (text.getContent() instanceof TranslatableTextContent content) {
            string = translateText(content.getKey());
        } else {
            // TODO: Properly handle this
            string = "";
        }
        list.add(new LiteralText(string, text.getStyle()));

        for (Text children : text.getSiblings()) {
            recursivelyAppendList(list, children);
        }
    }

    private String translateText(String key) {
        Language language = Language.getInstance();
        return language.hasTranslation(key) ? language.get(key) : key;
    }
}
