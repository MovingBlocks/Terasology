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

import org.terasology.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.ui.style.DefaultDocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackDocumentRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.FallbackParagraphRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.input.MouseInput;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;

import java.util.LinkedList;
import java.util.List;

public class BrowserWidget extends CoreWidget {
    private DocumentData displayedPage;

    private List<BrowserHyperlinkListener> listenerList = new LinkedList<>();

    private List<HyperlinkBox> hyperlinkBoxes = new LinkedList<>();
    private ParagraphRenderable.HyperlinkRegister register = new HyperlinkRegisterImpl();

    public void addBrowserHyperlinkListener(BrowserHyperlinkListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void onDraw(Canvas canvas) {
        hyperlinkBoxes.clear();
        if (displayedPage != null) {
            DefaultDocumentRenderStyle defaultDocumentRenderStyle = new DefaultDocumentRenderStyle(canvas);

            DocumentRenderStyle documentRenderStyle = getDocumentRenderStyle(defaultDocumentRenderStyle, displayedPage);

            Rect2i region = canvas.getRegion();

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
            for (ParagraphData paragraphData : displayedPage.getParagraphs()) {
                if (lastRenderStyle != null) {
                    y += lastRenderStyle.getParagraphIndentBottom(false);
                }
                ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(documentRenderStyle, paragraphData);

                int paragraphBorderWidth = availableWidth - paragraphRenderStyle.getParagraphIndentLeft() - paragraphRenderStyle.getParagraphIndentRight();
                int paragraphWidth = paragraphBorderWidth - paragraphRenderStyle.getParagraphBackgroundIndentLeft() - paragraphRenderStyle.getParagraphBackgroundIndentRight();

                ParagraphRenderable paragraphContents = paragraphData.getParagraphContents();
                int paragraphHeight = paragraphContents.getPreferredHeight(canvas, paragraphRenderStyle, paragraphWidth);
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

                paragraphContents.render(canvas, paragraphRegion, paragraphRenderStyle, register);

                y += paragraphRenderStyle.getParagraphIndentTop(first);
                y += paragraphBorderHeight;

                lastRenderStyle = paragraphRenderStyle;
                first = false;
            }
        }
        canvas.addInteractionRegion(
                new BaseInteractionListener() {
                    @Override
                    public boolean onMouseClick(MouseInput button, Vector2i pos) {
                        for (HyperlinkBox hyperlinkBox : hyperlinkBoxes) {
                            if (hyperlinkBox.box.contains(pos)) {
                                for (BrowserHyperlinkListener browserHyperlinkListener : listenerList) {
                                    browserHyperlinkListener.hyperlinkClicked(hyperlinkBox.hyperlink);
                                }

                                break;
                            }
                        }

                        return true;
                    }
                });
    }

    private DocumentRenderStyle getDocumentRenderStyle(DefaultDocumentRenderStyle defaultDocumentRenderStyle, DocumentData document) {
        DocumentRenderStyle documentStyle = document.getDocumentRenderStyle();
        if (documentStyle == null) {
            return defaultDocumentRenderStyle;
        }
        return new FallbackDocumentRenderStyle(documentStyle, defaultDocumentRenderStyle);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        int x = 0;
        int y = 0;

        if (displayedPage != null) {
            DefaultDocumentRenderStyle defaultDocumentRenderStyle = new DefaultDocumentRenderStyle(canvas);

            DocumentRenderStyle documentRenderStyle = getDocumentRenderStyle(defaultDocumentRenderStyle, displayedPage);

            y += documentRenderStyle.getDocumentIndentTop();

            int documentIndentSides = documentRenderStyle.getDocumentIndentLeft() + documentRenderStyle.getDocumentIndentRight();
            int availableSpace = canvas.getRegion().sizeX() - documentIndentSides;

            for (ParagraphData paragraphData : displayedPage.getParagraphs()) {
                ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(documentRenderStyle, paragraphData);
                int paragraphSideIndent = paragraphRenderStyle.getParagraphIndentLeft() + paragraphRenderStyle.getParagraphIndentRight()
                        + paragraphRenderStyle.getParagraphBackgroundIndentLeft() + paragraphRenderStyle.getParagraphBackgroundIndentRight();
                int paragraphMinWidth = Math.max(paragraphRenderStyle.getParagraphMinimumWidth(),
                        paragraphData.getParagraphContents().getMinWidth(canvas, paragraphRenderStyle));
                availableSpace = Math.max(availableSpace, paragraphSideIndent + paragraphMinWidth);
            }

            ParagraphRenderStyle lastRenderStyle = null;
            boolean first = true;
            for (ParagraphData paragraphData : displayedPage.getParagraphs()) {
                if (lastRenderStyle != null) {
                    y += lastRenderStyle.getParagraphIndentBottom(false);
                }
                ParagraphRenderStyle paragraphRenderStyle = getParagraphRenderStyle(documentRenderStyle, paragraphData);
                y += paragraphRenderStyle.getParagraphIndentTop(first);

                y += paragraphRenderStyle.getParagraphBackgroundIndentTop() + paragraphRenderStyle.getParagraphBackgroundIndentBottom();

                int paragraphSideIndent = paragraphRenderStyle.getParagraphIndentLeft() + paragraphRenderStyle.getParagraphIndentRight()
                        + paragraphRenderStyle.getParagraphBackgroundIndentLeft() + paragraphRenderStyle.getParagraphBackgroundIndentRight();

                y += paragraphData.getParagraphContents().getPreferredHeight(canvas, paragraphRenderStyle, availableSpace - paragraphSideIndent);

                lastRenderStyle = paragraphRenderStyle;
                first = false;
            }
            if (lastRenderStyle != null) {
                y += lastRenderStyle.getParagraphIndentBottom(true);
            }

            y += documentRenderStyle.getDocumentIndentBottom();

            // Bring back the document indents to sides
            x += availableSpace + documentIndentSides;
        }
        return new Vector2i(x, y);
    }

    private ParagraphRenderStyle getParagraphRenderStyle(DocumentRenderStyle documentRenderStyle, ParagraphData paragraphData) {
        ParagraphRenderStyle paragraphStyle = paragraphData.getParagraphRenderStyle();
        if (paragraphStyle == null) {
            return documentRenderStyle;
        }
        return new FallbackParagraphRenderStyle(paragraphStyle, documentRenderStyle);
    }

    public void navigateTo(DocumentData page) {
        this.displayedPage = page;
    }

    private final class HyperlinkBox {
        private Rect2i box;
        private String hyperlink;

        private HyperlinkBox(Rect2i box, String hyperlink) {
            this.box = box;
            this.hyperlink = hyperlink;
        }
    }

    private class HyperlinkRegisterImpl implements ParagraphRenderable.HyperlinkRegister {
        @Override
        public void registerHyperlink(Rect2i region, String hyperlink) {
            hyperlinkBoxes.add(new HyperlinkBox(region, hyperlink));
        }
    }
}
