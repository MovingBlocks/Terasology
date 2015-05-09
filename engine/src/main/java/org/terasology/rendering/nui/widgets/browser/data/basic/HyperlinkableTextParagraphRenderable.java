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

import org.terasology.rendering.nui.widgets.browser.data.basic.flow.FlowLineBuilder;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.LaidFlowLine;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;
import org.terasology.math.Rect2i;
import org.terasology.rendering.nui.Canvas;

public class HyperlinkableTextParagraphRenderable implements ParagraphRenderable {
    private HyperlinkParagraphData hyperlinkParagraphData;

    private LineBuilderCache lineBuilderCache;

    public HyperlinkableTextParagraphRenderable(HyperlinkParagraphData hyperlinkParagraphData) {
        this.hyperlinkParagraphData = hyperlinkParagraphData;
    }

    @Override
    public int getMinWidth(Canvas canvas, TextRenderStyle defaultStyle) {
        int minWidth = 0;
        for (HyperlinkParagraphData.HyperlinkParagraphElement element : hyperlinkParagraphData.getElements()) {
            minWidth = Math.max(minWidth, element.getMinWidth(defaultStyle));
        }

        return minWidth;
    }

    @Override
    public void render(Canvas canvas, Rect2i region, TextRenderStyle defaultStyle, HyperlinkRegister hyperlinkRegister) {
        int y = 0;

        int width = region.width();

        updateCacheIfNeeded(defaultStyle, width);
        for (LaidFlowLine<HyperlinkParagraphData.HyperlinkParagraphElement> line : lineBuilderCache.laidLines) {
            int x = 0;

            int height = line.getHeight();

            for (HyperlinkParagraphData.HyperlinkParagraphElement hyperlinkParagraphElement : line.getFlowRenderables()) {
                int elementWidth = hyperlinkParagraphElement.getWidth(defaultStyle);
                Rect2i elementRegion = Rect2i.createFromMinAndSize(region.minX() + x, region.minY() + y, elementWidth, height);
                if (hyperlinkParagraphElement.hyperlink != null) {
                    hyperlinkRegister.registerHyperlink(elementRegion, hyperlinkParagraphElement.hyperlink);
                }
                hyperlinkParagraphElement.render(canvas, elementRegion,
                        defaultStyle);
                x += elementWidth;
            }

            y += height;
        }
    }

    @Override
    public int getPreferredHeight(Canvas canvas, TextRenderStyle defaultStyle, int width) {
        int height = 0;
        updateCacheIfNeeded(defaultStyle, width);
        for (LaidFlowLine<HyperlinkParagraphData.HyperlinkParagraphElement> element : lineBuilderCache.laidLines) {
            height += element.getHeight();
        }
        return height;
    }

    private void updateCacheIfNeeded(TextRenderStyle defaultStyle, int width) {
        if (lineBuilderCache == null || width != lineBuilderCache.width) {
            lineBuilderCache = new LineBuilderCache(width, FlowLineBuilder.getLines(hyperlinkParagraphData.getElements(), defaultStyle, width));
        }
    }

    private static final class LineBuilderCache {
        public final int width;
        public final Iterable<LaidFlowLine<HyperlinkParagraphData.HyperlinkParagraphElement>> laidLines;

        private LineBuilderCache(int width, Iterable<LaidFlowLine<HyperlinkParagraphData.HyperlinkParagraphElement>> laidLines) {
            this.width = width;
            this.laidLines = laidLines;
        }
    }
}
