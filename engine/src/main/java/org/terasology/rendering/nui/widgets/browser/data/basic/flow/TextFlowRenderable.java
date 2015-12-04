/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.widgets.browser.data.basic.flow;

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackTextRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

public class TextFlowRenderable implements FlowRenderable<TextFlowRenderable> {
    private final String text;
    private TextRenderStyle textRenderStyle;
    private final String hyperlink;

    public TextFlowRenderable(String text, TextRenderStyle textRenderStyle, String hyperlink) {
        this.text = text;
        this.textRenderStyle = textRenderStyle;
        this.hyperlink = hyperlink;
    }

    @Override
    public void render(Canvas canvas, Rect2i bounds, TextRenderStyle defaultRenderStyle) {
        TextRenderStyle safeRenderStyle = getTextRenderStyle(defaultRenderStyle);
        Font font = safeRenderStyle.getFont(hyperlink != null);
        int lineHeight = font.getLineHeight();
        Rect2i bottomBounds = Rect2i.createFromMinAndSize(bounds.minX(), bounds.maxY() - lineHeight, bounds.sizeX(), lineHeight);
        canvas.drawTextRaw(text, font, safeRenderStyle.getColor(hyperlink != null), bottomBounds);
    }

    @Override
    public int getMinWidth(TextRenderStyle defaultRenderStyle) {
        Font font = getTextRenderStyle(defaultRenderStyle).getFont(hyperlink != null);

        int minWidth = 0;
        String[] words = text.split("[ \\n]");
        for (String word : words) {
            int width = font.getWidth(word);
            minWidth = Math.max(minWidth, width);
        }

        return minWidth;
    }

    @Override
    public int getWidth(TextRenderStyle defaultRenderStyle) {
        Font font = getTextRenderStyle(defaultRenderStyle).getFont(hyperlink != null);

        return font.getWidth(text);
    }

    @Override
    public int getHeight(TextRenderStyle defaultRenderStyle) {
        Font font = getTextRenderStyle(defaultRenderStyle).getFont(hyperlink != null);
        return font.getLineHeight();
    }

    @Override
    public String getAction() {
        return hyperlink;
    }

    @Override
    public FlowRenderable.SplitResult<TextFlowRenderable> splitAt(TextRenderStyle defaultRenderStyle, int width) {
        Font font = getTextRenderStyle(defaultRenderStyle).getFont(hyperlink != null);
        if (!text.contains("\n")) {
            int wholeTextWidth = font.getWidth(text);
            if (wholeTextWidth <= width) {
                return new SplitResult<>(this, null);
            }
        }

        int spaceWidth = font.getWidth(' ');

        boolean first = true;
        int usedSpace = 0;

        StringBuilder before = new StringBuilder();
        StringBuilder after = new StringBuilder();

        boolean appendingToBefore = true;

        String[] lines = text.split("\n");

        String[] words = lines[0].split(" ");
        for (String word : words) {
            if (appendingToBefore) {
                if (!first) {
                    usedSpace += spaceWidth;
                    before.append(" ");
                }

                usedSpace += font.getWidth(word);
                if (usedSpace > width) {
                    if (before.length() == 0) {
                        return new SplitResult<>(null, this);
                    } else {
                        appendingToBefore = false;
                        after.append(word);
                    }
                } else {
                    before.append(word);
                }
                first = false;
            } else {
                after.append(" ");
                after.append(word);
            }
        }
        if (lines[0].endsWith(" ")) {
            after.append(" ");
        }

        after = trimLeft(after);

        boolean firstLine = true;
        for (int i = 1; i < lines.length; i++) {
            if (!firstLine || after.length() > 0) {
                after.append("\n");
            }
            after.append(lines[i]);
            firstLine = false;
        }

        String beforeText = trimRight(before).toString();
        String afterText = after.toString();

        if (afterText.isEmpty()) {
            return new SplitResult<>(
                    new TextFlowRenderable(beforeText, textRenderStyle, hyperlink), null);
        } else {
            return new SplitResult<>(
                    new TextFlowRenderable(beforeText, textRenderStyle, hyperlink),
                    new TextFlowRenderable(afterText, textRenderStyle, hyperlink));
        }
    }


    private TextRenderStyle getTextRenderStyle(TextRenderStyle defaultRenderStyle) {
        if (textRenderStyle == null) {
            return defaultRenderStyle;
        }
        return new FallbackTextRenderStyle(textRenderStyle, defaultRenderStyle);
    }

    private StringBuilder trimRight(StringBuilder stringBuilder) {
        int size = stringBuilder.length();
        for (int i = size - 1; i >= 0; i--) {
            if (stringBuilder.charAt(i) != ' ') {
                stringBuilder.replace(i + 1, size, "");
                break;
            }
        }
        return stringBuilder;
    }

    private StringBuilder trimLeft(StringBuilder stringBuilder) {
        int size = stringBuilder.length();
        for (int i = 0; i < size; i++) {
            if (stringBuilder.charAt(i) != ' ') {
                stringBuilder.replace(0, i, "");
                break;
            }
        }
        return stringBuilder;
    }
}
