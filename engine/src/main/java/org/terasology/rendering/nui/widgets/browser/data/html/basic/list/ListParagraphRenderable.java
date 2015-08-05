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

import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.rendering.nui.widgets.browser.data.html.basic.MultiBlockParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

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
        multiBlockParagraphRenderable.renderContents(canvas, startPos, containerRenderSpace, leftIndent, rightIndent, defaultStyle, horizontalAlign, hyperlinkRegister);
    }
}
