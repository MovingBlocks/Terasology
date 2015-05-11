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

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.FlowLineBuilder;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.LaidFlowLine;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.RenderSpace;
import org.terasology.rendering.nui.widgets.browser.ui.DocumentRenderer;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

import java.util.Collection;

public class FlowParagraphRenderable implements ParagraphRenderable {
    private DocumentData insetDocument;
    private int insetWidth;
    private boolean left;

    private Collection<FlowRenderable> flowParagraphData;

    private LineBuilderCache lineBuilderCache;

    public FlowParagraphRenderable(DocumentData insetDocument, boolean left, int insetWidth, Collection<FlowRenderable> flowParagraphData) {
        this.insetDocument = insetDocument;
        this.left = left;
        this.insetWidth = insetWidth;
        this.flowParagraphData = flowParagraphData;
    }

    @Override
    public int getMinWidth(TextRenderStyle defaultStyle) {
        int minWidth = 0;

        for (FlowRenderable element : flowParagraphData) {
            minWidth = Math.max(minWidth, element.getMinWidth(defaultStyle));
        }

        minWidth += this.insetWidth;

        return minWidth;
    }

    @Override
    public void render(Canvas canvas, Rect2i region, TextRenderStyle defaultStyle, HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister) {
        int y = 0;

        int width = region.width();

        int insetHeight = getInsetHeight(defaultStyle);

        if (insetDocument != null) {
            Rect2i insetRegion;
            if (left) {
                insetRegion = Rect2i.createFromMinAndSize(region.minX(), region.minY(), insetWidth, insetHeight);
            } else {
                insetRegion = Rect2i.createFromMinAndSize(region.maxX() - insetWidth, region.minY(), insetWidth, insetHeight);
            }

            DocumentRenderer.drawDocumentInRegion(insetDocument, canvas, defaultStyle.getFont(false), defaultStyle.getColor(false),
                    insetRegion, hyperlinkRegister);
        }

        updateCacheIfNeeded(defaultStyle, width, insetHeight);
        for (LaidFlowLine<FlowRenderable> line : lineBuilderCache.laidLines) {
            int x = 0;

            int insetXAdvance;
            int availableWidth;
            if (left && y < insetHeight) {
                insetXAdvance = insetWidth;
                availableWidth = width - insetWidth;
            } else {
                insetXAdvance = 0;
                availableWidth = width;
            }

            int lineHeight = line.getHeight();
            int lineWidth = line.getWidth();

            int alignOffset = horizontalAlign.getOffset(lineWidth, availableWidth);

            for (FlowRenderable flowRenderable : line.getFlowRenderables()) {
                int elementWidth = flowRenderable.getWidth(defaultStyle);
                Rect2i renderableRegion = Rect2i.createFromMinAndSize(insetXAdvance + alignOffset + region.minX() + x, region.minY() + y, elementWidth, lineHeight);
                String hyperlink = flowRenderable.getAction();
                if (hyperlink != null) {
                    hyperlinkRegister.registerHyperlink(renderableRegion, hyperlink);
                }
                flowRenderable.render(canvas, renderableRegion,
                        defaultStyle);
                x += elementWidth;
            }

            y += lineHeight;
        }
    }

    @Override
    public int getPreferredHeight(TextRenderStyle defaultStyle, int width) {
        int height = 0;

        int insetHeight = getInsetHeight(defaultStyle);

        updateCacheIfNeeded(defaultStyle, width, insetHeight);
        for (LaidFlowLine<FlowRenderable> element : lineBuilderCache.laidLines) {
            height += element.getHeight();
        }
        return Math.max(height, insetHeight);
    }

    private int getInsetHeight(TextRenderStyle defaultStyle) {
        int insetHeight = 0;
        if (insetDocument != null) {
            Vector2i documentPreferredSize = DocumentRenderer.getDocumentPreferredSize(insetDocument, defaultStyle.getFont(false), defaultStyle.getColor(false), insetWidth);
            insetHeight = documentPreferredSize.y;
        }
        return insetHeight;
    }

    private void updateCacheIfNeeded(TextRenderStyle defaultStyle, int width, int insetHeight) {
        if (lineBuilderCache == null || width != lineBuilderCache.width || insetWidth != lineBuilderCache.insetWidth
                || insetHeight != lineBuilderCache.insetHeight) {
            lineBuilderCache = new LineBuilderCache(width, insetWidth, insetHeight,
                    FlowLineBuilder.getLines(flowParagraphData, defaultStyle,
                            new RenderSpace() {
                                @Override
                                public int getWidthForVerticalPosition(int y) {
                                    if (y >= insetHeight) {
                                        return width;
                                    } else {
                                        return width - insetWidth;
                                    }
                                }
                            }));
        }
    }

    private static final class LineBuilderCache {
        public final int width;
        public final int insetWidth;
        public final int insetHeight;
        public final Iterable<LaidFlowLine<FlowRenderable>> laidLines;

        private LineBuilderCache(int width, int insetWidth, int insetHeight, Iterable<LaidFlowLine<FlowRenderable>> laidLines) {
            this.width = width;
            this.insetWidth = insetWidth;
            this.insetHeight = insetHeight;
            this.laidLines = laidLines;
        }
    }
}
