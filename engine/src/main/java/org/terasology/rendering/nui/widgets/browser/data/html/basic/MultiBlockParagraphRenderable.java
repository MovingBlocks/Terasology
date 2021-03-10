// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import org.joml.Vector2i;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.nui.Canvas;
import org.terasology.nui.HorizontalAlign;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.ui.DocumentRenderer;
import org.terasology.engine.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.FallbackParagraphRenderStyle;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

import java.util.LinkedList;
import java.util.List;

public class MultiBlockParagraphRenderable implements ParagraphRenderable {
    private List<ParagraphData> paragraphs = new LinkedList<>();
    private ParagraphRenderStyle paragraphRenderStyle;

    public MultiBlockParagraphRenderable(ParagraphRenderStyle paragraphRenderStyle) {
        this.paragraphRenderStyle = paragraphRenderStyle;
    }

    public void addParagraph(ParagraphData paragraphData) {
        paragraphs.add(paragraphData);
    }

    @Override
    public int getContentsMinWidth(ParagraphRenderStyle defaultStyle) {
        FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(paragraphRenderStyle, defaultStyle);
        int paragraphIndents = fallbackStyle.getParagraphMarginLeft().getValue(0)
                + fallbackStyle.getParagraphPaddingLeft().getValue(0)
                + fallbackStyle.getParagraphPaddingRight().getValue(0)
                + fallbackStyle.getParagraphMarginRight().getValue(0);
        return paragraphIndents + DocumentRenderer.getParagraphsMinimumWidth(0, fallbackStyle, paragraphs);
    }

    @Override
    public int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart, ContainerRenderSpace containerRenderSpace, int sideIndents) {
        FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(paragraphRenderStyle, defaultStyle);
        int containerWidth = containerRenderSpace.getContainerWidth();
        int topIndent = fallbackStyle.getParagraphMarginTop().getValue(containerWidth)
                + fallbackStyle.getParagraphPaddingTop().getValue(containerWidth);
        int paragraphIndents = topIndent
                + fallbackStyle.getParagraphPaddingBottom().getValue(containerWidth)
                + fallbackStyle.getParagraphMarginBottom().getValue(containerWidth);
        return paragraphIndents + DocumentRenderer.getParagraphsPreferredHeight(fallbackStyle, paragraphs, containerRenderSpace, yStart + topIndent);
    }

    @Override
    public void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace,
                               int leftIndent, int rightIndent, ParagraphRenderStyle defaultStyle,
                               HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister) {
        FallbackParagraphRenderStyle fallbackStyle = new FallbackParagraphRenderStyle(paragraphRenderStyle, defaultStyle);
        int containerWidth = containerRenderSpace.getContainerWidth();
        int leftIndents = fallbackStyle.getParagraphMarginLeft().getValue(containerWidth)
                + fallbackStyle.getParagraphPaddingLeft().getValue(containerWidth);
        int rightIndents = fallbackStyle.getParagraphPaddingRight().getValue(containerWidth)
                + fallbackStyle.getParagraphMarginRight().getValue(containerWidth);
        int topIndents = fallbackStyle.getParagraphMarginTop().getValue(containerWidth)
                + fallbackStyle.getParagraphPaddingTop().getValue(containerWidth);

        DocumentRenderer.renderParagraphs(canvas, hyperlinkRegister, fallbackStyle, startPos.x, startPos.y + topIndents,
                leftIndent + leftIndents, rightIndent + rightIndents, paragraphs, containerRenderSpace);
    }
}
