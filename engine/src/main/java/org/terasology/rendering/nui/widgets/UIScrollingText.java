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

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A widget that scrolls through long strings of text
 */
public class UIScrollingText extends CoreWidget {
    /**
     * The text to be shown by this widget
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
     * Maps text to their Y coordinates
     */
    private Map<String, Integer> textY = new HashMap<String, Integer>();
    /**
     * Specifies whether scrolling will restart from the beginning when all text has been scrolled through
     */
    private boolean autoReset;
    private boolean isScrolling = true;

    /**
     * Creates an empty {@code UIScrollingText}.
     */
    public UIScrollingText() {
    }

    /**
     * Creates an empty {@code UIScrollingText} with the given id.
     *
     * @param text The text to be shown in this {@code UIScrollingText}
     */
    public UIScrollingText(String text) {
        this.text.set(text);
    }

    /**
     * Creates a {@code UIScrollingText} containing the text in the given {@link Binding}.
     *
     * @param text the {@code Binding} containing the text to be shown in this {@code UIScrollingText}
     */
    public UIScrollingText(Binding<String> text) {
        this.text = text;
    }

    /**
     * Creates a {@code UIScrollingText} with the given id, containing the given text.
     *
     * @param id The id assigned to this {@code UIScrollingText}
     * @param text The text to be shown in this {@code UIScrollingText}
     */
    public UIScrollingText(String id, String text) {
        super(id);
        this.text.set(text);
    }

    /**
     * Creates a {@code UIScrollingText} with the given id, containing the text in the given {@code Binding}.
     *
     * @param id The id assigned to this {@code UIScrollingText}
     * @param text The text to be shown in this {@code UIScrollingText}
     */
    public UIScrollingText(String id, Binding<String> text) {
        super(id);
        this.text = text;
    }

    /**
     * Retrieves the text shown in this {@code UIScrollingText}.
     * }
     * @return The text shown in this {@code UIScrollingText}
     */
    public String getText() {
        if (text.get() == null) {
            return "";
        }
        return text.get();
    }

    /**
     * Sets the text shown in this {@code UIScrollingText}.
     *
     * @param text The text to be shown in the {@code UIScrollingText}
     */
    public void setText(String text) {
        this.text.set(text);
    }

    /**
     * Binds the text to be shown in this {@code UIScrollingText}.
     *
     * @param binding The {@code Binding} containing the text to be shown in this {@code UIScrollingText}
     */
    public void bindText(Binding<String> binding) {
        this.text = binding;
    }

    /**
     * Starts the scrolling of the text shown in this {@code UIScrollingText}.
     */
    public void startScrolling() {
        isScrolling = true;
    }

    /**
     * Stops the scrolling of the text shown in this {@code UIScrollingText}.
     */
    public void stopScrolling() {
        isScrolling = false;
    }

    /**
     * Resets the {@code UIScrollingText} to the beginning of the text.
     */
    public void resetScrolling() {
        textY.clear();
    }

    /**
     * Sets the scrolling speed of the text shown in the {@code UIScrollingText}.
     *
     * @param speed The increase in the Y values of the text shown each frame, in pixels
     */
    public void setScrollingSpeed(int speed) {
        this.step = speed;
    }

    /**
     * Specifies whether scrolling will restart from the beginning when all text has been scrolled through.
     *
     * @param reset Whether the {@code UIScrollingText} will reset automatically
     */
    public void setAutoReset(boolean reset) {
        this.autoReset = reset;
    }

    /**
     * Handles how this {@code UIScrollingText} is drawn.
     * This is called every frame.
     *
     * @param canvas The {@link Canvas} on which this {@code UIScrollingText} is drawn
     */
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
                Rect2i coords = Rect2i.createFromMinAndSize(w - font.getWidth(line) / 2, y, font.getWidth(line), font.getHeight(line));
                canvas.drawText(entry.getKey(), coords);
            }
            if (y >= -offsetTop) {
                finished = false;
            }
        }
        if (finished && autoReset) {
            resetScrolling();
        }
    }

    /**
     * Retrieves the preferred content size of this {@code UIScrollingText}.
     * This is the minimum size this layout will take, given no space restrictions.
     *
     * @param canvas The {@code Canvas} on which this {@code UIScrollingText} is drawn
     * @param areaHint A {@link Vector2i} representing the available space for this {@code UIScrollingText}
     * @return A {@code Vector2i} representing the preferred content size of this {@code UIScrollingText}
     */
    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, getText(), areaHint.x);
        return font.getSize(lines);
    }

    /**
     * Updates the Y values of the text shown in this {@code UIScrollingText} each frame
     * or initialises them if they are empty.
     *
     * @param canvas The {@code Canvas} on which this {@code UIScrollingText} is drawn
     */
    private void updateYValues(Canvas canvas) {
        if (!textY.isEmpty()) {
            for (Entry<String, Integer> entry : textY.entrySet()) {
                textY.put(entry.getKey(), entry.getValue() - step);
            }
        } else {
            String[] parsed = getText().split("\\r?\\n", -1);
            Font font = canvas.getCurrentStyle().getFont();
            int y = canvas.size().y + lineSpacing;
            for (String line : parsed) {
                textY.put(line, y);
                y += font.getHeight(line) + lineSpacing;
            }
        }
    }
}
