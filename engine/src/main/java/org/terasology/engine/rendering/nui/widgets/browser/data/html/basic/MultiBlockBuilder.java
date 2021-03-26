// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLDocumentBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLParseException;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLUtils;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.xml.sax.Attributes;

public class MultiBlockBuilder implements HTMLBlockBuilder {
    private final HTMLFontResolver htmlFontResolver;
    private MultiBlockParagraphRenderable renderable;

    private HTMLDocumentBuilder htmlDocumentBuilder;

    private HTMLBlockBuilder currentBlockBuilder;
    private String currentBlockTag;
    private int level;
    private final ParagraphRenderStyle paragraphRenderStyle;

    public MultiBlockBuilder(HTMLDocumentBuilder htmlDocumentBuilder, HTMLFontResolver htmlFontResolver, Attributes attributes) {
        this.htmlDocumentBuilder = htmlDocumentBuilder;
        this.htmlFontResolver = htmlFontResolver;
        paragraphRenderStyle = HTMLUtils.createParagraphRenderStyleFromCommonAttributes(attributes);
        renderable = new MultiBlockParagraphRenderable(paragraphRenderStyle);
    }

    @Override
    public boolean startTag(String tag, Attributes attributes) throws HTMLParseException {
        if (currentBlockBuilder != null) {
            if (currentBlockTag.equalsIgnoreCase(tag)) {
                level++;
            }
            if (currentBlockBuilder.startTag(tag, attributes)) {
                return true;
            } else {
                throw new HTMLParseException("Unexpected document structure");
            }
        } else {
            currentBlockBuilder = htmlDocumentBuilder.startTag(tag, attributes);
            currentBlockTag = tag;
            return true;
        }
    }

    @Override
    public void text(String text) throws HTMLParseException {
        if (currentBlockBuilder != null) {
            currentBlockBuilder.text(text);
        } else {
            throw new HTMLParseException("Unexpected text element");
        }
    }

    @Override
    public void endTag(String tag) throws HTMLParseException {
        if (currentBlockBuilder != null) {
            if (tag.equalsIgnoreCase(tag)) {
                if (level == 0) {
                    renderable.addParagraph(currentBlockBuilder.build());
                    currentBlockBuilder = null;
                    currentBlockTag = null;
                } else {
                    currentBlockBuilder.endTag(tag);
                    level--;
                }
            } else {
                currentBlockBuilder.endTag(tag);
            }
        } else {
            throw new HTMLParseException("Unexpected document structure");
        }
    }

    @Override
    public ParagraphData build() {
        return new DefaultParagraphData(paragraphRenderStyle, renderable);
    }
}
