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
package org.terasology.rendering.nui.widgets.browser.data.html.basic;

import com.google.api.client.util.Maps;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLDocument;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLDocumentBuilder;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.terasology.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;
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
