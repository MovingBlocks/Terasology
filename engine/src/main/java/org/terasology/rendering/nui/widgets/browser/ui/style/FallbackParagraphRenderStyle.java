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
import org.terasology.rendering.nui.HorizontalAlign;

public class FallbackParagraphRenderStyle extends FallbackTextRenderStyle implements ParagraphRenderStyle {
    private ParagraphRenderStyle style;
    private ParagraphRenderStyle fallback;

    public FallbackParagraphRenderStyle(ParagraphRenderStyle style, ParagraphRenderStyle fallback) {
        super(style, fallback);
        this.style = style;
        this.fallback = fallback;
    }

    @Override
    public ContainerInteger getParagraphMarginTop() {
        ContainerInteger indentAbove = style.getParagraphMarginTop();
        if (indentAbove == null) {
            indentAbove = fallback.getParagraphMarginTop();
        }
        return indentAbove;
    }

    @Override
    public ContainerInteger getParagraphMarginBottom() {
        ContainerInteger indentBelow = style.getParagraphMarginBottom();
        if (indentBelow == null) {
            indentBelow = fallback.getParagraphMarginBottom();
        }
        return indentBelow;
    }

    @Override
    public ContainerInteger getParagraphMarginLeft() {
        ContainerInteger indentLeft = style.getParagraphMarginLeft();
        if (indentLeft == null) {
            indentLeft = fallback.getParagraphMarginLeft();
        }
        return indentLeft;
    }

    @Override
    public ContainerInteger getParagraphMarginRight() {
        ContainerInteger indentRight = style.getParagraphMarginRight();
        if (indentRight == null) {
            indentRight = fallback.getParagraphMarginRight();
        }
        return indentRight;
    }

    @Override
    public ContainerInteger getParagraphPaddingTop() {
        ContainerInteger paragraphBackgroundIndentTop = style.getParagraphPaddingTop();
        if (paragraphBackgroundIndentTop == null) {
            paragraphBackgroundIndentTop = fallback.getParagraphPaddingTop();
        }
        return paragraphBackgroundIndentTop;
    }

    @Override
    public ContainerInteger getParagraphPaddingBottom() {
        ContainerInteger paragraphBackgroundIndentBottom = style.getParagraphPaddingBottom();
        if (paragraphBackgroundIndentBottom == null) {
            paragraphBackgroundIndentBottom = fallback.getParagraphPaddingBottom();
        }
        return paragraphBackgroundIndentBottom;
    }

    @Override
    public ContainerInteger getParagraphPaddingLeft() {
        ContainerInteger paragraphBackgroundIndentLeft = style.getParagraphPaddingLeft();
        if (paragraphBackgroundIndentLeft == null) {
            paragraphBackgroundIndentLeft = fallback.getParagraphPaddingLeft();
        }
        return paragraphBackgroundIndentLeft;
    }

    @Override
    public ContainerInteger getParagraphPaddingRight() {
        ContainerInteger paragraphBackgroundIndentRight = style.getParagraphPaddingRight();
        if (paragraphBackgroundIndentRight == null) {
            paragraphBackgroundIndentRight = fallback.getParagraphPaddingRight();
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
    public ContainerInteger getParagraphMinimumWidth() {
        ContainerInteger paragraphMinimumWidth = style.getParagraphMinimumWidth();
        if (paragraphMinimumWidth == null) {
            paragraphMinimumWidth = fallback.getParagraphMinimumWidth();
        }
        return paragraphMinimumWidth;
    }

    @Override
    public HorizontalAlign getHorizontalAlignment() {
        HorizontalAlign horizontalAlignment = style.getHorizontalAlignment();
        if (horizontalAlignment == null) {
            horizontalAlignment = fallback.getHorizontalAlignment();
        }
        return horizontalAlignment;
    }

    @Override
    public FloatStyle getFloatStyle() {
        FloatStyle floatStyle = style.getFloatStyle();
        if (floatStyle == null) {
            floatStyle = fallback.getFloatStyle();
        }
        return floatStyle;
    }

    @Override
    public ClearStyle getClearStyle() {
        ClearStyle clearStyle = style.getClearStyle();
        if (clearStyle == null) {
            clearStyle = fallback.getClearStyle();
        }
        return clearStyle;
    }
}
