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
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.basic.flow.ContainerRenderSpace;
import org.terasology.rendering.nui.widgets.browser.ui.style.DefaultDocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackDocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackParagraphRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

import java.util.Collection;

public final class DocumentRenderer {
    private DocumentRenderer() {
    }

    public static Vector2i getDocumentPreferredSize(DocumentData documentData, Font defaultFont, Color defaultColor, int availableWidth) {
        DefaultDocumentRenderStyle defaultDocumentRenderStyle = new DefaultDocumentRenderStyle(defaultFont, defaultColor);

        DocumentRenderStyle documentRenderStyle = getDocumentRenderStyle(defaultDocumentRenderStyle, documentData);

        Collection<ParagraphData> paragraphs = documentData.getParagraphs();
        int minParagraphsWidth = getParagraphsMinimumWidth(availableWidth, documentRenderStyle, paragraphs);

        int documentSideMargins = documentRenderStyle.getDocumentMarginLeft().getValue(availableWidth)
                + documentRenderStyle.getDocumentMarginRight().getValue(availableWidth);
        int documentWidth = Math.max(availableWidth, minParagraphsWidth + documentSideMargins);

        ContainerFlowContainerRenderSpace containerRenderSpace = new ContainerFlowContainerRenderSpace(documentWidth);
        int preferredHeight = Math.max(
                getParagraphsPreferredHeight(documentRenderStyle, paragraphs, containerRenderSpace, 0),
                containerRenderSpace.getNextClearYPosition(ParagraphRenderStyle.ClearStyle.BOTH));

        int documentVerticalMargins = documentRenderStyle.getDocumentMarginTop().getValue(documentWidth)
                + documentRenderStyle.getDocumentMarginBottom().getValue(documentWidth);

        // Bring back the document indents to sides
        return new Vector2i(documentWidth, preferredHeight + documentVerticalMargins);
    }

    public static int getParagraphsMinimumWidth(int availableWidth, ParagraphRenderStyle baseParagraphRenderStyle, Collection<ParagraphData> paragraphs) {
        int minParagraphsWidth = 0;
        for (ParagraphData paragraphData : paragraphs) {
            ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(baseParagraphRenderStyle, paragraphData);
            int paragraphSideIndent = paragraphRenderStyle.getParagraphMarginLeft().getValue(availableWidth)
                    + paragraphRenderStyle.getParagraphMarginRight().getValue(availableWidth)
                    + paragraphRenderStyle.getParagraphPaddingLeft().getValue(availableWidth)
                    + paragraphRenderStyle.getParagraphPaddingRight().getValue(availableWidth);
            int paragraphMinWidth = Math.max(paragraphRenderStyle.getParagraphMinimumWidth().getValue(availableWidth),
                    paragraphData.getParagraphContents().getContentsMinWidth(paragraphRenderStyle));
            minParagraphsWidth = Math.max(minParagraphsWidth, paragraphSideIndent + paragraphMinWidth);
        }
        return minParagraphsWidth;
    }

