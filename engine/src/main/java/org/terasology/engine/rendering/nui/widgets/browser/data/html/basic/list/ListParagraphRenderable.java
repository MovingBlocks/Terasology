// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.list;

import org.joml.Vector2i;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.nui.Canvas;
import org.terasology.nui.HorizontalAlign;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.MultiBlockParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

public class ListParagraphRenderable implements ParagraphRenderable {
    private MultiBlockParagraphRenderable multiBlockParagraphRenderable;
    private ListDecorator listDecorator;

    public ListParagraphRenderable(ParagraphRenderStyle paragraphRenderStyle, ListDecorator listDecorator) {
        multiBlockParagraphRenderable = new MultiBlockParagraphRenderable(paragraphRenderStyle);
        this.listDecorator = listDecorator;
    }

    public void addListElement(ParagraphData paragraphData) {
        multiBlockParagraphRenderable.addParagraph(listDecorator.wrapParagraph(paragraphData));
    }

    @Override
    public int getContentsMinWidth(ParagraphRenderStyle defaultStyle) {
        return multiBlockParagraphRenderable.getContentsMinWidth(defaultStyle);
    }

    @Override
    public int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart, ContainerRenderSpace containerRenderSpace, int sideIndents) {
        return multiBlockParagraphRenderable.getPreferredContentsHeight(defaultStyle, yStart, containerRenderSpace, sideIndents);
    }

    @Override
    public void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace, int leftIndent, int rightIndent,
                               ParagraphRenderStyle defaultStyle, HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister) {
        multiBlockParagraphRenderable.renderContents(canvas, startPos, containerRenderSpace, leftIndent, rightIndent,
                defaultStyle, horizontalAlign, hyperlinkRegister);
    }
}
