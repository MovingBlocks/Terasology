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
package org.terasology.rendering.nui.widgets.browser.data.html.basic.list;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.rendering.nui.widgets.browser.data.html.basic.DefaultParagraphData;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackParagraphRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

public class OrderedListDecorator implements ListDecorator {
    private static final int LEFT_INDENT = 3;
    private static final int RIGHT_INDENT = 3;

    private int paragraphsCount;
    private Integer maxIndentCache;

    @Override
    public ParagraphData wrapParagraph(ParagraphData paragraphData) {
        DefaultParagraphData defaultParagraphData = new DefaultParagraphData(paragraphData.getParagraphRenderStyle(),
                new OrderedListParagraphRenderable(paragraphsCount, paragraphData));
        paragraphsCount++;
        return defaultParagraphData;
    }

    private int getMaxIndent(Font font) {
        if (maxIndentCache == null) {
            int minWidth = 0;
            for (int i = 0; i < paragraphsCount; i++) {
                minWidth = Math.max(minWidth, font.getWidth((i + 1) + "."));
            }
            maxIndentCache = LEFT_INDENT + minWidth + RIGHT_INDENT;
        }
        return maxIndentCache;
    }

    private final class OrderedListParagraphRenderable implements ParagraphRenderable {
        private int index;
        private ParagraphData paragraphData;

        private OrderedListParagraphRenderable(int index, ParagraphData paragraphData) {
            this.index = index;
            this.paragraphData = paragraphData;
        }

        @Override
        public int getContentsMinWidth(ParagraphRenderStyle defaultStyle) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);
            int maxIndent = getMaxIndent(font);
            return maxIndent + paragraphData.getParagraphContents().getContentsMinWidth(fallbackStyle);
        }

        @Override
        public int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart, ContainerRenderSpace containerRenderSpace, int sideIndents) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);
            int maxIndent = getMaxIndent(font);
            return paragraphData.getParagraphContents().getPreferredContentsHeight(fallbackStyle, yStart, containerRenderSpace, sideIndents + maxIndent);
        }

        @Override
        public void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace,
                int leftIndent, int rightIndent, ParagraphRenderStyle defaultStyle, HorizontalAlign horizontalAlign,
                HyperlinkRegister hyperlinkRegister) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);

            int advance = containerRenderSpace.getAdvanceForVerticalPosition(startPos.y);

            String text = (index + 1) + ".";

            Rect2i bounds = Rect2i.createFromMinAndSize(startPos.x + leftIndent + advance, startPos.y, font.getWidth(text), font.getLineHeight());
            canvas.drawTextRaw(text, font, fallbackStyle.getColor(false), bounds);

            int maxIndent = getMaxIndent(font);
            paragraphData.getParagraphContents().renderContents(canvas, startPos, containerRenderSpace,
                    leftIndent + maxIndent, rightIndent, fallbackStyle, horizontalAlign, hyperlinkRegister);
        }
    }
}
