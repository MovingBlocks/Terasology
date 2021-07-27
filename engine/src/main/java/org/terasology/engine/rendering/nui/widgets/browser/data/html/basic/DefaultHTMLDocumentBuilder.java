// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import com.google.common.collect.Maps;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLDocument;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLDocumentBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;
import org.xml.sax.Attributes;

import java.util.Map;

public class DefaultHTMLDocumentBuilder implements HTMLDocumentBuilder {
    private Map<String, HTMLBlockBuilderFactory> paragraphBuilderFactoryMap = Maps.newHashMap();

    private HTMLFontResolver htmlFontResolver;
    private DocumentRenderStyle documentRenderStyle;

    public DefaultHTMLDocumentBuilder(HTMLFontResolver htmlFontResolver, DocumentRenderStyle documentRenderStyle) {
        this.htmlFontResolver = htmlFontResolver;
        this.documentRenderStyle = documentRenderStyle;
    }

    public void addKnownTag(String tag, HTMLBlockBuilderFactory htmlBlockBuilderFactory) {
        paragraphBuilderFactoryMap.put(tag, htmlBlockBuilderFactory);
    }

    @Override
    public HTMLBlockBuilder startTag(String tag, Attributes attributes) {
        for (Map.Entry<String, HTMLBlockBuilderFactory> paragraphBuilderFactoryEntry : paragraphBuilderFactoryMap.entrySet()) {
            if (paragraphBuilderFactoryEntry.getKey().equalsIgnoreCase(tag)) {
                return paragraphBuilderFactoryEntry.getValue().create(htmlFontResolver, attributes);
            }
        }

        return null;
    }

    @Override
    public HTMLDocument createDocument() {
        return new HTMLDocument(documentRenderStyle);
    }
}
