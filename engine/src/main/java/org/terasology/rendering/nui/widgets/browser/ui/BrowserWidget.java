// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.browser.ui;

import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.events.NUIMouseClickEvent;
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
                            if (hyperlinkBox.box.containsPoint(event.getRelativeMousePosition())) {
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
            DocumentRenderer.drawDocumentInRegion(
                    displayedPage, canvas, canvas.getCurrentStyle().getFont(), canvas.getCurrentStyle().getTextColor(),
                    canvas.size(), register
            );
        }
    }


    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (displayedPage != null) {
            return DocumentRenderer.getDocumentPreferredSize(
                    displayedPage, canvas.getCurrentStyle().getFont(), canvas.getCurrentStyle().getTextColor(),
                    canvas.getRegion().lengthX()
            );
        } else {
            return new Vector2i();
        }
    }

    public void navigateTo(DocumentData page) {
        this.displayedPage = page;
    }

    private final class HyperlinkBox {
        private Rectanglei box;
        private String hyperlink;

        private HyperlinkBox(Rectanglei box, String hyperlink) {
            this.box = box;
            this.hyperlink = hyperlink;
        }
    }

    private class HyperlinkRegisterImpl implements ParagraphRenderable.HyperlinkRegister {
        @Override
        public void registerHyperlink(Rectanglei region, String hyperlink) {
            hyperlinkBoxes.add(new HyperlinkBox(region, hyperlink));
        }
    }
}
