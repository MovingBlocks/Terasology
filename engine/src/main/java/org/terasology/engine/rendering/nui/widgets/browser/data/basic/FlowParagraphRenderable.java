// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic;

import org.joml.Vector2i;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.FlowLineBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.LaidFlowLine;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.util.RectUtility;
import org.terasology.engine.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

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
                Rectanglei renderableRegion = RectUtility.createFromMinAndSize(insetXAdvance + leftIndent + alignOffset + startPos.x + x, y, elementWidth, lineHeight);
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