    public static int getParagraphsPreferredHeight(ParagraphRenderStyle baseRenderStyle, Collection<ParagraphData> paragraphs,
                                                   ContainerRenderSpace containerRenderSpace, int yStart) {
        int containerWidth = containerRenderSpace.getContainerWidth();
        int yShift = yStart;
        for (ParagraphData paragraphData : paragraphs) {
            ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(baseRenderStyle, paragraphData);

            ParagraphRenderStyle.ClearStyle clearStyle = paragraphRenderStyle.getClearStyle();
            if (clearStyle != ParagraphRenderStyle.ClearStyle.NONE) {
                yShift = Math.max(yShift, containerRenderSpace.getNextClearYPosition(clearStyle));
            }

            ParagraphRenderStyle.FloatStyle floatStyle = paragraphRenderStyle.getFloatStyle();
            if (floatStyle == ParagraphRenderStyle.FloatStyle.LEFT
                    || floatStyle == ParagraphRenderStyle.FloatStyle.RIGHT) {
                int paragraphMinWidth = Math.max(paragraphRenderStyle.getParagraphMinimumWidth().getValue(containerWidth),
                        paragraphData.getParagraphContents().getContentsMinWidth(paragraphRenderStyle));
                int paragraphSideIndent = paragraphRenderStyle.getParagraphMarginLeft().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphMarginRight().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphPaddingLeft().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphPaddingRight().getValue(containerWidth);

                int height = paragraphRenderStyle.getParagraphMarginTop().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphPaddingTop().getValue(containerWidth);

                height += paragraphData.getParagraphContents().getPreferredContentsHeight(paragraphRenderStyle, 0,
                        new ContainerFlowContainerRenderSpace(paragraphMinWidth), paragraphSideIndent);

                height += paragraphRenderStyle.getParagraphPaddingBottom().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphMarginBottom().getValue(containerWidth);

                if (floatStyle == ParagraphRenderStyle.FloatStyle.LEFT) {
                    Rect2i position = containerRenderSpace.addLeftFloat(yShift, paragraphMinWidth, height);
                    yShift = position.minY();
                } else {
                    Rect2i position = containerRenderSpace.addRightFloat(yShift, paragraphMinWidth, height);
                    yShift = position.minY();
                }
            } else {
                yShift += paragraphRenderStyle.getParagraphMarginTop().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphPaddingTop().getValue(containerWidth);

                int paragraphSideIndent = paragraphRenderStyle.getParagraphMarginLeft().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphMarginRight().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphPaddingLeft().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphPaddingRight().getValue(containerWidth);

                yShift += paragraphData.getParagraphContents().getPreferredContentsHeight(paragraphRenderStyle, yShift, containerRenderSpace, paragraphSideIndent);

                yShift += paragraphRenderStyle.getParagraphPaddingBottom().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphMarginBottom().getValue(containerWidth);
            }
        }
        return yShift - yStart;
    }

    public static void drawDocumentInRegion(DocumentData documentData, Canvas canvas, Font defaultFont, Color defaultColor,
                                            Vector2i size, ParagraphRenderable.HyperlinkRegister register) {
        DefaultDocumentRenderStyle defaultDocumentRenderStyle = new DefaultDocumentRenderStyle(defaultFont, defaultColor);

        DocumentRenderStyle documentRenderStyle = getDocumentRenderStyle(defaultDocumentRenderStyle, documentData);

        int documentWidth = size.x;
        int documentMarginLeft = documentRenderStyle.getDocumentMarginLeft().getValue(documentWidth);
        int documentMarginRight = documentRenderStyle.getDocumentMarginRight().getValue(documentWidth);

        int documentMarginTop = documentRenderStyle.getDocumentMarginTop().getValue(documentWidth);

        Color backgroundColor = documentRenderStyle.getBackgroundColor();
        if (backgroundColor != null) {
            canvas.drawFilledRectangle(canvas.getRegion(), backgroundColor);
        }

        Collection<ParagraphData> paragraphs = documentData.getParagraphs();

        ContainerFlowContainerRenderSpace renderSpace = new ContainerFlowContainerRenderSpace(documentWidth);

        renderParagraphs(canvas, register, documentRenderStyle, documentMarginLeft, documentMarginTop, documentMarginLeft, documentMarginRight, paragraphs, renderSpace);
    }

    public static void renderParagraphs(Canvas canvas, ParagraphRenderable.HyperlinkRegister register, ParagraphRenderStyle baseRenderStyle,
                                        int xShift, int startY, int leftIndent, int rightIndent, Collection<ParagraphData> paragraphs,
                                        ContainerRenderSpace containerRenderSpace) {
        int containerWidth = containerRenderSpace.getContainerWidth();

        int yShift = startY;
        for (ParagraphData paragraphData : paragraphs) {
            yShift += renderParagraph(canvas, register, baseRenderStyle, xShift, leftIndent, rightIndent, containerRenderSpace,
                    containerWidth, yShift, paragraphData);
        }
    }

