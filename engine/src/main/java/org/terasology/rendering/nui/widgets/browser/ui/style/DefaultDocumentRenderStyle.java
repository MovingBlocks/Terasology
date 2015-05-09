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
package org.terasology.rendering.nui.widgets.browser.ui.style;

import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;

public class DefaultDocumentRenderStyle implements DocumentRenderStyle {
    private Canvas canvas;

    public DefaultDocumentRenderStyle(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public Integer getDocumentIndentTop() {
        return 3;
    }

    @Override
    public Integer getDocumentIndentBottom() {
        return 3;
    }

    @Override
    public Integer getDocumentIndentLeft() {
        return 3;
    }

    @Override
    public Integer getDocumentIndentRight() {
        return 3;
    }

    @Override
    public Integer getParagraphIndentTop(boolean firstParagraph) {
        return 0;
    }

    @Override
    public Integer getParagraphIndentBottom(boolean lastParagraph) {
        return 0;
    }

    @Override
    public Integer getParagraphIndentLeft() {
        return 0;
    }

    @Override
    public Integer getParagraphIndentRight() {
        return 0;
    }

    @Override
    public Integer getParagraphBackgroundIndentTop() {
        return 0;
    }

    @Override
    public Integer getParagraphBackgroundIndentBottom() {
        return 0;
    }

    @Override
    public Integer getParagraphBackgroundIndentLeft() {
        return 0;
    }

    @Override
    public Integer getParagraphBackgroundIndentRight() {
        return 0;
    }

    @Override
    public Integer getParagraphMinimumWidth() {
        return 0;
    }

    @Override
    public Font getFont(boolean hyperlink) {
        return canvas.getCurrentStyle().getFont();
    }

    @Override
    public Color getColor(boolean hyperlink) {
        if (hyperlink) {
            return Color.BLUE;
        } else {
            return canvas.getCurrentStyle().getTextColor();
        }
    }

    @Override
    public Color getBackgroundColor() {
        return null;
    }

    @Override
    public Color getParagraphBackground() {
        return null;
    }
}
