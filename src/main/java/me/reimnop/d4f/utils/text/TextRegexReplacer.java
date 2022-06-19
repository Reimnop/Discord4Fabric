package me.reimnop.d4f.utils.text;

import net.minecraft.text.Text;

import java.util.regex.Matcher;

public interface TextRegexReplacer {
    Text replace(Matcher match);
}
