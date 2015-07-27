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
package org.terasology.rendering.nui.widgets.browser.data.html.basic;

import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.rendering.nui.widgets.browser.ui.DocumentRenderer;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackParagraphRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

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
