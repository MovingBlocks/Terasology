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

import org.terasology.rendering.nui.Color;

public class FallbackParagraphRenderStyle extends FallbackTextRenderStyle implements ParagraphRenderStyle {
    private ParagraphRenderStyle style;
    private ParagraphRenderStyle fallback;

    public FallbackParagraphRenderStyle(ParagraphRenderStyle style, ParagraphRenderStyle fallback) {
        super(style, fallback);
        this.style = style;
        this.fallback = fallback;
    }

    @Override
    public Integer getParagraphIndentTop(boolean firstParagraph) {
        Integer indentAbove = style.getParagraphIndentTop(firstParagraph);
        if (indentAbove == null) {
            indentAbove = fallback.getParagraphIndentTop(firstParagraph);
        }
        return indentAbove;
    }

    @Override
    public Integer getParagraphIndentBottom(boolean lastParagraph) {
        Integer indentBelow = style.getParagraphIndentBottom(lastParagraph);
        if (indentBelow == null) {
            indentBelow = fallback.getParagraphIndentBottom(lastParagraph);
        }
        return indentBelow;
    }

    @Override
    public Integer getParagraphIndentLeft() {
        Integer indentLeft = style.getParagraphIndentLeft();
        if (indentLeft == null) {
            indentLeft = fallback.getParagraphIndentLeft();
        }
        return indentLeft;
    }

    @Override
    public Integer getParagraphIndentRight() {
        Integer indentRight = style.getParagraphIndentRight();
        if (indentRight == null) {
            indentRight = fallback.getParagraphIndentRight();
        }
        return indentRight;
    }

    @Override
    public Integer getParagraphBackgroundIndentTop() {
        Integer paragraphBackgroundIndentTop = style.getParagraphBackgroundIndentTop();
        if (paragraphBackgroundIndentTop == null) {
            paragraphBackgroundIndentTop = fallback.getParagraphBackgroundIndentTop();
        }
        return paragraphBackgroundIndentTop;
    }

    @Override
    public Integer getParagraphBackgroundIndentBottom() {
        Integer paragraphBackgroundIndentBottom = style.getParagraphBackgroundIndentBottom();
        if (paragraphBackgroundIndentBottom == null) {
            paragraphBackgroundIndentBottom = fallback.getParagraphBackgroundIndentBottom();
        }
        return paragraphBackgroundIndentBottom;
    }

    @Override
    public Integer getParagraphBackgroundIndentLeft() {
        Integer paragraphBackgroundIndentLeft = style.getParagraphBackgroundIndentLeft();
        if (paragraphBackgroundIndentLeft == null) {
            paragraphBackgroundIndentLeft = fallback.getParagraphBackgroundIndentLeft();
        }
        return paragraphBackgroundIndentLeft;
    }

    @Override
    public Integer getParagraphBackgroundIndentRight() {
        Integer paragraphBackgroundIndentRight = style.getParagraphBackgroundIndentRight();
        if (paragraphBackgroundIndentRight == null) {
            paragraphBackgroundIndentRight = fallback.getParagraphBackgroundIndentRight();
        }
        return paragraphBackgroundIndentRight;
    }

    @Override
    public Color getParagraphBackground() {
        Color paragraphBackground = style.getParagraphBackground();
        if (paragraphBackground == null) {
            paragraphBackground = fallback.getParagraphBackground();
        }
        return paragraphBackground;
    }

    @Override
    public Integer getParagraphMinimumWidth() {
        Integer paragraphMinimumWidth = style.getParagraphMinimumWidth();
        if (paragraphMinimumWidth == null) {
            paragraphMinimumWidth = fallback.getParagraphMinimumWidth();
        }
        return paragraphMinimumWidth;
    }
}
