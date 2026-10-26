package com.naizo.elementals.client;

/** A minimal single-line text input drawn with a Canvas. */
public final class TextField {
    public String text = "";
    public boolean focused;
    public final int maxLength;
    public final String placeholder;

    public TextField(int maxLength, String placeholder) {
        this.maxLength = maxLength;
        this.placeholder = placeholder;
    }

    public boolean type(char c) {
        if (!focused || c < 32 || c == 127 || text.length() >= maxLength) {
            return false;
        }
        text += c;
        return true;
    }

    public boolean backspace(boolean word) {
        if (!focused || text.isEmpty()) {
            return false;
        }
        if (word) {
            String trimmed = text.stripTrailing();
            int cut = trimmed.lastIndexOf(' ');
            text = cut < 0 ? "" : trimmed.substring(0, cut + 1);
        } else {
            text = text.substring(0, text.length() - 1);
        }
        return true;
    }

    public void paste(String value) {
        if (focused && value != null) {
            text = (text + value.replace('\n', ' ').replace('\r', ' ')).trim();
            if (text.length() > maxLength) {
                text = text.substring(0, maxLength);
            }
        }
    }

    public void draw(Canvas c, float x0, float y0, float x1, float y1, long millis, int accent) {
        c.fill(x0, y0, x1, y1, focused ? 0xF0101420 : 0xC0101420);
        c.rect(x0, y0, x1, y1, focused ? Canvas.alpha(accent, 1) : 0xFF3A3F55);
        c.scissor((int) x0 + 2, (int) y0, (int) x1 - 2, (int) y1);
        float ty = y0 + (y1 - y0 - 8) / 2f;
        if (text.isEmpty() && !focused) {
            c.text(placeholder, x0 + 4, ty, 1, 0xFF6E7590, false);
        } else {
            int width = c.textWidth(text);
            float available = x1 - x0 - 10;
            float tx = x0 + 4 - Math.max(0, width - available);
            c.text(text, tx, ty, 1, 0xFFE8E6F0, false);
            if (focused && (millis / 500) % 2 == 0) {
                c.fill(tx + width + 1, ty - 1, tx + width + 2, ty + 9, 0xFFE8E6F0);
            }
        }
        c.endScissor();
    }
}
