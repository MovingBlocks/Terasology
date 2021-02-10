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

import org.terasology.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLDocumentBuilder;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLParseException;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLUtils;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
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
