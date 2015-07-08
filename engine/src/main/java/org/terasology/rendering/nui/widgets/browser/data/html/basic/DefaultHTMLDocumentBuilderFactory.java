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

import org.terasology.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLDocumentBuilder;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLDocumentBuilderFactory;
import org.terasology.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.terasology.rendering.nui.widgets.browser.data.html.basic.list.ListBlockBuilder;
import org.terasology.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;
import org.xml.sax.Attributes;

public class DefaultHTMLDocumentBuilderFactory implements HTMLDocumentBuilderFactory {
    private HTMLFontResolver htmlFontResolver;

    public DefaultHTMLDocumentBuilderFactory(HTMLFontResolver htmlFontResolver) {
        this.htmlFontResolver = htmlFontResolver;
    }

    @Override
    public HTMLDocumentBuilder create(Attributes attributes) {
        DefaultHTMLDocumentBuilder htmlDocumentBuilder = new DefaultHTMLDocumentBuilder(htmlFontResolver, createDocumentStyle(attributes));
        ParagraphBuilderFactory htmlParagraphBuilderFactory = new ParagraphBuilderFactory();
        htmlDocumentBuilder.addKnownTag("p", htmlParagraphBuilderFactory);
        MultiBlockBuilderFactory multiBlockBuilderFactory = new MultiBlockBuilderFactory(htmlDocumentBuilder);
        htmlDocumentBuilder.addKnownTag("ul", new ListBlockBuilderFactory(false, multiBlockBuilderFactory));
        htmlDocumentBuilder.addKnownTag("ol", new ListBlockBuilderFactory(true, multiBlockBuilderFactory));
        return htmlDocumentBuilder;
    }

    private DocumentRenderStyle createDocumentStyle(Attributes attributes) {
        return null;
    }

    private static class ParagraphBuilderFactory implements HTMLBlockBuilderFactory {
        @Override
        public HTMLBlockBuilder create(HTMLFontResolver htmlFontResolver, Attributes attributes) {
            ParagraphBuilder paragraphBuilder = new ParagraphBuilder(htmlFontResolver, attributes);
            paragraphBuilder.addKnownFlowTag("img", new ImageFlowRenderableFactory());
            return paragraphBuilder;
        }
    }

    private static final class ListBlockBuilderFactory implements HTMLBlockBuilderFactory {
        private boolean ordered;
        private HTMLBlockBuilderFactory paragraphBuilderFactory;

        private ListBlockBuilderFactory(boolean ordered, HTMLBlockBuilderFactory paragraphBuilderFactory) {
            this.ordered = ordered;
            this.paragraphBuilderFactory = paragraphBuilderFactory;
        }

        @Override
        public HTMLBlockBuilder create(HTMLFontResolver htmlFontResolver, Attributes attributes) {
            return new ListBlockBuilder(htmlFontResolver, paragraphBuilderFactory, attributes, "li", ordered);
        }
    }

    private static final class MultiBlockBuilderFactory implements HTMLBlockBuilderFactory {
        private HTMLDocumentBuilder htmlDocumentBuilder;

        private MultiBlockBuilderFactory(HTMLDocumentBuilder htmlDocumentBuilder) {
            this.htmlDocumentBuilder = htmlDocumentBuilder;
        }

        @Override
        public HTMLBlockBuilder create(HTMLFontResolver htmlFontResolver, Attributes attributes) {
            return new MultiBlockBuilder(htmlDocumentBuilder, htmlFontResolver, attributes);
        }
    }
}
