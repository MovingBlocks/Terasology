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
package org.terasology.rendering.nui.widgets.browser.ui;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

public interface ParagraphRenderable {
    /**
     * Gets minimum width for this paragraph.
     *
     * @param defaultStyle TextRenderStyle used to render the textual information of this paragraph.
     * @return
     */
    int getContentsMinWidth(ParagraphRenderStyle defaultStyle);

    /**
     * Gets preferred height of this paragraph.
     *
     * @param defaultStyle         TextRenderStyle used to render the textual information of this paragraph.
     * @param yStart               Y position where this paragraph is starting (excluding areas limited by RenderSpace).
     * @param containerRenderSpace RenderSpace that is designated for this paragraph.
     * @param sideIndents          Side indents in pixels (sum of right and left).   @return Preferred height to render this paragraph.
     */
    int getPreferredContentsHeight(ParagraphRenderStyle defaultStyle, int yStart, ContainerRenderSpace containerRenderSpace, int sideIndents);

    /**
     * Renders the paragraph.
     *
     * @param canvas               Canvas to render the paragraph on.
     * @param startPos             Position where this paragraph is starting (excluding areas removed by RenderSpace).
     * @param containerRenderSpace RenderSpace that is designated for this paragraph.
     * @param leftIndent           Left indent that has to be added to the position allowed by the renderSpace and region.
     * @param rightIndent          Right indent that has to be added to the position allowed by the renderSpace and region.
     * @param defaultStyle         TextRenderStyle used to render the textual information of this paragraph.
     * @param horizontalAlign      Horizontal alignment used for this paragraph.
     * @param hyperlinkRegister    HyperlinkRegister used to register any hyperlink actions for elements in this paragraph.
     */
    void renderContents(Canvas canvas, Vector2i startPos, ContainerRenderSpace containerRenderSpace, int leftIndent, int rightIndent,
                        ParagraphRenderStyle defaultStyle, HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister);

    public interface HyperlinkRegister {
        void registerHyperlink(Rect2i region, String hyperlink);
    }
}
