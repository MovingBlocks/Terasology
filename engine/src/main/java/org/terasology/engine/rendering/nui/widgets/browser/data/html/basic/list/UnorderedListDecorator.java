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

public class UnorderedListDecorator implements ListDecorator {
    private static final int LEFT_INDENT = 3;
    private static final int RIGHT_INDENT = 3;

    private Integer maxIndentCache;

    @Override
    public ParagraphData wrapParagraph(ParagraphData paragraphData) {
        return new DefaultParagraphData(paragraphData.getParagraphRenderStyle(),
                new UnorderedListParagraphRenderable(paragraphData));
    }

    private int getMaxIndent(Font font) {
        if (maxIndentCache == null) {
            int minWidth = font.getWidth("o");
            maxIndentCache = LEFT_INDENT + minWidth + RIGHT_INDENT;
        }
        return maxIndentCache;
    }

    private final class UnorderedListParagraphRenderable implements ParagraphRenderable {
        private ParagraphData paragraphData;

        private UnorderedListParagraphRenderable(ParagraphData paragraphData) {
            this.paragraphData = paragraphData;
        }

        @Override
        public int getContentsMinWidth(ParagraphRenderStyle defaultStyle) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);
            int maxIndent = getMaxIndent(font);
            return maxIndent + paragraphData.getParagraphContents().getContentsMinWidth(defaultStyle);
        }

        @Override
        public int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart,
                                              ContainerRenderSpace containerRenderSpace, int sideIndents) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);
            int maxIndent = getMaxIndent(font);
            return paragraphData.getParagraphContents()
                    .getPreferredContentsHeight(defaultStyle, yStart, containerRenderSpace, sideIndents + maxIndent);
        }

        @Override
        public void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace, int leftIndent, int rightIndent,
                                   ParagraphRenderStyle defaultStyle, HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister) {
            FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(defaultStyle, paragraphData.getParagraphRenderStyle());
            Font font = fallbackStyle.getFont(false);

            int advance = containerRenderSpace.getAdvanceForVerticalPosition(startPos.y);

            String text = "o";

            Rectanglei bounds = RectUtility.createFromMinAndSize(
                    startPos.x + leftIndent + advance, startPos.y, font.getWidth(text), font.getLineHeight());
            canvas.drawTextRaw(text, font, fallbackStyle.getColor(false), bounds);

            int maxIndent = getMaxIndent(font);
            paragraphData.getParagraphContents().renderContents(canvas, startPos, containerRenderSpace, leftIndent + maxIndent, rightIndent,
                    defaultStyle, horizontalAlign, hyperlinkRegister);
        }
    }
}
