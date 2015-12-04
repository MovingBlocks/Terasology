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
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.widgets.browser.data.DocumentData;

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
        canvas.addInteractionRegion(
                new BaseInteractionListener() {
                    @Override
                    public boolean onMouseClick(NUIMouseClickEvent event) {
                        for (HyperlinkBox hyperlinkBox : hyperlinkBoxes) {
                            if (hyperlinkBox.box.contains(event.getRelativeMousePosition())) {
                                for (BrowserHyperlinkListener browserHyperlinkListener : listenerList) {
                                    browserHyperlinkListener.hyperlinkClicked(hyperlinkBox.hyperlink);
                                }

                                break;
                            }
                        }

                        return true;
                    }
                });
        if (displayedPage != null) {
            DocumentRenderer.drawDocumentInRegion(displayedPage, canvas, canvas.getCurrentStyle().getFont(), canvas.getCurrentStyle().getTextColor(), canvas.size(), register);
        }
    }


    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (displayedPage != null) {
            return DocumentRenderer.getDocumentPreferredSize(displayedPage, canvas.getCurrentStyle().getFont(), canvas.getCurrentStyle().getTextColor(),
                    canvas.getRegion().sizeX());
        } else {
            return new Vector2i(0, 0);
        }
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
