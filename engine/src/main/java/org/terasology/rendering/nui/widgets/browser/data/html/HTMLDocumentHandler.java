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
package org.terasology.rendering.nui.widgets.browser.data.html;

import org.terasology.rendering.nui.widgets.browser.data.DocumentData;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class HTMLDocumentHandler extends DefaultHandler {
    private HTMLDocumentBuilderFactory htmlDocumentBuilderFactory;

    private HTMLDocumentBuilder currentDocumentBuilder;
    private HTMLDocument currentDocument;

    private DocumentData resultDocument;

    private HTMLBlockBuilder currentParagraphBuilder;
    private String currentParagraphTag;
    private int level;

    public HTMLDocumentHandler(HTMLDocumentBuilderFactory htmlDocumentBuilderFactory) {
        this.htmlDocumentBuilderFactory = htmlDocumentBuilderFactory;
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (currentDocumentBuilder == null) {
            if (!qName.equalsIgnoreCase("body")) {
                throw new HTMLParseException("Expected <body> tag");
            }
        }
        processStartTag(qName, attributes);
    }

    private void processStartTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("body")) {
            processBodyStart(attributes);
        } else if (currentParagraphBuilder != null) {
            if (!currentParagraphBuilder.startTag(tag, attributes)) {
                throw new HTMLParseException("Unknown tag - " + tag);
            }
            if (currentParagraphTag.equalsIgnoreCase(tag)) {
                level++;
            }
        } else if (currentDocumentBuilder != null) {
            currentParagraphBuilder = currentDocumentBuilder.startTag(tag, attributes);
            currentParagraphTag = tag;
        } else {
            throw new HTMLParseException("Unexpected document structure");
        }
    }

    private void processEndTag(String tag) {
        if (tag.equalsIgnoreCase("body")) {
            processBodyEnd();
        } else if (currentParagraphBuilder != null) {
            if (currentParagraphTag.equalsIgnoreCase(tag)) {
                if (level == 0) {
                    currentDocument.addParagraph(currentParagraphBuilder.build());
                    currentParagraphBuilder = null;
                    currentParagraphTag = null;
                } else {
                    currentParagraphBuilder.endTag(tag);
                    level--;
                }
            } else {
                currentParagraphBuilder.endTag(tag);
            }
        } else {
            throw new HTMLParseException("Unexpected document structure");
        }
    }

    private void processBodyStart(Attributes attributes) {
        if (currentDocumentBuilder != null) {
            throw new HTMLParseException("Unexpected document structure");
        }
        currentDocumentBuilder = htmlDocumentBuilderFactory.create(attributes);
        currentDocument = currentDocumentBuilder.createDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (currentDocumentBuilder == null) {
            throw new HTMLParseException("End element outside of <body> tag");
        }
        processEndTag(qName);
    }

    private void processBodyEnd() {
        if (currentParagraphBuilder != null || currentDocumentBuilder == null) {
            throw new HTMLParseException("Unexpected document structure");
        }
        currentDocumentBuilder = null;
        resultDocument = currentDocument;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String text = new String(ch, start, length);
        if (currentParagraphBuilder == null && !text.trim().isEmpty()) {
            throw new HTMLParseException("Characters outside of <body> tag");
        } else if (currentParagraphBuilder != null) {
            currentParagraphBuilder.text(text);
        }
    }

    public DocumentData getDocument() {
        return resultDocument;
    }
}
