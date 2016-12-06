/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.widgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.skin.UIStyle;
import org.terasology.utilities.Assets;
import org.w3c.dom.css.Rect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * A widget that scrolls through long strings of text
 */
public class UIScrollingText extends CoreWidget {
    private static final String UNDERLINE = "__ ";
    private static final String BOLD = "** ";
    private static final String HEADER1 = "# ";
    private static final String HEADER2 = "## ";
    /**
     * The text to be shown by the widget
     */
    @LayoutConfig
    private Binding<String> text = new DefaultBinding<>("");
    /**
     * Specifies the change in the Y values of the text every frame
     */
    @LayoutConfig
    private int step = 1;
    /**
     * Text offset from the top of the canvas, in pixels
     */
    @LayoutConfig
    private int offsetTop;
    /**
     * Text offset from the bottom of the canvas, in pixels
     */
    @LayoutConfig
    private int offsetBottom;
    /**
     * Spacing between the lines of text, in pixels
     */
    @LayoutConfig
    private int lineSpacing = 3;
    /**
     * Enables fancy text formatting based on the symbols at the start of each line
     * ** for bold
     * # for header 1
     * ## for header 2
     * __ for underline
     */
    @LayoutConfig
    private boolean fancyFormatting;

    /**
     * Maps text to their Y coordinates
     */
    private Map<String, Integer> textY = new HashMap<String, Integer>();
    /**
     * Specifies whether scrolling will restart from the beginning when all text has been scrolled through
     */
    private boolean autoReset;
    private Map<String, Font> fancyFonts = new HashMap<String, Font>();
    private boolean isScrolling = true;

    public UIScrollingText() {
    }

    public UIScrollingText(String text) {
        this.text.set(text);
    }

    public UIScrollingText(Binding<String> text) {
        this.text = text;
    }

    public UIScrollingText(String id, String text) {
        super(id);
        this.text.set(text);
    }

    public UIScrollingText(String id, Binding<String> text) {
        super(id);
        this.text = text;
    }

    public String getText() {
        if (text.get() == null) {
            return "";
        }
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public void bindText(Binding<String> binding) {
        this.text = binding;
    }

    public void startScrolling() {
        isScrolling = true;
    }

    public void stopScrolling() {
        isScrolling = false;
    }

    public void resetScrolling() {
        textY.clear();
    }

    public void setScrollingSpeed(int speed) {
        this.step = speed;
    }

    public void setAutoReset(boolean reset) {
        this.autoReset = reset;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isScrolling) {
            updateYValues(canvas);
        }

        Font font = canvas.getCurrentStyle().getFont();
        int w = canvas.size().x / 2;
        boolean finished = true;
        for (Entry<String, Integer> entry : textY.entrySet()) {
            int y = entry.getValue();
            //ignores offsets
            if (y >= -offsetTop && y <= canvas.size().y - offsetBottom + font.getHeight(entry.getKey())) {
                String line = entry.getKey();
                if (fancyFormatting) {
                    drawFancyText(canvas, entry.getKey(), y);
                } else {
                    Rect2i coords = Rect2i.createFromMinAndSize(w - font.getWidth(line) / 2, y, font.getWidth(line), font.getHeight(line));
                    canvas.drawText(entry.getKey(), coords);
                }
            }
            if (y >= -offsetTop) {
                finished = false;
            }
        }
        if (finished && autoReset) {
            resetScrolling();
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, getText(), areaHint.x);
        return font.getSize(lines);
    }

    private void updateYValues(Canvas canvas) {
        if (!textY.isEmpty()) {
            for (Entry<String, Integer> entry : textY.entrySet()) {
                textY.put(entry.getKey(), entry.getValue() - step);
            }
        } else {
            //on initialise or scrolling reset
            String[] parsed = getText().split("\\r?\\n", -1);
            Font font = canvas.getCurrentStyle().getFont();
            int y = canvas.size().y + lineSpacing;
            for (String line : parsed) {
                if (fancyFonts.isEmpty()) {
                    fancyFonts.put(HEADER1, Assets.getFont("engine:NotoSans-Regular-Large").get());
                    fancyFonts.put(HEADER2, Assets.getFont("engine:NotoSans-Regular-Medium").get());
                    fancyFonts.put(BOLD, Assets.getFont("engine:NotoSans-Bold").get());
                }
                textY.put(line, y);
                if (fancyFormatting && getFancyFont(line) != null) {
                    y += getFancyFont(line).getHeight(line) + lineSpacing;
                } else {
                    y += font.getHeight(line) + lineSpacing;
                }
            }
        }
    }

    private void drawFancyText(Canvas canvas, String s, int y) {
        UIStyle currentStyle = canvas.getCurrentStyle();
        Font font = currentStyle.getFont();
        boolean underline = false;
        for (Entry<String, Font> entry : fancyFonts.entrySet()) {
            if (s.startsWith(entry.getKey())) {
                font = entry.getValue();
                s = s.substring(entry.getKey().length());
                if (s.startsWith(UNDERLINE)) {
                    underline = true;
                    s = s.substring(3);
                }
            } else if (s.startsWith("\\" + entry.getKey())) {
                s = s.substring(entry.getKey().length() + 1);
            }
        }
        if (s.startsWith(UNDERLINE)) {
            underline = true;
            s = s.substring(3);
        }
        Rect2i coords = Rect2i.createFromMinAndSize(canvas.size().x / 2 - font.getWidth(s) / 2, y, font.getWidth(s), font.getHeight(s));
        canvas.drawTextRawShadowed(s, font, currentStyle.getTextColor(), currentStyle.getTextShadowColor(), underline, coords, currentStyle.getHorizontalAlignment(), currentStyle.getVerticalAlignment());
    }

    private Font getFancyFont(String s) {
        for (Entry<String, Font> entry : fancyFonts.entrySet()) {
            if (s.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
