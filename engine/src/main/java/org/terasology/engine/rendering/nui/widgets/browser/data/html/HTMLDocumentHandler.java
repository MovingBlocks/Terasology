// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

import org.terasology.engine.rendering.nui.widgets.browser.data.DocumentData;
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
        if (currentDocumentBuilder == null && !qName.equalsIgnoreCase("body")) {
            throw new HTMLParseException("Expected <body> tag");
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
