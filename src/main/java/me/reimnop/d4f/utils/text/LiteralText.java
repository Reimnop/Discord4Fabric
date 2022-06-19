package me.reimnop.d4f.utils.text;

import net.minecraft.text.Style;

public class LiteralText {
    public String text;
    public Style style;

    public LiteralText(String text, Style style) {
        this.text = text;
        this.style = style;
    }

    public LiteralText() {
        this("", Style.EMPTY);
    }
}
