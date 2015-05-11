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

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.ui.style.DefaultDocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackDocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackParagraphRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;

public final class DocumentRenderer {
    private DocumentRenderer() { }

    public static Vector2i getDocumentPreferredSize(DocumentData documentData, Font defaultFont, Color defaultColor, int availableWidth) {
        int x = 0;
        int y = 0;

        DefaultDocumentRenderStyle defaultDocumentRenderStyle = new DefaultDocumentRenderStyle(defaultFont, defaultColor);

        DocumentRenderStyle documentRenderStyle = getDocumentRenderStyle(defaultDocumentRenderStyle, documentData);

        y += documentRenderStyle.getDocumentIndentTop();

        int documentIndentSides = documentRenderStyle.getDocumentIndentLeft() + documentRenderStyle.getDocumentIndentRight();
        int availableSpace = availableWidth - documentIndentSides;

        for (ParagraphData paragraphData : documentData.getParagraphs()) {
            ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(documentRenderStyle, paragraphData);
            int paragraphSideIndent = paragraphRenderStyle.getParagraphIndentLeft() + paragraphRenderStyle.getParagraphIndentRight()
                    + paragraphRenderStyle.getParagraphBackgroundIndentLeft() + paragraphRenderStyle.getParagraphBackgroundIndentRight();
            int paragraphMinWidth = Math.max(paragraphRenderStyle.getParagraphMinimumWidth(),
                    paragraphData.getParagraphContents().getMinWidth(paragraphRenderStyle));
            availableSpace = Math.max(availableSpace, paragraphSideIndent + paragraphMinWidth);
        }

        ParagraphRenderStyle lastRenderStyle = null;
        boolean first = true;
        for (ParagraphData paragraphData : documentData.getParagraphs()) {
            if (lastRenderStyle != null) {
                y += lastRenderStyle.getParagraphIndentBottom(false);
            }
            ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(documentRenderStyle, paragraphData);
            y += paragraphRenderStyle.getParagraphIndentTop(first);

            y += paragraphRenderStyle.getParagraphBackgroundIndentTop() + paragraphRenderStyle.getParagraphBackgroundIndentBottom();

            int paragraphSideIndent = paragraphRenderStyle.getParagraphIndentLeft() + paragraphRenderStyle.getParagraphIndentRight()
                    + paragraphRenderStyle.getParagraphBackgroundIndentLeft() + paragraphRenderStyle.getParagraphBackgroundIndentRight();

            y += paragraphData.getParagraphContents().getPreferredHeight(paragraphRenderStyle, availableSpace - paragraphSideIndent);

            lastRenderStyle = paragraphRenderStyle;
            first = false;
        }
        if (lastRenderStyle != null) {
            y += lastRenderStyle.getParagraphIndentBottom(true);
        }

        y += documentRenderStyle.getDocumentIndentBottom();

        // Bring back the document indents to sides
        x += availableSpace + documentIndentSides;
        return new Vector2i(x, y);
    }

    public static void drawDocumentInRegion(DocumentData documentData, Canvas canvas, Font defaultFont, Color defaultColor, Rect2i region, ParagraphRenderable.HyperlinkRegister register) {
        DefaultDocumentRenderStyle defaultDocumentRenderStyle = new DefaultDocumentRenderStyle(defaultFont, defaultColor);

        DocumentRenderStyle documentRenderStyle = getDocumentRenderStyle(defaultDocumentRenderStyle, documentData);

        int x = region.minX() + documentRenderStyle.getDocumentIndentLeft();
        int y = region.minY() + documentRenderStyle.getDocumentIndentTop();
        int availableWidth = region.width() - documentRenderStyle.getDocumentIndentLeft() - documentRenderStyle.getDocumentIndentRight();

        Color backgroundColor = documentRenderStyle.getBackgroundColor();
        if (backgroundColor != null) {
            canvas.drawFilledRectangle(canvas.getRegion(), backgroundColor);
        }

        y += documentRenderStyle.getDocumentIndentTop();

        ParagraphRenderStyle lastRenderStyle = null;
        boolean first = true;
        for (ParagraphData paragraphData : documentData.getParagraphs()) {
            if (lastRenderStyle != null) {
                y += lastRenderStyle.getParagraphIndentBottom(false);
            }
            ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(documentRenderStyle, paragraphData);

            int paragraphBorderWidth = availableWidth - paragraphRenderStyle.getParagraphIndentLeft() - paragraphRenderStyle.getParagraphIndentRight();
            int paragraphWidth = paragraphBorderWidth - paragraphRenderStyle.getParagraphBackgroundIndentLeft() - paragraphRenderStyle.getParagraphBackgroundIndentRight();

            ParagraphRenderable paragraphContents = paragraphData.getParagraphContents();
            int paragraphHeight = paragraphContents.getPreferredHeight(paragraphRenderStyle, paragraphWidth);
            int paragraphBorderHeight = paragraphHeight + paragraphRenderStyle.getParagraphBackgroundIndentTop()
                    + paragraphRenderStyle.getParagraphBackgroundIndentBottom();

            Rect2i paragraphBorderRegion =
                    Rect2i.createFromMinAndSize(x + paragraphRenderStyle.getParagraphIndentLeft(), y + paragraphRenderStyle.getParagraphIndentTop(first),
                            paragraphBorderWidth, paragraphBorderHeight);

            Color paragraphBackground = paragraphRenderStyle.getParagraphBackground();
            if (paragraphBackground != null) {
                canvas.drawFilledRectangle(paragraphBorderRegion, paragraphBackground);
            }

            Rect2i paragraphRegion =
                    Rect2i.createFromMinAndSize(x + paragraphRenderStyle.getParagraphIndentLeft() + paragraphRenderStyle.getParagraphBackgroundIndentLeft(),
                            y + paragraphRenderStyle.getParagraphIndentTop(first) + paragraphRenderStyle.getParagraphBackgroundIndentTop(),
                            paragraphWidth, paragraphHeight);

            paragraphContents.render(canvas, paragraphRegion, paragraphRenderStyle, paragraphRenderStyle.getHorizontalAlignment(), register);

            y += paragraphRenderStyle.getParagraphIndentTop(first);
            y += paragraphBorderHeight;

            lastRenderStyle = paragraphRenderStyle;
            first = false;
        }
    }

    private static DocumentRenderStyle getDocumentRenderStyle(DefaultDocumentRenderStyle defaultDocumentRenderStyle, DocumentData document) {
        DocumentRenderStyle documentStyle = document.getDocumentRenderStyle();
        if (documentStyle == null) {
            return defaultDocumentRenderStyle;
        }
        return new FallbackDocumentRenderStyle(documentStyle, defaultDocumentRenderStyle);
    }


    private static ParagraphRenderStyle getParagraphRenderStyle(DocumentRenderStyle documentRenderStyle, ParagraphData paragraphData) {
        ParagraphRenderStyle paragraphStyle = paragraphData.getParagraphRenderStyle();
        if (paragraphStyle == null) {
            return documentRenderStyle;
        }
        return new FallbackParagraphRenderStyle(paragraphStyle, documentRenderStyle);
    }

}
