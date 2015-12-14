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
package org.terasology.rendering.nui.widgets.browser.data.basic;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.FlowLineBuilder;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.LaidFlowLine;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

import java.util.Collection;

public class FlowParagraphRenderable implements ParagraphRenderable {
    private Collection<FlowRenderable> flowParagraphData;

    public FlowParagraphRenderable(Collection<FlowRenderable> flowParagraphData) {
        this.flowParagraphData = flowParagraphData;
    }

    @Override
    public int getContentsMinWidth(ParagraphRenderStyle defaultStyle) {
        int minWidth = 0;

        for (FlowRenderable element : flowParagraphData) {
            minWidth = Math.max(minWidth, element.getMinWidth(defaultStyle));
        }

        return minWidth;
    }

    @Override
    public void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace, int leftIndent, int rightIndent,
                               ParagraphRenderStyle defaultStyle, HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister) {
        int y = startPos.y;

        for (LaidFlowLine<FlowRenderable> line : updateCacheIfNeeded(defaultStyle, startPos.y, containerRenderSpace)) {
            int x = 0;

            int insetXAdvance = containerRenderSpace.getAdvanceForVerticalPosition(y);
            int availableWidth = containerRenderSpace.getWidthForVerticalPosition(y);
            if (horizontalAlign == HorizontalAlign.LEFT || horizontalAlign == HorizontalAlign.CENTER) {
                availableWidth -= leftIndent;
            }
            if (horizontalAlign == HorizontalAlign.RIGHT || horizontalAlign == HorizontalAlign.CENTER) {
                availableWidth -= rightIndent;
            }

            int lineHeight = line.getHeight();
            int lineWidth = line.getWidth();

            int alignOffset = horizontalAlign.getOffset(lineWidth, availableWidth);

            for (FlowRenderable flowRenderable : line.getFlowRenderables()) {
                int elementWidth = flowRenderable.getWidth(defaultStyle);
                Rect2i renderableRegion = Rect2i.createFromMinAndSize(insetXAdvance + leftIndent + alignOffset + startPos.x + x, y, elementWidth, lineHeight);
                String hyperlink = flowRenderable.getAction();
                if (hyperlink != null) {
                    hyperlinkRegister.registerHyperlink(renderableRegion, hyperlink);
                }
                flowRenderable.render(canvas, renderableRegion, defaultStyle);
                x += elementWidth;
            }

            y += lineHeight;
        }
    }

    @Override
    public int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart, ContainerRenderSpace containerRenderSpace, int sideIndents) {
        int height = 0;

        for (LaidFlowLine<FlowRenderable> element : updateCacheIfNeeded(defaultStyle, yStart, containerRenderSpace)) {
            height += element.getHeight();
        }
        return height;
    }

    private Iterable<LaidFlowLine<FlowRenderable>> updateCacheIfNeeded(ParagraphRenderStyle defaultStyle, int yStart, ContainerRenderSpace containerRenderSpace) {
        // TODO introduce cache, once the RenderSpace gets stabilized and allows comparing
        return FlowLineBuilder.getLines(flowParagraphData, defaultStyle, yStart, containerRenderSpace);
    }
//
//    private static final class LineBuilderCache {
//        public final RenderSpace renderSpace;
//        public final Iterable<LaidFlowLine<FlowRenderable>> laidLines;
//
//        private LineBuilderCache(RenderSpace renderSpace, Iterable<LaidFlowLine<FlowRenderable>> laidLines) {
//            this.renderSpace = renderSpace;
//            this.laidLines = laidLines;
//        }
//    }
}