    public static int renderParagraph(Canvas canvas, ParagraphRenderable.HyperlinkRegister register, ParagraphRenderStyle baseRenderStyle,
                                      int xShift, int leftIndent, int rightIndent, ContainerRenderSpace containerRenderSpace, int containerWidth,
                                      int startY, ParagraphData paragraphData) {
        int yShift = startY;
        ParagraphRenderable paragraphContents = paragraphData.getParagraphContents();

        ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(baseRenderStyle, paragraphData);

        ParagraphRenderStyle.ClearStyle clearStyle = paragraphRenderStyle.getClearStyle();
        if (clearStyle != ParagraphRenderStyle.ClearStyle.NONE) {
            yShift = Math.max(yShift, containerRenderSpace.getNextClearYPosition(clearStyle));
        }

        ParagraphRenderStyle.FloatStyle floatStyle = paragraphRenderStyle.getFloatStyle();
        if (floatStyle == ParagraphRenderStyle.FloatStyle.LEFT
                || floatStyle == ParagraphRenderStyle.FloatStyle.RIGHT) {
            int leftParagraphIndent = paragraphRenderStyle.getParagraphMarginLeft().getValue(containerWidth)
                    + paragraphRenderStyle.getParagraphPaddingLeft().getValue(containerWidth);
            int rightParagraphIndent = paragraphRenderStyle.getParagraphMarginRight().getValue(containerWidth)
                    + paragraphRenderStyle.getParagraphPaddingRight().getValue(containerWidth);

            int paragraphWidth = Math.max(paragraphRenderStyle.getParagraphMinimumWidth().getValue(containerWidth),
                    paragraphContents.getContentsMinWidth(paragraphRenderStyle) + leftParagraphIndent + rightParagraphIndent);

            int height = paragraphRenderStyle.getParagraphMarginTop().getValue(containerWidth)
                    + paragraphRenderStyle.getParagraphPaddingTop().getValue(containerWidth);

            int paragraphHeight = paragraphContents.getPreferredContentsHeight(paragraphRenderStyle, 0,
                    new ContainerFlowContainerRenderSpace(paragraphWidth), leftParagraphIndent + rightParagraphIndent);

            height += paragraphHeight;

            height += paragraphRenderStyle.getParagraphPaddingBottom().getValue(containerWidth)
                    + paragraphRenderStyle.getParagraphMarginBottom().getValue(containerWidth);

            Rect2i position;
            if (floatStyle == ParagraphRenderStyle.FloatStyle.LEFT) {
                position = containerRenderSpace.addLeftFloat(yShift, paragraphWidth, height);
            } else {
                position = containerRenderSpace.addRightFloat(yShift, paragraphWidth, height);
            }

            Rect2i paragraphBorderRegion =
                    Rect2i.createFromMinAndMax(position.minX() + paragraphRenderStyle.getParagraphMarginLeft().getValue(containerWidth),
                            position.minY() + paragraphRenderStyle.getParagraphMarginTop().getValue(containerWidth),
                            position.maxX() - paragraphRenderStyle.getParagraphMarginRight().getValue(containerWidth) - 1,
                            position.maxY() - paragraphRenderStyle.getParagraphMarginBottom().getValue(containerWidth) - 1);

            Color paragraphBackground = paragraphRenderStyle.getParagraphBackground();
            if (paragraphBackground != null) {
                canvas.drawFilledRectangle(paragraphBorderRegion, paragraphBackground);
            }

            Vector2i paragraphStart = new Vector2i(position.minX(),
                    position.minY()
                            + paragraphRenderStyle.getParagraphMarginTop().getValue(containerWidth)
                            + paragraphRenderStyle.getParagraphPaddingTop().getValue(containerWidth));

            paragraphContents.renderContents(canvas, paragraphStart, new ContainerFlowContainerRenderSpace(paragraphWidth),
                    leftParagraphIndent, rightParagraphIndent, paragraphRenderStyle, paragraphRenderStyle.getHorizontalAlignment(), register);

            yShift = position.minY();
        } else {
            yShift += paragraphRenderStyle.getParagraphMarginTop().getValue(containerWidth);

            int leftParagraphIndent = paragraphRenderStyle.getParagraphMarginLeft().getValue(containerWidth)
                    + paragraphRenderStyle.getParagraphPaddingLeft().getValue(containerWidth);
            int rightParagraphIndent = paragraphRenderStyle.getParagraphMarginRight().getValue(containerWidth)
                    + paragraphRenderStyle.getParagraphPaddingRight().getValue(containerWidth);

            int paragraphHeight = paragraphContents.getPreferredContentsHeight(paragraphRenderStyle,
                    yShift + paragraphRenderStyle.getParagraphPaddingTop().getValue(containerWidth), containerRenderSpace,
                    leftIndent + leftParagraphIndent + rightParagraphIndent + rightIndent);

            Color paragraphBackground = paragraphRenderStyle.getParagraphBackground();
            if (paragraphBackground != null) {
                int borderAdvance = 0;
                int borderHeight = paragraphHeight + paragraphRenderStyle.getParagraphPaddingTop().getValue(containerWidth)
                        + paragraphRenderStyle.getParagraphPaddingBottom().getValue(containerWidth);

                while (borderAdvance < borderHeight) {
                    int backgroundStart = yShift + borderAdvance;
                    int availableBackgroundWidth = containerRenderSpace.getWidthForVerticalPosition(backgroundStart);
                    int backgroundAdvance = containerRenderSpace.getAdvanceForVerticalPosition(backgroundStart);
                    int maxSpace = containerRenderSpace.getNextWidthChange(backgroundStart);

                    Rect2i backgroundRegion =
                            Rect2i.createFromMinAndSize(
                                    xShift + paragraphRenderStyle.getParagraphMarginLeft().getValue(containerWidth) + backgroundAdvance,
                                    backgroundStart,
                                    availableBackgroundWidth - 1,
                                    Math.min(maxSpace, borderHeight - borderAdvance) - 1);

                    canvas.drawFilledRectangle(backgroundRegion, paragraphBackground);

                    borderAdvance += maxSpace - backgroundStart;
                }
            }

            yShift += paragraphRenderStyle.getParagraphPaddingTop().getValue(containerWidth);

            paragraphContents.renderContents(canvas, new Vector2i(xShift, yShift), containerRenderSpace, leftIndent + leftParagraphIndent,
                    rightIndent + rightParagraphIndent, paragraphRenderStyle, paragraphRenderStyle.getHorizontalAlignment(), register);

            yShift += paragraphHeight;

            yShift += paragraphRenderStyle.getParagraphPaddingBottom().getValue(containerWidth);

            yShift += paragraphRenderStyle.getParagraphMarginBottom().getValue(containerWidth);
        }
        return yShift - startY;
    }

    private static DocumentRenderStyle getDocumentRenderStyle(DefaultDocumentRenderStyle defaultDocumentRenderStyle, DocumentData document) {
        DocumentRenderStyle documentStyle = document.getDocumentRenderStyle();
        if (documentStyle == null) {
            return defaultDocumentRenderStyle;
        }
        return new FallbackDocumentRenderStyle(documentStyle, defaultDocumentRenderStyle);
    }


    private static ParagraphRenderStyle getParagraphRenderStyle(ParagraphRenderStyle documentRenderStyle, ParagraphData paragraphData) {
        ParagraphRenderStyle paragraphStyle = paragraphData.getParagraphRenderStyle();
        if (paragraphStyle == null) {
            return documentRenderStyle;
        }
        return new FallbackParagraphRenderStyle(paragraphStyle, documentRenderStyle);
    }
}
