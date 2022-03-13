// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.list;

import org.joml.Vector2i;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.asset.font.Font;
import org.terasology.nui.util.RectUtility;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.DefaultParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.FallbackParagraphRenderStyle;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

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
        public int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart,
                                              ContainerRenderSpace containerRenderSpace, int sideIndents) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);
            int maxIndent = getMaxIndent(font);
            return paragraphData.getParagraphContents().getPreferredContentsHeight(fallbackStyle, yStart,
                    containerRenderSpace, sideIndents + maxIndent);
        }

        @Override
        public void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace,
                                   int leftIndent, int rightIndent, ParagraphRenderStyle defaultStyle, HorizontalAlign horizontalAlign,
                                   HyperlinkRegister hyperlinkRegister) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);

            int advance = containerRenderSpace.getAdvanceForVerticalPosition(startPos.y);

            String text = (index + 1) + ".";

            Rectanglei bounds = RectUtility.createFromMinAndSize(
                    startPos.x + leftIndent + advance, startPos.y, font.getWidth(text), font.getLineHeight());
            canvas.drawTextRaw(text, font, fallbackStyle.getColor(false), bounds);

            int maxIndent = getMaxIndent(font);
            paragraphData.getParagraphContents().renderContents(canvas, startPos, containerRenderSpace,
                    leftIndent + maxIndent, rightIndent, fallbackStyle, horizontalAlign, hyperlinkRegister);
        }
    }
}
