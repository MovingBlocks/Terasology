// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

import org.terasology.engine.rendering.nui.widgets.browser.data.DocumentData;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class HTMLDocument implements DocumentData {
    private DocumentRenderStyle documentRenderStyle;
    private List<ParagraphData> paragraphs = new LinkedList<>();

    public HTMLDocument(DocumentRenderStyle documentRenderStyle) {
        this.documentRenderStyle = documentRenderStyle;
    }

    @Override
    public DocumentRenderStyle getDocumentRenderStyle() {
        return documentRenderStyle;
    }

    public void addParagraph(ParagraphData paragraphData) {
        paragraphs.add(paragraphData);
    }

    public void addParagraphs(Collection<? extends ParagraphData> paragraphData) {
        paragraphs.addAll(paragraphData);
    }

    @Override
    public Collection<ParagraphData> getParagraphs() {
        return paragraphs;
    }
}
