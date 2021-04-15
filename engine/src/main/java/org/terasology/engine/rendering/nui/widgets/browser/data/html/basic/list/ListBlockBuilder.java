// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.list;

import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLParseException;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLUtils;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.DefaultParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.HTMLBlockBuilderFactory;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.xml.sax.Attributes;

public class ListBlockBuilder implements HTMLBlockBuilder {
    private ParagraphData paragraphData;
    private HTMLFontResolver htmlFontResolver;
    private ListParagraphRenderable listParagraphRenderable;

    private HTMLBlockBuilderFactory htmlBlockBuilderFactory;
    private HTMLBlockBuilder paragraphBuilder;
    private int level;
    private String elementTag;

    public ListBlockBuilder(HTMLFontResolver htmlFontResolver, HTMLBlockBuilderFactory htmlBlockBuilderFactory,
                            Attributes attributes, String elementTag, boolean ordered) {
        this.htmlFontResolver = htmlFontResolver;
        this.htmlBlockBuilderFactory = htmlBlockBuilderFactory;
        this.elementTag = elementTag;

        ParagraphRenderStyle paragraphRenderStyle = HTMLUtils.createParagraphRenderStyleFromCommonAttributes(attributes);

        ListDecorator listDecorator = createListDecorator(attributes, ordered);
        listParagraphRenderable = new ListParagraphRenderable(paragraphRenderStyle, listDecorator);

        paragraphData = new DefaultParagraphData(paragraphRenderStyle, listParagraphRenderable);
    }

    private ListDecorator createListDecorator(Attributes attributes, boolean ordered) {
        if (ordered) {
            return new OrderedListDecorator();
        } else {
            return new UnorderedListDecorator();
        }
    }

    @Override
    public boolean startTag(String tag, Attributes attributes) throws HTMLParseException {
        if (tag.equalsIgnoreCase(elementTag)) {
            if (level == 0) {
                paragraphBuilder = htmlBlockBuilderFactory.create(htmlFontResolver, attributes);
            } else {
                paragraphBuilder.startTag(tag, attributes);
            }
            level++;
            return true;
        } else if (paragraphBuilder != null) {
            return paragraphBuilder.startTag(tag, attributes);
        }
        return false;
    }

    @Override
    public void text(String text) throws HTMLParseException {
        if (paragraphBuilder != null) {
            paragraphBuilder.text(text);
        } else if (!text.trim().isEmpty()) {
            throw new HTMLParseException("Unexpected text element");
        }
    }

    @Override
    public void endTag(String tag) throws HTMLParseException {
        if (tag.equalsIgnoreCase(elementTag)) {
            if (level == 1) {
                listParagraphRenderable.addListElement(paragraphBuilder.build());
                paragraphBuilder = null;
            } else {
                paragraphBuilder.endTag(tag);
            }
            level--;
        } else if (paragraphBuilder != null) {
            paragraphBuilder.endTag(tag);
        }
    }

    @Override
    public ParagraphData build() {
        return paragraphData;
    }
}
