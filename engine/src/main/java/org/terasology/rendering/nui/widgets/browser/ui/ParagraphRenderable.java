// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.browser.ui;

import org.joml.Vector2i;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.nui.HorizontalAlign;
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
        void registerHyperlink(Rectanglei region, String hyperlink);
    }
}
